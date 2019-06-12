package com.yee.trading.auto.portfolio;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.strategy.Strategy;

@Entity
@Table(name="Portfolio")
public class Portfolio {
	
	@Id
	@Column(name="Id")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "portfolioIdGenerator")
	@SequenceGenerator(name = "portfolioIdGenerator", sequenceName = "PORTFOLIO_SEQ")	
	private int id;
	@Column(name="StockCode")
	private String stockCode;
	@Column(name="StockName")
	private String stockName;
	@Column(name="BuyPrice",precision=6, scale=3)
	private BigDecimal buyPrice;
	@Column(name="SellPrice",precision=6, scale=3)
	private BigDecimal sellPrice;
	@Column(name="LastClosePrice",precision=6, scale=3)
	private BigDecimal lastClosePrice;
	@Column(name="Quantity")
	private int quantity;
	//TODO Suppose need to have another column for cost
	//equity = total amount - cost
	@Column(name="TotalAmount",precision=9, scale=2)
	private BigDecimal totalAmount;	
	@Column(name="TotalAmountIncCost",precision=9, scale=2)
	private BigDecimal totalAmountIncCost;
	@Column(name="AllocatedCash",precision=9, scale=2)
	private BigDecimal allocatedCash;
	@Column(name="BuyDate")
	private Date buyDate;
	@Column(name="SellDate")
	private Date sellDate;
	@Type(type="yes_no")
	@Column(name="Hold")
	private boolean hold;
	@ManyToOne(fetch = FetchType.LAZY)  
	@JoinColumn(name = "StrategyId")
	private Strategy strategy;
	@OneToMany(cascade=CascadeType.PERSIST, fetch = FetchType.LAZY, mappedBy="portfolio" ) 
	private Set<Order> orders;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getStockCode() {
		return stockCode;
	}
	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}
	public String getStockName() {
		return stockName;
	}
	public void setStockName(String stockName) {
		this.stockName = stockName;
	}
	public BigDecimal getBuyPrice() {
		return buyPrice;
	}
	public void setBuyPrice(BigDecimal buyPrice) {
		this.buyPrice = buyPrice;
	}
	public BigDecimal getSellPrice() {
		return sellPrice;
	}
	public void setSellPrice(BigDecimal sellPrice) {
		this.sellPrice = sellPrice;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}
	public Date getBuyDate() {
		return buyDate;
	}
	public void setBuyDate(Date buyDate) {
		this.buyDate = buyDate;
	}
	public Date getSellDate() {
		return sellDate;
	}
	public void setSellDate(Date sellDate) {
		this.sellDate = sellDate;
	}
	public boolean isHold() {
		return hold;
	}
	public void setHold(boolean hold) {
		this.hold = hold;
	}
	public Strategy getStrategy() {
		return strategy;
	}
	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}
	public BigDecimal getTotalAmountIncCost() {
		return totalAmountIncCost;
	}
	public void setTotalAmountIncCost(BigDecimal totalAmountIncCost) {
		this.totalAmountIncCost = totalAmountIncCost;
	}
	public BigDecimal getAllocatedCash() {
		return allocatedCash;
	}
	public void setAllocatedCash(BigDecimal allocatedCash) {
		this.allocatedCash = allocatedCash;
	}
	public Set<Order> getOrders() {
		return orders;
	}
	public void setOrders(Set<Order> orders) {
		this.orders = orders;
	}
	public BigDecimal getLastClosePrice() {
		return lastClosePrice;
	}
	public void setLastClosePrice(BigDecimal lastClosePrice) {
		this.lastClosePrice = lastClosePrice;
	}
	
}
