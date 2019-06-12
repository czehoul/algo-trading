package com.yee.trading.auto.util;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.strategy.Strategy;

public class OrderQueueContainer {
	private static OrderQueueContainer orderQueueContainer;
	private static List<Order> orderQueue;

	private OrderQueueContainer() {
		orderQueue = new LinkedList<Order>();
	}

	public static OrderQueueContainer getInstance() {
		if (orderQueueContainer == null) {
			orderQueueContainer = new OrderQueueContainer();
		}
		return orderQueueContainer;
	}

	public void enQueueOrder(Order order) {		
		orderQueue.add(order);
	}

	public Order deQueueOrder(int strategyId) {
		Order order = null;
		try {			
			for(Order aOrder : orderQueue) {
				if(aOrder.getStrategy().getId() == strategyId) {
					order = aOrder;
					break;
				}
			}
			if(order != null)
				orderQueue.remove(order);
		} catch (NoSuchElementException e) {
			order = null;
		}
		return order;
	}

	public void clearQueue() {
		orderQueue.clear();		
	}
	
	public static void main(String[] args) {
		Order order1 = new Order();
		order1.setStockName("abc");
		Order order2 = new Order();
		order2.setStockName("def");
		Strategy strag1 = new Strategy();
		strag1.setId(1);
		Strategy strag2 = new Strategy();
		strag2.setId(2);
		
		order1.setStrategy(strag1);
		order2.setStrategy(strag2);
		OrderQueueContainer.getInstance().enQueueOrder(order1);
		OrderQueueContainer.getInstance().enQueueOrder(order2);
		Order orderRest = OrderQueueContainer.getInstance().deQueueOrder(2);
		System.out.println(orderRest.getStockName());
	}
}
