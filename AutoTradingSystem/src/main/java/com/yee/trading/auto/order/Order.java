package com.yee.trading.auto.order;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.yee.trading.auto.portfolio.Portfolio;
import com.yee.trading.auto.strategy.Strategy;

@Entity
@Table(name = "ORDERTRANSACTION")
public class Order implements Cloneable{
	@Id
	@Column(name="Id")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "orderIdGenerator")
	@SequenceGenerator(name = "orderIdGenerator", sequenceName = "ORDER_SEQ")	
	private int id;
	@Enumerated(EnumType.STRING)
	@Column(name="OrderType")
	private OrderType orderType;
	@Enumerated(EnumType.STRING)
	@Column(name="OrderExecutionType")
	private OrderExecutionType orderExecutionType;
	@Column(name="Price",precision=6, scale=3)
	private BigDecimal price;
	@Column(name="StockCode")
	private String stockCode;
	@Column(name="StockName")
	private String stockName;
	@Column(name="Quantity")
	private int quantity;
	@ManyToOne(fetch = FetchType.LAZY)  
	@JoinColumn(name = "StrategyId")
	private Strategy strategy;
	@ManyToOne(fetch = FetchType.LAZY)  
	@JoinColumn(name = "PortfolioId")
	private Portfolio portfolio;
	@Column(name="OrderDate")
	private Date orderDate;
	@Type(type="yes_no")
	@Column(name="Done")
	private boolean done;
	@Column(name="SubmitCount")
	private int submitCount;
	@Column(name="OriginalQuantity")
	private int originalQuantity;
	@Transient
	private BigDecimal amount;
	@Transient
	private boolean live;
	
	public OrderType getOrderType() {
		return orderType;
	}
	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}
	public OrderExecutionType getOrderExecutionType() {
		return orderExecutionType;
	}
	public void setOrderExecutionType(OrderExecutionType orderExecutionType) {
		this.orderExecutionType = orderExecutionType;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
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
	
	public Date getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public Strategy getStrategy() {
		return strategy;
	}
	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}	
	public Portfolio getPortfolio() {
		return portfolio;
	}
	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}	
	public boolean isDone() {
		return done;
	}
	public void setDone(boolean done) {
		this.done = done;
	}
	
	public int getSubmitCount() {
		return submitCount;
	}
	public void setSubmitCount(int submitCount) {
		this.submitCount = submitCount;
	}
	
	public BigDecimal getAmount() {
		if(amount == null) 
			amount = price.multiply(new BigDecimal(quantity*100));
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}	
	
	public boolean isLive() {
		return live;
	}
	public void setLive(boolean live) {
		this.live = live;
	}
	public int getOriginalQuantity() {
		return originalQuantity;
	}
	public void setOriginalQuantity(int originalQuantity) {
		this.originalQuantity = originalQuantity;
	}
	public String toString(){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder = stringBuilder.append("OrderInfo=>")
			.append("OrderType=").append(orderType).append(",")
			.append("OrderExecutionType=").append(orderExecutionType).append(",")
			.append("Price=").append(price).append(",")
			.append("StockCode=").append(stockCode).append(",")
			.append("StockName=").append(stockName).append(",")
			.append("Quantity=").append(quantity).append(",")
			.append("StrategyId=").append(strategy.getId()).append(",")
			.append("SubmitCount=").append(this.submitCount);
		return stringBuilder.toString();
			
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Order order = new Order();
		order.setOrderExecutionType(this.orderExecutionType);
		order.setOrderType(this.orderType);
		order.setStockCode(this.stockCode);
		order.setStockName(this.stockName);
		order.setPortfolio(this.portfolio);
		order.setStrategy(this.strategy);	
		order.setSubmitCount(this.submitCount);
		return order;
	}
}
