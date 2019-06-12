package com.yee.trading.auto.order;

//this is prototype bean
public interface OrderManager extends Runnable{
	
	
	/**
	 * Retrieve order from DB and submit to broker
	 */
	public void run();
	
	public Order getOrder();
	public void setOrder(Order order);

}
