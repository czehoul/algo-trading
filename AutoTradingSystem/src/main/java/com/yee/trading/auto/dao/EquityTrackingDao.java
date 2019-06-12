package com.yee.trading.auto.dao;

import java.util.Date;
import java.util.List;

import com.yee.trading.auto.report.EquityTracking;

public interface EquityTrackingDao {
	
	public void createEquityTracking(EquityTracking equityTracking);
	
	public List<EquityTracking> getEquityTrackingByStrategy(int strategyId, Date fromDate, Date toDate);

}
