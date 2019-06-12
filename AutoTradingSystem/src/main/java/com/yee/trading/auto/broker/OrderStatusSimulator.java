package com.yee.trading.auto.broker;

import com.yee.trading.auto.order.Order;

public interface OrderStatusSimulator extends Runnable {
	
	public void setOrder(Order order);
	
	

}
