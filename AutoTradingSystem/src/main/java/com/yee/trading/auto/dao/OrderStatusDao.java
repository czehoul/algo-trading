package com.yee.trading.auto.dao;

import java.util.Date;
import java.util.List;

import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.stockinfo.OrderStatus;

public interface OrderStatusDao {
	

	public OrderStatus updateOrderStatus(OrderStatus orderStatus);
	
	public void deleteOrderStatus(OrderStatus orderStatus);
	
	public OrderStatus createOrderStatus(OrderStatus orderStatus);
	
	//public List<OrderStatus> getActiveOrderStatusByStrategy(int strategyId, Date fromDate, Date toDate);
	
	public OrderStatus getOrderStatusByStockName(String stockName, Date fromDate, Date toDate);
	
	public OrderStatus getOrderStatusByOrder(Order order);
	
	public OrderStatus getOrderStatusByBrokerOrderId(String brokerOrderId);
	
	public List<OrderStatus> getAllOrderStatus(Date fromDate, Date toDate);

}
