package com.yee.trading.auto.dao;

import java.util.Date;
import java.util.List;

import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.stockinfo.OrderStatus;

public interface OrderDao {

	public void updateOrder(Order order);
	
	public void deleteOrder(Order order);
	
	public Order createOrder(Order order);
	
	public Order getOrderById(int id);
	
	public Order getOrderByOrderStatus(OrderStatus orderStatus);
	
	public List<Order> getOrdersByStrategy(int strategyId, Date fromDate, Date toDate);
	
	public List<Order> getActiveOrders(Date fromDate, Date toDate);
	
}
