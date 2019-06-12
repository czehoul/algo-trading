package com.yee.trading.auto.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.yee.trading.auto.broker.BrokerInterfaceException;
import com.yee.trading.auto.broker.BrokerInterfaceManager;
import com.yee.trading.auto.dao.OrderDao;
import com.yee.trading.auto.event.EventProcessor;
import com.yee.trading.auto.event.EventType;
import com.yee.trading.auto.event.OrderEvent;
import com.yee.trading.auto.order.GenericOrderStatusManager;
import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.order.OrderBook;
import com.yee.trading.auto.order.OrderBookAnalyzer;
import com.yee.trading.auto.order.OrderType;
import com.yee.trading.auto.portfolio.PortfolioManager;
import com.yee.trading.auto.stockinfo.OrderStatus;
import com.yee.trading.auto.stockinfo.StockQuote;
import com.yee.trading.auto.util.NotificationEventUtil;
import com.yee.trading.auto.util.TradingDayChecker;

@Component("intradayOpenStrategyHandler")
public class IntradayOpenStrategyHandlerImpl implements IntradayOpenStrategyHandler {
	@Autowired	
	@Qualifier("simulationBrokerInterfaceManager")
	private BrokerInterfaceManager simulationBrokerInterfaceManager;
	@Qualifier("hLeBrokingInterfaceManager")
	private BrokerInterfaceManager brokerInterfaceManager;
	@Autowired
	private EventProcessor eventProcessor;
	private static final BigDecimal zero = BigDecimal.ZERO;
	private static final BigDecimal hundred = new BigDecimal(100);
	@Autowired
	private OrderBookAnalyzer orderBookAnalyzer;
	@Autowired
	@Qualifier("autoTradingScheduler")
	private TaskScheduler taskScheduler;
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private PortfolioManager portfolioManager;
	@Autowired
	private TradingDayChecker tradingDayChecker;

	private Date maxOrderTime;
	private Date sellOrderTime;
	private Date pollingTime;

	// configure as property
	private BigDecimal minTransactionValue = new BigDecimal(100000);
	private BigDecimal minOrderValue = new BigDecimal(8000);

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private final Logger logger = LoggerFactory.getLogger(GenericOrderStatusManager.class);

	public IntradayOpenStrategyHandlerImpl(@Value("${intraday.open.max.order.time}") String maxOrderTimeStr,
			@Value("${intraday.open.sell.order.time}") String sellOrderTimeStr,
			@Value("${intraday.open.polling.time}") String pollingTimeStr) {
		String currentDateStr = dateFormat.format(new Date());
		String maxOrderDateTimeStr = String.format("%s %s", currentDateStr, maxOrderTimeStr);
		String sellOrderDateTimeStr = String.format("%s %s", currentDateStr, sellOrderTimeStr);
		String pollingDateTimeStr = String.format("%s %s", currentDateStr, pollingTimeStr);
		try {
			maxOrderTime = dateTimeFormat.parse(maxOrderDateTimeStr);
			sellOrderTime = dateTimeFormat.parse(sellOrderDateTimeStr);
			pollingTime = dateTimeFormat.parse(pollingDateTimeStr);
		} catch (ParseException e) {
			new RuntimeException(e);
		}
	}

	// get the pro
	private BigDecimal getOrderPrice(OrderType orderType, StockQuote stockQuote, int orderQuantity) {
		OrderBook orderBook = orderBookAnalyzer.analyze(stockQuote.getBuyQueues(), stockQuote.getSellQueues());
		if (orderType == OrderType.Buy) {
			BigDecimal firstLevelSellVal = orderBook.getLowestSellPrice()
					.multiply(new BigDecimal(orderBook.getLowestSellQuantity() * 100));
			if (firstLevelSellVal.compareTo(minOrderValue) > 0) {
				return orderBook.getLowestSellPrice();
			} else {
				BigDecimal secondLevelSellVal = orderBook.getSecondLowestSellPrice()
						.multiply(new BigDecimal(orderBook.getSecondLowestSellQuantity() * 100));
				if ((secondLevelSellVal.add(firstLevelSellVal)).compareTo(minOrderValue) > 0) {
					return orderBook.getSecondLowestSellPrice();
				} else {
					return null;
				}
			}
		} else {

			if (orderBook.getHighestBuyQuantity() > orderQuantity) {
				return orderBook.getHighestBuyPrice();
			} else {
				if ((orderBook.getHighestBuyQuantity() + orderBook.getSecondHighestBuyQuantity()) > orderQuantity) {
					return orderBook.getSecondHighestBuyPrice();
				} else {
					// very rare situation - send notification and handle
					// manually for the time being
					return null;
				}
			}
		}
	}

	private Order createOrder(Strategy strategy, OrderType orderType, IntradayOpenSignal signal, BigDecimal orderPrice,
			int quantity) throws BrokerInterfaceException {

		Order order = new Order();
		order.setOrderExecutionType(orderType == OrderType.Buy ? strategy.getBuyOrderExecutionType()
				: strategy.getSellOrderExecutionType());
		order.setOrderType(orderType);
		order.setPrice(orderPrice);
		order.setStockCode(signal.getStockCode());
		order.setStockName(signal.getStockName());		
		order.setStrategy(strategy);
		order.setQuantity(quantity);
		order.setLive(strategy.isLive());
		return order;
	}

	private Order triggerOrderEvent(OrderType orderType, IntradayOpenSignal signal, BigDecimal orderPrice, int quantity)
			throws BrokerInterfaceException {
		Calendar today = Calendar.getInstance();
		if(tradingDayChecker.nonTradingDay(today)){
			logger.info("Today is a holiday, order will not be submitted.");
			eventProcessor.onEvent(
					NotificationEventUtil.createNotificationEvent("Today is a holiday, order will not be submitted."));
			return null;
		}
		Log.debug(String.format("Triggering %s order event for stock %s", orderType.toString(), signal));
		OrderEvent orderEvent = new OrderEvent(EventType.ORDER_GENERATED);
		Order order = createOrder(signal.getStrategy(), orderType, signal, orderPrice, quantity);
		orderEvent.setOrder((order));
		eventProcessor.onEvent(orderEvent);
		return order;
	}

	// @Async("autoTradingTaskExecutor")
	@Async
	public void handleStrategy(IntradayOpenSignal signal) {
		// start pooling stock quote 1 minute/30 Second before MO for every 5/10
		// second
		// once open price come in then compare with previous close price, if
		// open price > previous close price,
		// then verify decent volume, if yes buy
		//
		// if open price = close price keep polling after price move up a few
		// bips with decent volume, buy
		// after buy keep polling every minute, sell:
		// -if price > 10% from buy price close the position
		// -if price < 5% from buy price close the position
		// - 5% trailing stop?
		// - if time >= 9.30 am close the position
		// -----------------
		// submit via market order order manager - hv to calculate order price
		// to make sure order match immediately
		// poll getOrderStatus to confirm order status
		// after all match start polling stock quote, sell if the above
		// condition match

		try {
			// poll until open price > 0 in first hour
			Order order = null;
			while (true) {
				Date currentTime = Calendar.getInstance().getTime();
				if (currentTime.after(pollingTime)) {
					StockQuote stockQuote = getBrokerInterfaceManager(order).queryStockQuote(signal.getStockCode());

					if (stockQuote.getOpenPrice().compareTo(zero) > 0
							&& stockQuote.getOpenPrice().compareTo(signal.getPreviousClosePrice()) > 0
							&& stockQuote.getValueTraded().compareTo(minTransactionValue) > 0
							&& currentTime.before(maxOrderTime)) {

						BigDecimal orderPrice = getOrderPrice(OrderType.Buy, stockQuote, 0);
						if (orderPrice == null) {
							logger.error("Error submitting buy order for " + signal.getStockName()
									+ " - issuficient first and second level volume");
							eventProcessor.onEvent(
									NotificationEventUtil.createNotificationEvent("Error submitting buy order for "
											+ signal.getStockName() + " - issuficient first and second level volume"));
						} else {
							int quantity = minOrderValue.divide(orderPrice, 3, RoundingMode.HALF_UP)
									.divide(hundred, 3, RoundingMode.HALF_UP).intValue();
							order = triggerOrderEvent(OrderType.Buy, signal, orderPrice, quantity);
							if(order != null)
								order.setOrderDate(currentTime);
						}
						break;
					}
				}

				// stop polling and do not submit order after max order time
				if (currentTime.after(maxOrderTime)) {
					logger.error("Stop submitting buy order for " + signal.getStockName() + " - exceed max order time");
					eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
							"Stop submitting buy order for " + signal.getStockName() + " - exceed max order time"));
					break;
				}

				try {
					Thread.sleep(10000); // pause for 10 seconds
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
			if (order != null) {
				SellOrderHandler sellOrderHandler = new SellOrderHandler(order, signal);
				taskScheduler.schedule(sellOrderHandler, sellOrderTime);
			} else {
				eventProcessor.onEvent(NotificationEventUtil
						.createNotificationEvent("Intraday open order not submitted, creteria doesn't meet for order - "
								+ signal.getStockName()));
			}

		} catch (BrokerInterfaceException e) {
			logger.error("Error submitting order for " + signal.getStockName() + " - " + e.getMessage());
			eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
					"Error submitting order for " + signal.getStockName() + " - " + e.getMessage()));
			e.printStackTrace();
		}

	}

	private class SellOrderHandler implements Runnable {
		private Order order;
		private IntradayOpenSignal signal;

		public SellOrderHandler(Order order, IntradayOpenSignal signal) {
			this.order = order;
			this.signal = signal;
		}
		
		private void exceuteSellOrder(int quantity) throws BrokerInterfaceException {
			//trigger sell order
			StockQuote matchStockQuote = getBrokerInterfaceManager(order).queryStockQuote(signal.getStockCode());
			BigDecimal matchOrderPrice = getOrderPrice(OrderType.Sell, matchStockQuote, quantity);
			if (matchOrderPrice == null) {
				logger.error("Error submitting sell order for " + signal.getStockName()
						+ " - issuficient first and second level volume");
				eventProcessor
						.onEvent(NotificationEventUtil.createNotificationEvent("Error submitting sell order for "
								+ signal.getStockName() + " - issuficient first and second level volume"));
			} else {
				triggerOrderEvent(OrderType.Sell, signal, matchOrderPrice, quantity);
			}
		}

		@Override
		public void run() {
			try {
				OrderStatus orderStatus = getBrokerInterfaceManager(order).queryOrdersStatus(order);
				//check order status, if buy order was not matched, simply cancel the order, else
				//only execute sell order
				// sell
				Order previousOrder = orderDao.getOrderByOrderStatus(orderStatus);
				switch (orderStatus.getOrderStatusType()) {
				case ALL_MATCHED:
					//trigger sell order
					exceuteSellOrder(order.getQuantity());
					break;
				case PARTIALLY_MATCHED:
					//reduce order
					int quantityToReduce = order.getQuantity() - orderStatus.getMatchedQuantity();
					portfolioManager.reduceOrder(previousOrder, quantityToReduce);
					getBrokerInterfaceManager(order).reduceOrder(orderStatus.getBrokerOrderId(), orderStatus.getPrice(),
							quantityToReduce);
					//trigger sell order
					exceuteSellOrder(orderStatus.getMatchedQuantity());
					
					break;
				default:
					//cancel order
					portfolioManager.reduceOrder(previousOrder, order.getQuantity());
					getBrokerInterfaceManager(order).reduceOrder(orderStatus.getBrokerOrderId(), orderStatus.getPrice(),
							order.getQuantity());
					break;
				}
				
				
			} catch (BrokerInterfaceException e) {
				logger.error(String.format("Error submitting sell order for %s - %s", signal.getStockName(), e.getMessage()));
				eventProcessor
					.onEvent(NotificationEventUtil.createNotificationEvent(String.format("Error submitting sell order for %s - %s", signal.getStockName(), e.getMessage())));
				e.printStackTrace();
				
			}

		}

	}
	
	private BrokerInterfaceManager getBrokerInterfaceManager(Order order){
		if(order.isLive()){
			return brokerInterfaceManager;
		} else{
			return simulationBrokerInterfaceManager;
		}
	}
	
//	public static void main(String[] args) {
//		IntradayOpenStrategyHandlerImpl impl = new IntradayOpenStrategyHandlerImpl();
//		OrderBookAnalyzer analyzer = new OrderBookAnalyzer();
//		impl.orderBookAnalyzer = analyzer;
//		StockQuote stockQuote = new StockQuote();
//		stockQuote.setStokCode("5095");
//		Map<BigDecimal, Integer> buyQueues = new LinkedHashMap<>();
//		buyQueues.put(new BigDecimal(1.2).setScale(3, RoundingMode.HALF_UP), 20);
//		buyQueues.put(new BigDecimal(1.19).setScale(3, RoundingMode.HALF_UP), 100);
//		buyQueues.put(new BigDecimal(1.18).setScale(3, RoundingMode.HALF_UP), 300);
//		stockQuote.setBuyQueues(buyQueues);
//		Map<BigDecimal, Integer> sellQueues = new LinkedHashMap<>();
//		sellQueues.put(new BigDecimal(1.21).setScale(3, RoundingMode.HALF_UP), 5);
//		sellQueues.put(new BigDecimal(1.22).setScale(3,RoundingMode.HALF_UP), 1);
//		sellQueues.put(new BigDecimal(1.23).setScale(3, RoundingMode.HALF_UP), 3);
//		stockQuote.setSellQueues(sellQueues);
//		BigDecimal orderPrice = impl.getOrderPrice(OrderType.Buy, stockQuote, 21);
//		System.out.println(orderPrice);
//	}

}
