package com.yee.trading.auto.order;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yee.trading.auto.broker.BrokerInterfaceException;
import com.yee.trading.auto.stockinfo.StockQuote;
import com.yee.trading.auto.util.NotificationEventUtil;
import com.yee.trading.auto.util.SpringContext;

public class ImmediateOpenOrderExecutor extends OrderExecutor implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(ImmediateOpenOrderExecutor.class);
	private Order order;
	public static final BigDecimal zero = new BigDecimal(0);
	private BigDecimal limit;
	private Date lastOrderTime;
	private final static int STOCK_QUOTE_RETRY_COUNT = 3; 

	public ImmediateOpenOrderExecutor(Order order, Date lastOrderTime, double dlimit, SpringContext springContext) {
		// initialize super class
		super(springContext);
		this.order = order;
		limit = new BigDecimal(dlimit);
		this.lastOrderTime = lastOrderTime;
	}
	
	public BigDecimal getOrderPrice(Order order, OrderBook orderBook)  {	

		BigDecimal price = null;
		
		// get closest sell queue as market price for buy order
		if (order.getOrderType() == OrderType.Buy) {
			if(((orderBook.getBuyQueueTotalQuantity() * 2) < orderBook.getSellQueueTotalQuantity()) 
					&& ((orderBook.getHighestBuyQuantity() * 2 ) < orderBook.getLowestSellQuantity())){
				if (orderBook.getSpread() > 1) {
					// queue ahead 1 bps					
					price = orderBook.getHighestBuyPrice().add(orderBook.getBipsValue()); 
				} else {
					price = orderBook.getHighestBuyPrice();
				}
			} else {
				if (orderBook.getSpread() > 2) { //illiquid share					
					long halfSpread = Math.round((double)orderBook.getSpread()/2);
					price = orderBook.getHighestBuyPrice().add(orderBook.getBipsValue().multiply(new BigDecimal(halfSpread)));
				} else {
					price = orderBook.getLowestSellPrice();
				}
			}

		} else {
			if ((orderBook.getBuyQueueTotalQuantity() > (orderBook.getSellQueueTotalQuantity() * 2))
					&& (orderBook.getHighestBuyQuantity() > (orderBook.getLowestSellQuantity() * 2))) {
				if (orderBook.getSpread() > 1) {
					// queue ahead 1 bps					
					price = orderBook.getLowestSellPrice().subtract(orderBook.getBipsValue());
				} else {
					price = orderBook.getLowestSellPrice();
				}
			} else {
				if (orderBook.getSpread() > 2) { //illiquid share
					long halfSpread = Math.round((double)orderBook.getSpread()/2);
					price = orderBook.getLowestSellPrice().subtract(orderBook.getBipsValue().multiply(new BigDecimal(halfSpread)));
				}else{
					price = orderBook.getHighestBuyPrice();	;
				}
				
			}
		}
		return price;
	}

	// buy at market (as close as open price)
//	public BigDecimal getOrderPrice(Order order, OrderBook orderBook) {		
//		
//		if(order.getOrderType() == OrderType.Buy){			
//		    return orderBook.getLowestSellPrice();
//		} else{			
//			return orderBook.getHighestBuyPrice();			
//		}	
//		
//		
//	}

	public void run() {

		try {
			// poll until open price > 0 in first hour
			while (true) {
				Date currentTime = Calendar.getInstance().getTime();
				if (currentTime.after(lastOrderTime) && (order.getOrderType() == OrderType.Buy)) {
					getEventProcessor().onEvent(NotificationEventUtil.createNotificationEvent(
							"Order is not submitted due to exceed maximum open order time - " + order.getStockName()));

					break;
				} else {
					StockQuote stockQuote = null;
					int retryCounter = 1;
					while(true) {
						retryCounter++;
						try{
							stockQuote = getBrokerInterfaceManager().queryStockQuote(order.getStockCode());
							break;
						} catch (BrokerInterfaceException e) {
							if(retryCounter > STOCK_QUOTE_RETRY_COUNT)
								throw e;
						}						
					}
					OrderBook orderBook = getOrderBookAnalyzer().analyze(stockQuote.getBuyQueues(),
							stockQuote.getSellQueues());
					if (stockQuote.getOpenPrice().compareTo(zero) > 0
							&& orderBook.getLowestSellPrice().compareTo(orderBook.getHighestBuyPrice()) > 0) {
						// if open price > yesterday close for more than limit,
						// stop
						// buying

						if (((order.getOrderType() == OrderType.Buy) && (stockQuote.getOpenPrice()
								.compareTo(stockQuote.getPreviousClosePrice().multiply(limit)) <= 0))
								|| (order.getOrderType() == OrderType.Sell)) {
							// submit order
							order.setPrice(getOrderPrice(order, orderBook));
							order = submitOrder(order);
							logger.info("New order is submitted." + order.toString());
						} else {
							getEventProcessor().onEvent(NotificationEventUtil.createNotificationEvent(
									"Order is not submitted due to condition not meet - " + order.getStockName()));
						}

						break;
					} else {
						try {
							Thread.sleep(10000); // pause for 10 seconds
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}

		} catch (BrokerInterfaceException e) {
			logger.error("Error submitting order for " + order.getStockName() + " - " + e.getMessage());
			getEventProcessor().onEvent(NotificationEventUtil.createNotificationEvent(
					"Error submitting order for " + order.getStockName() + " - " + e.getMessage()));
			e.printStackTrace();
		}
	}
	
//	public static void main(String[] args) {
//		Order order = new Order();
//		order.setOrderType(OrderType.Buy);
//		OrderBook orderBook = new OrderBook();
//		orderBook.setBipsValue(new BigDecimal("0.01"));
//		orderBook.setSpread(2);
//		orderBook.setHighestBuyPrice(new BigDecimal("1.01"));
//		orderBook.setHighestBuyQuantity(1000);
//		orderBook.setBuyQueueTotalQuantity(1000);
//		orderBook.setLowestSellPrice(new BigDecimal("1.03"));
//		orderBook.setLowestSellQuantity(3000);
//		orderBook.setSellQueueTotalQuantity(7000);
//		System.out.println(getOrderPrice(order, orderBook));
//	}

}
