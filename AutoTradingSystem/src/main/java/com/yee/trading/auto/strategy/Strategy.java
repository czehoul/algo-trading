package com.yee.trading.auto.strategy;

import java.math.BigDecimal;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.yee.trading.auto.order.OrderExecutionType;
import com.yee.trading.auto.portfolio.Portfolio;
import com.yee.trading.auto.report.EquityTracking;

@Entity
@Table(name="Strategy")
public class Strategy {
	@Id
	@Column(name="Id")
	private int id;
	@Column(name="Name")
	private String name;
	@Column(name="MaxPortfolioNumber")
	private int maxPortfolioNumber;
	@Column(name="InitialEquity", precision=9, scale=2)
	private BigDecimal initialEquity;	
	@Column(name="TotalEquity", precision=9, scale=2)
	private BigDecimal totalEquity; //equity=cash+holding
	@Column(name="AvailableCash", precision=9, scale=2)
	private BigDecimal availableCash;
	@Column(name="AvailablePosition")
	private int availablePosition;
	//private String portfolioWeight;
	@Enumerated(EnumType.STRING)
	@Column(name="BuyOrderExecutionType")
	private OrderExecutionType buyOrderExecutionType;
	@Enumerated(EnumType.STRING)
	@Column(name="SellOrderExecutionType")
	private OrderExecutionType sellOrderExecutionType;
	@Column(name="BuyDelay")
	private int buyDelay;
	@Column(name="SellDelay")
	private int sellDelay;
	@Type(type="yes_no")
	@Column(name="Live")
	private boolean live;
	@OneToMany(cascade=CascadeType.ALL, fetch = FetchType.LAZY, mappedBy="strategy" ) 
	private Set<Portfolio> portfolios;
	@OneToMany(cascade=CascadeType.ALL, fetch = FetchType.LAZY, mappedBy="strategy" ) 
	private Set<EquityTracking> equityTrackings;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getMaxPortfolioNumber() {
		return maxPortfolioNumber;
	}
	public void setMaxPortfolioNumber(int maxPortfolioNumber) {
		this.maxPortfolioNumber = maxPortfolioNumber;
	}	
	public int getBuyDelay() {
		return buyDelay;
	}
	public void setBuyDelay(int buyDelay) {
		this.buyDelay = buyDelay;
	}
	public int getSellDelay() {
		return sellDelay;
	}
	public void setSellDelay(int sellDelay) {
		this.sellDelay = sellDelay;
	}
	public OrderExecutionType getBuyOrderExecutionType() {
		return buyOrderExecutionType;
	}
	public void setBuyOrderExecutionType(OrderExecutionType buyOrderExecutionType) {
		this.buyOrderExecutionType = buyOrderExecutionType;
	}
	public OrderExecutionType getSellOrderExecutionType() {
		return sellOrderExecutionType;
	}
	public void setSellOrderExecutionType(OrderExecutionType sellOrderExecutionType) {
		this.sellOrderExecutionType = sellOrderExecutionType;
	}
	public BigDecimal getInitialEquity() {
		return initialEquity;
	}
	public void setInitialEquity(BigDecimal initialEquity) {
		this.initialEquity = initialEquity;
	}
	public BigDecimal getTotalEquity() {
		return totalEquity;
	}
	public void setTotalEquity(BigDecimal totalEquity) {
		this.totalEquity = totalEquity;
	}
	public Set<Portfolio> getPortfolios() {
		return portfolios;
	}
	public void setPortfolios(Set<Portfolio> portfolios) {
		this.portfolios = portfolios;
	}
	public BigDecimal getAvailableCash() {
		return availableCash;
	}
	public void setAvailableCash(BigDecimal availableCash) {
		this.availableCash = availableCash;
	}
	public int getAvailablePosition() {
		return availablePosition;
	}
	public void setAvailablePosition(int availablePosition) {
		this.availablePosition = availablePosition;
	}
	public Set<EquityTracking> getEquityTrackings() {
		return equityTrackings;
	}
	public void setEquityTrackings(Set<EquityTracking> equityTrackings) {
		this.equityTrackings = equityTrackings;
	}
	public boolean isLive() {
		return live;
	}
	public void setLive(boolean live) {
		this.live = live;
	}
	
	
}
