package com.yee.trading.auto.stockinfo;

import java.math.BigDecimal;
import java.util.Map;

public class StockQuote {
	
	private String stokCode;
	private BigDecimal currentPrice;
	private BigDecimal previousClosePrice;
	private BigDecimal openPrice;
	private BigDecimal highPrice;
	private BigDecimal lowPrice;
	private int volume;	
	private Map<BigDecimal, Integer> buyQueues;
	private Map<BigDecimal, Integer> sellQueues;
	private BigDecimal valueTraded;
	
	public String getStokCode() {
		return stokCode;
	}
	public void setStokCode(String stokCode) {
		this.stokCode = stokCode;
	}
	public BigDecimal getCurrentPrice() {
		return currentPrice;
	}
	public void setCurrentPrice(BigDecimal currentPrice) {
		this.currentPrice = currentPrice;
	}
	public BigDecimal getPreviousClosePrice() {
		return previousClosePrice;
	}
	public void setPreviousClosePrice(BigDecimal previousClosePrice) {
		this.previousClosePrice = previousClosePrice;
	}
	public BigDecimal getOpenPrice() {
		return openPrice;
	}
	public void setOpenPrice(BigDecimal openPrice) {
		this.openPrice = openPrice;
	}
	public BigDecimal getHighPrice() {
		return highPrice;
	}
	public void setHighPrice(BigDecimal highPrice) {
		this.highPrice = highPrice;
	}
	public BigDecimal getLowPrice() {
		return lowPrice;
	}
	
	public BigDecimal getValueTraded() {
		return valueTraded;
	}
	public void setValueTraded(BigDecimal valueTraded) {
		this.valueTraded = valueTraded;
	}
	public void setLowPrice(BigDecimal lowPrice) {
		this.lowPrice = lowPrice;
	}
	public int getVolume() {
		return volume;
	}
	public void setVolume(int volume) {
		this.volume = volume;
	}
	public Map<BigDecimal, Integer> getBuyQueues() {
		return buyQueues;
	}
	public void setBuyQueues(Map<BigDecimal, Integer> buyQueues) {
		this.buyQueues = buyQueues;
	}
	public Map<BigDecimal, Integer> getSellQueues() {
		return sellQueues;
	}
	public void setSellQueues(Map<BigDecimal, Integer> sellQueues) {
		this.sellQueues = sellQueues;
	}
}
