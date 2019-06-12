package com.yee.trading.auto.order;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.broker.BrokerInterfaceException;
import com.yee.trading.auto.broker.BrokerInterfaceManager;
import com.yee.trading.auto.event.EventProcessor;
import com.yee.trading.auto.portfolio.PortfolioManager;
import com.yee.trading.auto.portfolio.PortfolioManagerException;
import com.yee.trading.auto.stockinfo.OrderStatus;
import com.yee.trading.auto.stockinfo.OrderStatus.OrderStatusType;
import com.yee.trading.auto.stockinfo.StockQuote;
import com.yee.trading.auto.util.NotificationEventUtil;
import com.yee.trading.auto.util.SpringContext;

@Component
@Scope("prototype")
public class GenericOrderManager implements OrderManager {
	private Order order;

	@Autowired
	@Qualifier("autoTradingScheduler")
	private TaskScheduler taskScheduler;
	@Autowired	
	@Qualifier("simulationBrokerInterfaceManager")
	private BrokerInterfaceManager simulationBrokerInterfaceManager;
	@Autowired
	@Qualifier("hLeBrokingInterfaceManager")
	private BrokerInterfaceManager brokerInterfaceManager;
	@Autowired
	private PortfolioManager portfolioManager;
	@Autowired
	private EventProcessor eventProcessor;
	private Date marketOpenOrderTime;
	private Date marketOpenSecondOrderTime;
	private Date marketOpenLatestOrderTime;
	private Date marketCloseOrderTime;
	@Value("${max.order.submit.count:2}")
	private int maxOrderSubmitCount;
	@Value("${open.price.limit:1.1}")
	private double openPriceLimit;
	@Autowired
	private OrderBookAnalyzer orderBookAnalyzer;
	@Autowired
	private SpringContext springContext;

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private final Logger logger = LoggerFactory.getLogger(GenericOrderManager.class);

	// inject time in this format HH:mm:ss.SSS
	public GenericOrderManager(@Value("${market.open.order.time}") String marketOpenOrderTimeStr,
			@Value("${market.open.second.order.time}") String marketOpenSecondOrderTimeStr,
			@Value("${market.open.latest.order.time}") String marketOpenLatestOrderTimeStr,
			@Value("${market.close.order.time}") String marketCloseOrderTimeStr) {
		String currentDateStr = dateFormat.format(new Date());
		String marketOpenDateTimeStr = String.format("%s %s", currentDateStr, marketOpenOrderTimeStr);
		String marketCloseDateTimeStr = String.format("%s %s", currentDateStr, marketCloseOrderTimeStr);
		String marketOpenSecondOrderDateTimeStr = String.format("%s %s", currentDateStr, marketOpenSecondOrderTimeStr);
		String marketOpenLatestOrderDateTimeStr = String.format("%s %s", currentDateStr, marketOpenLatestOrderTimeStr);
		try {
			marketOpenOrderTime = dateTimeFormat.parse(marketOpenDateTimeStr);
			marketOpenSecondOrderTime = dateTimeFormat.parse(marketOpenSecondOrderDateTimeStr);
			marketOpenLatestOrderTime = dateTimeFormat.parse(marketOpenLatestOrderDateTimeStr);
			marketCloseOrderTime = dateTimeFormat.parse(marketCloseDateTimeStr);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void run() {
		try {
			switch (order.getOrderExecutionType()) {
			case OPEN:
				// run market order at open time

//				Date currentTime = new Date();
//				if (currentTime.after(marketOpenSecondOrderTime) && currentTime.before(marketOpenLatestOrderTime)) {
//					MarketOrderExecutor marketOrderExecutor = new MarketOrderExecutor(order);
//					taskScheduler.schedule(marketOrderExecutor, marketOpenLatestOrderTime);
//				} else if (currentTime.after(marketOpenOrderTime) && currentTime.before(marketOpenSecondOrderTime)) {
//					MarketOrderExecutor marketOrderExecutor = new MarketOrderExecutor(order);
//					taskScheduler.schedule(marketOrderExecutor, marketOpenSecondOrderTime);
//				} else if (currentTime.before(marketOpenOrderTime)) {
//					// this is the default
//					OpenOrderExecutor openOrderExecutor = new OpenOrderExecutor(order);
//					taskScheduler.schedule(openOrderExecutor, marketOpenOrderTime);
//				}
				ImmediateOpenOrderExecutor immediateOpenOrderExecutor = new ImmediateOpenOrderExecutor(order, marketOpenLatestOrderTime, openPriceLimit, springContext);
				taskScheduler.schedule(immediateOpenOrderExecutor, marketOpenOrderTime);
				break;
			case CLOSE:
				// run market order at close time
				MarketOrderExecutor closeOrderExecutor = new MarketOrderExecutor(order);
				taskScheduler.schedule(closeOrderExecutor, marketCloseOrderTime);
				break;
			case MARKET:
				MarketOrderExecutor marketOrderExecutor = new MarketOrderExecutor(order);
				marketOrderExecutor.run();
				break;
			case LIMIT:				
				submitOrder(order);//submit order directly, price has been passed in				
				break;
			default:
				logger.error("Close order execution type " + order.getOrderExecutionType() + " not yet supported.");
				eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
						"Close order execution type " + order.getOrderExecutionType() + " not yet supported."));
				break;
			}
		}catch(BrokerInterfaceException e){ 
			logger.error("Error submitting order for " + order.getStockName() + " - " + e.getMessage());
			eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
					"Error submitting order for " + order.getStockName() + " - " + e.getMessage()));
			e.printStackTrace();
		}catch (RuntimeException re) {
			logger.error("Runtime exception occured in GenericOrderManager", re);
			eventProcessor.onEvent(NotificationEventUtil
					.createNotificationEvent("Runtime exception occured in GenericOrderManager" + re.getMessage()));
		}
		// if (order.getOrderExecutionType() == OrderExecutionType.OPEN) {
		// taskScheduler.schedule(arg0, arg1)
		// taskScheduler.schedule(arg0, new CronTrigger("* 9-12 * *
		// MON-FRI"));
		// ScheduledFuture<V> future =
		// taskScheduler.scheduleWithFixedDelay(runnable, starttime,
		// 30*1000);
		// }

	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	/**
	 * Implement order resubmit, currently it only 2 order submission is allowed
	 * at fixed time ie when market just open and 10.30 am But in future it can
	 * be enhanced to support multiple submission more than 2 by using
	 * taskScheduler.scheduleAtFixedRate to retry after certain interval
	 * 
	 * @author CzeHoul
	 * 
	 */
	private class OpenOrderExecutor implements Runnable {
		private Order order;

		public OpenOrderExecutor(Order order) {
			this.order = order;
		}

		public void run() {

			try {
				if (order.getSubmitCount() > maxOrderSubmitCount) {
					logger.info("Exceed max order submit count. Order will not be submitted. " + order.toString());
					eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
							"Exceed max order submit count. Order will not be submitted. " + order.toString()));
					return;
				}
				if (order.getSubmitCount() > 0) {
					// previous order exist
					// TODO - in simulation replace with another interface (DAO)
					logger.debug("Order resubmit check for order - " + order.toString());
					OrderStatus orderStatus = getBrokerInterfaceManager(order).queryOrdersStatus(order);
					if (orderStatus.getOrderStatusType() == OrderStatusType.ALL_MATCHED) {
						// update status to DB
						// can do it eod too
						logger.info("Order submitted is all matched. " + order.toString());
						return;
					} else {
						logger.debug("Order submitted is not all matched and will be resubmit. " + order.toString());
						// TODO replace with following for simulation
						StockQuote stockQuote = getBrokerInterfaceManager(order).queryStockQuote(order.getStockCode());
						BigDecimal marketPrice = null;
						// get closest sell queue as market price for buy order
						if (order.getOrderType() == OrderType.Buy) {
							// Set<Entry<BigDecimal, Integer>> sellQueues =
							// stockQuote.getSellQueues().entrySet();
							for (Entry<BigDecimal, Integer> sellQueue : stockQuote.getSellQueues().entrySet()) {
								marketPrice = sellQueue.getKey();
								break;
							}
						} else {
							// Set<Entry<BigDecimal, Integer>> buyQueues =
							// stockQuote.getBuyQueues().entrySet();
							for (Entry<BigDecimal, Integer> buyQueue : stockQuote.getBuyQueues().entrySet()) {
								marketPrice = buyQueue.getKey();
								break;
							}
						}
						if (marketPrice.compareTo(order.getPrice()) == 0) {
							return;
						} else {

							// reduce order
							getBrokerInterfaceManager(order).reduceOrder(orderStatus.getBrokerOrderId(), orderStatus.getPrice(),
									order.getQuantity() - orderStatus.getMatchedQuantity());
							Order newOrder = (Order) order.clone();

							newOrder.setPrice(marketPrice);
							// resubmit order
							newOrder = reSubmitOrder(order, orderStatus, newOrder);
						}
					}
				} else {
					// new order
					order.setPrice(getOptimizedPrice(order.getStockCode()));
					order = submitOrder(order);
					OpenOrderExecutor openOrderExecutor = new OpenOrderExecutor(order);
					logger.info("New order is submitted. Order status verifying/resubmitting is scheduled at "
							+ marketOpenSecondOrderTime + ". " + order.toString());
					taskScheduler.schedule(openOrderExecutor, marketOpenSecondOrderTime);

				}

			} catch (BrokerInterfaceException e) {
				logger.error("Error submitting order for " + order.getStockName() + " - " + e.getMessage());
				eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
						"Error submitting order for " + order.getStockName() + " - " + e.getMessage()));
				e.printStackTrace();
			} catch (CloneNotSupportedException e) {
				// code issue, wont happen in runtime
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	private class MarketOrderExecutor implements Runnable {
		private Order order;

		public MarketOrderExecutor(Order order) {
			this.order = order;
		}

		public void run() {
			// poll status
			// then submit at market to match sell queue
			try {
				
				if(order.getPrice() == null){
					StockQuote stockQuote = getBrokerInterfaceManager(order).queryStockQuote(order.getStockCode());
					BigDecimal price = null;
					// get closest sell queue as market price for buy order
					if (order.getOrderType() == OrderType.Buy) {
						// Set<Entry<BigDecimal, Integer>> sellQueues =
						// stockQuote.getSellQueues().entrySet();
						for (Entry<BigDecimal, Integer> sellQueue : stockQuote.getSellQueues().entrySet()) {
							price = sellQueue.getKey();
							break;
						}
					} else {
						// Set<Entry<BigDecimal, Integer>> buyQueues =
						// stockQuote.getBuyQueues().entrySet();
						for (Entry<BigDecimal, Integer> buyQueue : stockQuote.getBuyQueues().entrySet()) {
							price = buyQueue.getKey();
							break;
						}
					}
					// price = price.setScale(3, RoundingMode.HALF_UP);
					order.setPrice(price);
				}

				submitOrder(order);

			} catch (BrokerInterfaceException e) {
				logger.error("Error submitting order for " + order.getStockName() + " - " + e.getMessage());
				eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
						"Error submitting order for " + order.getStockName() + " - " + e.getMessage()));
				e.printStackTrace();
			}
		}

	}

	private BigDecimal getOptimizedPrice(String stockCode) throws BrokerInterfaceException {
		StockQuote stockQuote = getBrokerInterfaceManager(order).queryStockQuote(stockCode);

		BigDecimal price = null;

		OrderBook orderBook = orderBookAnalyzer.analyze(stockQuote.getBuyQueues(), stockQuote.getSellQueues());
		// get closest sell queue as market price for buy order
		if (order.getOrderType() == OrderType.Buy) {
			if (orderBook.getBuyQueueTotalQuantity() > orderBook.getSellQueueTotalQuantity()
					&& orderBook.getHighestBuyQuantity() > orderBook.getLowestSellQuantity()
					&& orderBook.getSpread() <= 2) {
				price = orderBook.getLowestSellPrice();
			} else {
				if (orderBook.getSpread() > 2) {
					price = orderBook.getHighestBuyPrice().add(orderBook.getBipsValue()); // queue
																							// ahead
																							// 1
																							// bps
				} else {
					price = orderBook.getHighestBuyPrice();
				}
			}

		} else {
			if (orderBook.getBuyQueueTotalQuantity() < orderBook.getSellQueueTotalQuantity()
					&& orderBook.getHighestBuyQuantity() < orderBook.getLowestSellQuantity()
					&& orderBook.getSpread() <= 2) {
				price = orderBook.getHighestBuyPrice();
			} else {
				if (orderBook.getSpread() > 1) {
					price = orderBook.getLowestSellPrice().subtract(orderBook.getBipsValue()); // queue
																								// ahead
																								// 1
																								// bps
				} else {
					price = orderBook.getLowestSellPrice();
				}
			}
		}
		return price;
	}

	@Transactional(rollbackFor = { BrokerInterfaceException.class })
	private Order submitOrder(Order order) throws BrokerInterfaceException {
		// logger.debug("Submitting order ----- " + order);
		if (order.getPrice().compareTo(new BigDecimal(0)) <= 0) {
			logger.error("Market quote for stock " + order.getStockName() + " is zero. Order is not submitted.");
			eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
					"Market quote for stock " + order.getStockName() + " is zero. Order is not submitted."));
			return null;
		}

		order.setOrderDate(new Date());
		if (order.getOrderType() == OrderType.Buy) {
			// TODO
			// if no portfolio in order then create portfolio and return
			// portfolio
			// Otherwise update portfolio
			try {
				order = portfolioManager.createPortfolio(order);

				getBrokerInterfaceManager(order).submitOrder(order);
				logger.info("Order submitted. " + order.toString());
				eventProcessor
						.onEvent(NotificationEventUtil.createNotificationEvent("Order submitted. " + order.toString()));

			} catch (PortfolioManagerException e) {
				logger.error(String.format("Order not submitted. %s %s ", order.toString(), e.getMessage()));
				eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
						String.format("Order not submitted. %s %s ", order.toString(), e.getMessage())));
			}

		} else {
			// query quantity in portfolio
			// call dao to remove (change hold status to hold)
			// need to add the cash/position avaiable immediately? or do it
			// defensively in eod update/refresh to avoid over buy
			// for now just do it defensively in eod update/refresh to avoid
			// over buy
			try {
				order = portfolioManager.createOrder(order);

				getBrokerInterfaceManager(order).submitOrder(order);
				logger.info("Order submitted. " + order.toString());
				eventProcessor
						.onEvent(NotificationEventUtil.createNotificationEvent("Order submitted. " + order.toString()));
			} catch (NoResultException e) {
				logger.error("Order not submitted. Not existing portfolio." + order.toString());
				eventProcessor.onEvent(NotificationEventUtil
						.createNotificationEvent("Order not submitted. Not existing portfolio." + order.toString()));
			}
		}
		return order;

	}

	@Transactional(rollbackFor = { BrokerInterfaceException.class })
	private Order reSubmitOrder(Order previousOrder, OrderStatus previousOrderStatus, Order newOrder)
			throws BrokerInterfaceException {
		// logger.debug("Submitting order ----- " + order);
		if (newOrder.getPrice().compareTo(new BigDecimal(0)) <= 0) {
			logger.error("Market quote for stock " + newOrder.getStockName() + " is zero. Order is not submitted.");
			eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
					"Market quote for stock " + newOrder.getStockName() + " is zero. Order is not submitted."));
			return null;
		}
		newOrder.setOrderDate(new Date());

		// if no portfolio in order then create portfolio and return
		// portfolio
		// Otherwise update portfolio
		try {
			newOrder = portfolioManager.recreateOrder(previousOrder, previousOrderStatus, newOrder);

			// TODO comment out so that order will not be sent
			getBrokerInterfaceManager(order).submitOrder(newOrder);

			eventProcessor
					.onEvent(NotificationEventUtil.createNotificationEvent("Order submitted. " + newOrder.toString()));

		} catch (PortfolioManagerException e) {
			eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
					String.format("Order not submitted. %s %s ", newOrder.toString(), e.getMessage())));
		}

		return newOrder;
	}
	
	private BrokerInterfaceManager getBrokerInterfaceManager(Order order){
		if(order.isLive()){
			return brokerInterfaceManager;
		} else{
			return simulationBrokerInterfaceManager;
		}
	}

}
