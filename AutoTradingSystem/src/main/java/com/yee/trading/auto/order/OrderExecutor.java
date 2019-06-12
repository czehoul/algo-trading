package com.yee.trading.auto.order;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.broker.BrokerInterfaceException;
import com.yee.trading.auto.broker.BrokerInterfaceManager;
import com.yee.trading.auto.event.EventProcessor;
import com.yee.trading.auto.portfolio.PortfolioManager;
import com.yee.trading.auto.portfolio.PortfolioManagerException;
import com.yee.trading.auto.stockinfo.StockQuote;
import com.yee.trading.auto.util.NotificationEventUtil;
import com.yee.trading.auto.util.SpringContext;

public abstract class OrderExecutor {
	
	private final Logger logger = LoggerFactory.getLogger(OrderExecutor.class);
	private EventProcessor eventProcessor;
	private PortfolioManager portfolioManager;
	private BrokerInterfaceManager brokerInterfaceManager;
	private BrokerInterfaceManager simulationBrokerInterfaceManager;
	private OrderBookAnalyzer orderBookAnalyzer;
	private SpringContext springContext;
	
	public OrderExecutor(SpringContext springContext){
		this.springContext = springContext;
		orderBookAnalyzer = (OrderBookAnalyzer) springContext.getApplicationContext().getBean("orderBookAnalyzer");
		brokerInterfaceManager = (BrokerInterfaceManager) springContext.getApplicationContext().getBean("hLeBrokingInterfaceManager");
		simulationBrokerInterfaceManager = (BrokerInterfaceManager) springContext.getApplicationContext().getBean("simulationBrokerInterfaceManager");
		portfolioManager = (PortfolioManager) springContext.getApplicationContext().getBean("klsePortfolioManager");
		eventProcessor = (EventProcessor) springContext.getApplicationContext().getBean("eventProcessor");		
	}
	
	public BigDecimal getOrderPrice(Order order, StockQuote stockQuote) {
		OrderBook orderBook = orderBookAnalyzer.analyze(stockQuote.getBuyQueues(), stockQuote.getSellQueues());
		if(order.getOrderType() == OrderType.Buy){
			//buy as close as open price - next lowest sell price - if spread > 1 then can queue at 1 bips lower
			//if best buy price (lowest sell price) is same or lower than open price then just buy at market
			if(orderBook.getSpread() > 1){
				return orderBook.getHighestBuyPrice().add(orderBook.getBipsValue());
			} else{
				return orderBook.getLowestSellPrice();
			}	
			
		} else{
			if(orderBook.getSpread() > 1){
				return orderBook.getLowestSellPrice().subtract(orderBook.getBipsValue());
			} else{
				return orderBook.getHighestBuyPrice();
			}
		}		
	}
	
	//transaction will not work
	@Transactional(rollbackFor = { BrokerInterfaceException.class })
	public Order submitOrder(Order order) throws BrokerInterfaceException {
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
				if(order.isLive())
					brokerInterfaceManager.submitOrder(order);
				else
					simulationBrokerInterfaceManager.submitOrder(order);
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
				if(order.isLive())
					brokerInterfaceManager.submitOrder(order);
				else
					simulationBrokerInterfaceManager.submitOrder(order);
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

	public EventProcessor getEventProcessor() {
		return eventProcessor;
	}

	public PortfolioManager getPortfolioManager() {
		return portfolioManager;
	}

	public BrokerInterfaceManager getBrokerInterfaceManager() {
		return brokerInterfaceManager;
	}

	public OrderBookAnalyzer getOrderBookAnalyzer() {
		return orderBookAnalyzer;
	}

	public BrokerInterfaceManager getSimulationBrokerInterfaceManager() {
		return simulationBrokerInterfaceManager;
	}

	
	
}
