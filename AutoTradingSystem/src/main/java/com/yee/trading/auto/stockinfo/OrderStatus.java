package com.yee.trading.auto.stockinfo;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yee.trading.auto.broker.BrokerInterfaceException;
import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.order.OrderExecutionType;
import com.yee.trading.auto.order.OrderType;
import com.yee.trading.auto.portfolio.Portfolio;
import com.yee.trading.auto.strategy.Strategy;

@Entity
@Table(name="ORDERSTATUS")
public class OrderStatus {	
	@Enumerated(EnumType.STRING)
	@Column(name="OrderType")
	private OrderType orderType;	
	@Column(name="Price",precision=6, scale=3)
	private BigDecimal price;
	@Column(name="StockCode")
	private String stockCode;
	@Column(name="StockName")
	private String stockName;
	@Column(name="Quantity")
	private int quantity;	
	@Column(name="OrderDate")
	private Date orderDate;
	@Enumerated(EnumType.STRING)
	@Column(name="OrderStatusType")
	private OrderStatusType orderStatusType;
	@Column(name="MatchedQuantity")
	private int matchedQuantity;
	@Column(name="MatchedAmount",precision=6, scale=3)
	private BigDecimal matchedAmount;
	@Id
	@Column(name="BrokerOrderId")	
	private String brokerOrderId;
	@Transient
	private String entryDate;
	@Transient
	private String entryTime;

	public enum OrderStatusType {
		QUEUED, CANCELED, ALL_MATCHED, PARTIALLY_MATCHED, EXPIRED
	}

	public OrderStatusType getOrderStatusType() {
		return orderStatusType;
	}

	public void setOrderStatusType(OrderStatusType orderStatusType) {
		this.orderStatusType = orderStatusType;
	}

	public int getMatchedQuantity() {
		return matchedQuantity;
	}

	public void setMatchedQuantity(int matchedQuantity) {
		this.matchedQuantity = matchedQuantity;
	}

	public BigDecimal getMatchedAmount() {
		return matchedAmount;
	}

	public void setMatchedAmount(BigDecimal matchedAmount) {
		this.matchedAmount = matchedAmount;
	}

	public String getBrokerOrderId() {
		return brokerOrderId;
	}

	public void setBrokerOrderId(String brokerOrderId) {
		this.brokerOrderId = brokerOrderId;
	}	

	public OrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}

	
	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
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

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}	

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}	
	
	public String getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(String entryDate) {
		this.entryDate = entryDate;
	}

	public String getEntryTime() {
		return entryTime;
	}

	public void setEntryTime(String entryTime) {
		this.entryTime = entryTime;
	}	
	
	public void populateData() throws BrokerInterfaceException{		
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(
				"dd/M/yyyy HH:mm:ss");
		String dateTimeStr = this.entryDate + " "
				+ this.entryTime;
		try {
			orderDate = localSimpleDateFormat.parse(dateTimeStr);
			
		} catch (ParseException paramString) {
			throw new BrokerInterfaceException("Error parsing date time string " + dateTimeStr + " Error message: " + paramString.getMessage());
		}	
		
		if (this.getMatchedQuantity() == this.getQuantity()) {
			this.setOrderStatusType(OrderStatusType.ALL_MATCHED);
		} else if (this.getMatchedQuantity() > 0
				&& this.getMatchedQuantity() < this.getQuantity()) {
			this.setOrderStatusType(OrderStatusType.PARTIALLY_MATCHED);
		} else if (this.getMatchedQuantity() == 0) {
			this.setOrderStatusType(OrderStatusType.QUEUED);
		}
	}
	
	
	
}
