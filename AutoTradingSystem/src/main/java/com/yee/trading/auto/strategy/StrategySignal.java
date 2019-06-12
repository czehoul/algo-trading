package com.yee.trading.auto.strategy;

import java.math.BigDecimal;

import com.yee.trading.auto.order.OrderExecutionType;
import com.yee.trading.auto.order.OrderType;

public class StrategySignal {
	private String stockName;
	private String stockCode;
	private OrderExecutionType orderExecutionType;//if this is set then override strategy order exe type
	private OrderType orderType;
	private BigDecimal closePrice;
	
	public String getStockName() {
		return stockName;
	}
	public void setStockName(String stockName) {
		this.stockName = stockName;
	}
	public String getStockCode() {
		return stockCode;
	}
	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}
	public OrderExecutionType getOrderExecutionType() {
		return orderExecutionType;
	}
	public void setOrderExecutionType(OrderExecutionType orderExecutionType) {
		this.orderExecutionType = orderExecutionType;
	}
	public OrderType getOrderType() {
		return orderType;
	}
	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}
	public BigDecimal getClosePrice() {
		return closePrice;
	}
	public void setClosePrice(BigDecimal closePrice) {
		this.closePrice = closePrice;
	}
	
	
	
}
