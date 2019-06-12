package com.yee.trading.auto.broker;

import java.math.BigDecimal;
import java.util.List;

import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.stockinfo.OrderStatus;
import com.yee.trading.auto.stockinfo.StockQuote;

public interface BrokerInterfaceManager {
	
	public String searchStockCode(String stockName) throws BrokerInterfaceException;
	
	public void submitOrder(Order order) throws BrokerInterfaceException;
	
	//public List<OrderStatus> queryOrdersStatus()throws BrokerInterfaceException;
	
	public List<OrderStatus> querySummaryOrdersStatus()throws BrokerInterfaceException; 
	
	public OrderStatus queryOrdersStatus(Order order)throws BrokerInterfaceException;
	
	public StockQuote queryStockQuote(String stockCode)throws BrokerInterfaceException; 
	
	public void reduceOrder(String brokerOrderId, BigDecimal price, int quatityToReduce) throws BrokerInterfaceException; 

}
