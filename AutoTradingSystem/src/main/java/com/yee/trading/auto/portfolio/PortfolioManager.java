package com.yee.trading.auto.portfolio;

import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.stockinfo.OrderStatus;

public interface PortfolioManager {

	public Order createPortfolio(Order order) throws PortfolioManagerException;
	
	public Order recreateOrder(Order previousOrder, OrderStatus previousOrderStatus, Order newOrder) throws PortfolioManagerException;

	public Order createOrder(Order newOrder);
	
	// public void createPortfolio(int strategyId, int quantity, BigDecimal
	// buyPrice);

	//public Portfolio markPortfolioAsSell(Order order);
	//public int getShareQuantity(Order order);
	
	//public int removePortfolio(Order order);
	
	public void updatePortfolioByOrderStatus();
	
	public void updatePortfolioByOrderStatus(OrderStatus orderStatus);
	
	public void reduceOrder(Order order, int quantity);

}
