package com.yee.trading.auto.event;


import com.yee.trading.auto.stockinfo.OrderStatus;

public class OrderStatusEvent extends Event {
	
	public OrderStatusEvent(EventType eventType) {
		super(eventType);
	}

	private OrderStatus orderStatus;

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}
	
	
}
