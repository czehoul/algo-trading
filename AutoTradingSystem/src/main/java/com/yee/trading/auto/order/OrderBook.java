package com.yee.trading.auto.order;

import java.math.BigDecimal;

public class OrderBook {

	private int buyQueueTotalQuantity;	
	private int sellQueueTotalQuantity;	
	private BigDecimal highestBuyPrice;	
	private int highestBuyQuantity;
	private BigDecimal secondHighestBuyPrice;	
	private int secondHighestBuyQuantity;
	private BigDecimal lowestSellPrice;
	private int lowestSellQuantity;
	private BigDecimal secondLowestSellPrice;
	private int secondLowestSellQuantity;
	//no of bips different between nearest buy/sell queue
	private int spread;
        private BigDecimal bipsValue;

	public int getBuyQueueTotalQuantity() {
		return buyQueueTotalQuantity;
	}
	public void setBuyQueueTotalQuantity(int buyQueueTotalQuantity) {
		this.buyQueueTotalQuantity = buyQueueTotalQuantity;
	}
	public int getSellQueueTotalQuantity() {
		return sellQueueTotalQuantity;
	}
	public void setSellQueueTotalQuantity(int sellQueueTotalQuantity) {
		this.sellQueueTotalQuantity = sellQueueTotalQuantity;
	}
	public BigDecimal getHighestBuyPrice() {
		return highestBuyPrice;
	}
	public void setHighestBuyPrice(BigDecimal highestBuyPrice) {
		this.highestBuyPrice = highestBuyPrice;
	}
	public int getHighestBuyQuantity() {
		return highestBuyQuantity;
	}
	public void setHighestBuyQuantity(int highestBuyQuantity) {
		this.highestBuyQuantity = highestBuyQuantity;
	}
	public BigDecimal getLowestSellPrice() {
		return lowestSellPrice;
	}
	public void setLowestSellPrice(BigDecimal lowestSellPrice) {
		this.lowestSellPrice = lowestSellPrice;
	}
	public int getLowestSellQuantity() {
		return lowestSellQuantity;
	}
	public void setLowestSellQuantity(int lowestSellQuantity) {
		this.lowestSellQuantity = lowestSellQuantity;
	}
	public int getSpread() {
		return spread;
	}
	public void setSpread(int spread) {
		this.spread = spread;
	}
	public BigDecimal getBipsValue() {
		return bipsValue;
	}
	public void setBipsValue(BigDecimal bipsValue) {
		this.bipsValue = bipsValue;
	}
	public BigDecimal getSecondHighestBuyPrice() {
		return secondHighestBuyPrice;
	}
	public void setSecondHighestBuyPrice(BigDecimal secondHighestBuyPrice) {
		this.secondHighestBuyPrice = secondHighestBuyPrice;
	}
	public int getSecondHighestBuyQuantity() {
		return secondHighestBuyQuantity;
	}
	public void setSecondHighestBuyQuantity(int secondHighestBuyQuantity) {
		this.secondHighestBuyQuantity = secondHighestBuyQuantity;
	}
	public BigDecimal getSecondLowestSellPrice() {
		return secondLowestSellPrice;
	}
	public void setSecondLowestSellPrice(BigDecimal secondLowestSellPrice) {
		this.secondLowestSellPrice = secondLowestSellPrice;
	}
	public int getSecondLowestSellQuantity() {
		return secondLowestSellQuantity;
	}
	public void setSecondLowestSellQuantity(int secondLowestSellQuantity) {
		this.secondLowestSellQuantity = secondLowestSellQuantity;
	}
	
	
	
}
