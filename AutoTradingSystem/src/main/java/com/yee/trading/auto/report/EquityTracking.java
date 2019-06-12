package com.yee.trading.auto.report;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.yee.trading.auto.strategy.Strategy;

@Entity
@Table(name = "EQUITYTRACKING")
public class EquityTracking {
	
	@Id
	@Column(name="Id")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "eqIdGenerator")
	@SequenceGenerator(name = "eqIdGenerator", sequenceName = "EQUITYTRACKING_SEQ")	
	private int id;
	@ManyToOne(fetch = FetchType.LAZY)  
	@JoinColumn(name = "StrategyId")
	private Strategy strategy;
	@Column(name="CloseDate")
	private Date closeDate;
	@Column(name="Equity")
	private BigDecimal equity;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}	
	public Date getCloseDate() {
		return closeDate;
	}
	public void setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
	}
	public BigDecimal getEquity() {
		return equity;
	}
	public void setEquity(BigDecimal equity) {
		this.equity = equity;
	}
	public Strategy getStrategy() {
		return strategy;
	}
	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}
	
}
