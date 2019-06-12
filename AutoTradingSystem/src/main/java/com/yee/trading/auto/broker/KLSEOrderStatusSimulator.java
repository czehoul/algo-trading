package com.yee.trading.auto.broker;

import java.math.BigDecimal;
import java.util.Map.Entry;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.dao.OrderDao;
import com.yee.trading.auto.dao.OrderStatusDao;
import com.yee.trading.auto.event.EventProcessor;
import com.yee.trading.auto.order.GenericOrderManager;
import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.order.OrderType;
import com.yee.trading.auto.stockinfo.OrderStatus;
import com.yee.trading.auto.stockinfo.StockQuote;
import com.yee.trading.auto.stockinfo.OrderStatus.OrderStatusType;
import com.yee.trading.auto.util.NotificationEventUtil;

@Component("orderStatusSimulator")
@Scope("prototype")
public class KLSEOrderStatusSimulator implements OrderStatusSimulator {
	private Order order;
	@Autowired
	@Qualifier("simulationBrokerInterfaceManager")
	private BrokerInterfaceManager brokerInterfaceManager;
	@Autowired
	private OrderStatusDao orderStatusDao;
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private EventProcessor eventProcessor;
	private final Logger logger = LoggerFactory.getLogger(KLSEOrderStatusSimulator.class);

	public KLSEOrderStatusSimulator() {

	}

	public void setOrder(Order order) {
		this.order = order;
	}

	@Override
	public void run() {
		// every minutes do the following
		// 1. check order is done by orderdao, if yes stop and exit
		// 2. insert a record to order status table (for the 1st time in loop)
		// 3. query quote, verify the total quantity in order match (or partial
		// match) the queue quantity for the same price
		// 4. if 2. yes then update the match quantity in to order status table
		try {
			OrderStatus orderStatus = createOrderStatus(order);
			String orderStatusId = orderStatus.getBrokerOrderId();
			System.out.println("---->Order status created!");

			try {
				// just to simulate the actual order submission delay
				Thread.sleep(5000);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			boolean polling = true;
			while (polling) {
				try {
					// get the latest orderStatus
					orderStatus = orderStatusDao.getOrderStatusByBrokerOrderId(orderStatusId);
					// orderStatus = matchOrder(orderStatus);
					// start replace
					StockQuote stockQuote = brokerInterfaceManager.queryStockQuote(order.getStockCode());

					int pendingToMatchQuantity = orderStatus.getQuantity() - orderStatus.getMatchedQuantity();

					if (orderStatus.getOrderType() == OrderType.Buy) {
						int sellQueueCounter = 0;
						BigDecimal bestSell = null;
						int bestSellQuantity = 0;

						for (Entry<BigDecimal, Integer> queue : stockQuote.getSellQueues().entrySet()) {
							sellQueueCounter++;
							if (sellQueueCounter == 1) {
								bestSell = queue.getKey();
								bestSellQuantity = queue.getValue();
							}
						}
						if (orderStatus.getPrice().compareTo(bestSell) == 0) {
							if (bestSellQuantity >= pendingToMatchQuantity) { // all
																				// match
								orderStatus
										.setMatchedQuantity(orderStatus.getMatchedQuantity() + pendingToMatchQuantity);
								orderStatus.setOrderStatusType(OrderStatusType.ALL_MATCHED);
								polling = false;
								// eventProcessor.onEvent(
								// NotificationEventUtil.createNotificationEvent("Buy
								// order all matched for " +
								// orderStatus.getStockName()));
							} else { // partially match - can't 100% simulate
										// the actual scenario
								orderStatus.setMatchedQuantity(orderStatus.getMatchedQuantity() + bestSellQuantity);
								orderStatus.setOrderStatusType(OrderStatusType.PARTIALLY_MATCHED);
							}
						} else if (orderStatus.getPrice().compareTo(bestSell) == 1) {
							orderStatus.setMatchedQuantity(orderStatus.getMatchedQuantity() + pendingToMatchQuantity);
							orderStatus.setOrderStatusType(OrderStatusType.ALL_MATCHED);
							polling = false;
							// eventProcessor.onEvent(
							// NotificationEventUtil.createNotificationEvent("Buy
							// order all matched for " +
							// orderStatus.getStockName()));
						}
					} else {
						int buyQueueCounter = 0;
						BigDecimal bestBuy = null;
						int bestBuyQuantity = 0;
						for (Entry<BigDecimal, Integer> queue : stockQuote.getBuyQueues().entrySet()) {
							buyQueueCounter++;
							if (buyQueueCounter == 1) {
								bestBuy = queue.getKey();
								bestBuyQuantity = queue.getValue();
							}
						}
						if (orderStatus.getPrice().compareTo(bestBuy) == 0) {
							if (bestBuyQuantity >= pendingToMatchQuantity) { // all
																				// match
								orderStatus
										.setMatchedQuantity(orderStatus.getMatchedQuantity() + pendingToMatchQuantity);
								orderStatus.setOrderStatusType(OrderStatusType.ALL_MATCHED);
								polling = false;
								// eventProcessor.onEvent(
								// NotificationEventUtil.createNotificationEvent("Sell
								// order all matched for " +
								// orderStatus.getStockName()));
							} else { // partially match - can't 100% simulate
										// the actual scenario
								orderStatus.setMatchedQuantity(orderStatus.getMatchedQuantity() + bestBuyQuantity);
								orderStatus.setOrderStatusType(OrderStatusType.PARTIALLY_MATCHED);
							}
						} else if (orderStatus.getPrice().compareTo(bestBuy) == -1) {
							orderStatus.setMatchedQuantity(orderStatus.getMatchedQuantity() + pendingToMatchQuantity);
							orderStatus.setOrderStatusType(OrderStatusType.ALL_MATCHED);
							polling = false;
							// eventProcessor.onEvent(
							// NotificationEventUtil.createNotificationEvent("Sell
							// order all matched for " +
							// orderStatus.getStockName()));
						}
					}

					orderStatus = orderStatusDao.updateOrderStatus(orderStatus);
					// end replace
					order = orderDao.getOrderById(order.getId());
					if (order.isDone()) {
						polling = false;
					}

					Thread.sleep(10000);

				} catch (BrokerInterfaceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("Error retrieving stock quote - " + e.getMessage());
					eventProcessor.onEvent(NotificationEventUtil
							.createNotificationEvent("Error retrieving stock quote - " + e.getMessage()));

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						throw new RuntimeException(e1);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}

			}
		} catch (RuntimeException re) {
			logger.error("Runtime exception occured in OrderStatusSimulator", re);
			eventProcessor.onEvent(NotificationEventUtil
					.createNotificationEvent("Runtime exception occured in OrderStatusSimulator" + re.getMessage()));
		}
	}

	private OrderStatus createOrderStatus(Order order) {
		OrderStatus orderStatus = new OrderStatus();
		orderStatus.setOrderDate(order.getOrderDate());
		orderStatus.setOrderStatusType(OrderStatusType.QUEUED);
		orderStatus.setOrderType(order.getOrderType());
		orderStatus.setPrice(order.getPrice());
		orderStatus.setQuantity(order.getQuantity());
		orderStatus.setMatchedQuantity(0);
		orderStatus.setStockName(order.getStockName());
		orderStatus.setBrokerOrderId(UUID.randomUUID().toString());
		return orderStatusDao.createOrderStatus(orderStatus);
	}

}
