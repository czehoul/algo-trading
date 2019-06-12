package com.yee.trading.auto.strategy;

import java.math.BigDecimal;

public class IntradayOpenSignal {
	private String stockName;
	private String stockCode;
	private BigDecimal previousClosePrice;
	private int marketBullLevel;
	private Strategy strategy;
	
	public String getStockName() {
		return stockName;
	}
	public void setStockName(String stockName) {
		this.stockName = stockName;
	}
	public BigDecimal getPreviousClosePrice() {
		return previousClosePrice;
	}
	public void setPreviousClosePrice(BigDecimal previousClosePrice) {
		this.previousClosePrice = previousClosePrice;
	}
	public int getMarketBullLevel() {
		return marketBullLevel;
	}
	public void setMarketBullLevel(int marketBullLevel) {
		this.marketBullLevel = marketBullLevel;
	}
	public Strategy getStrategy() {
		return strategy;
	}
	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}
	public String getStockCode() {
		return stockCode;
	}
	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}
	
	

}
