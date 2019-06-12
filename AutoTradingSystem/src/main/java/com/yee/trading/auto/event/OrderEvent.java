package com.yee.trading.auto.event;

import com.yee.trading.auto.order.Order;

public class OrderEvent extends Event {
	
	public OrderEvent(EventType eventType) {
		super(eventType);
	}

	private Order order;

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
	
	
}
