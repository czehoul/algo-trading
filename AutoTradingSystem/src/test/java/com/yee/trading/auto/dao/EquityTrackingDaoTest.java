package com.yee.trading.auto.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.report.EquityTracking;
import com.yee.trading.auto.strategy.Strategy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class EquityTrackingDaoTest {
	
	@Autowired
	private EquityTrackingDao equityTrackingDao;
	@Autowired
	private StrategyDao strategyDao;
	
	@Test
	@Transactional
	@Rollback(false)
	public void createEquityTrackingTest(){
		Strategy strategy = strategyDao.loadStrategyById(1);
		EquityTracking equityTracking = new EquityTracking();
		equityTracking.setStrategy(strategy);
		equityTracking.setEquity(new BigDecimal(85500						).setScale(3, RoundingMode.HALF_UP));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 0);
		equityTracking.setCloseDate(cal.getTime());
		equityTrackingDao.createEquityTracking(equityTracking);
		
	}
	
	@Test
	@Transactional
	@Rollback(false)
	public void getEquityTrackingByStrategyTest(){
		
		Calendar calFrom = Calendar.getInstance();
		calFrom.add(Calendar.DAY_OF_MONTH, -10);
		Calendar calTo = Calendar.getInstance();
		calTo.add(Calendar.DAY_OF_MONTH, 3);
		List<EquityTracking> equityTrackingList = equityTrackingDao.getEquityTrackingByStrategy(1, calFrom.getTime(), calTo.getTime());
		System.out.println(equityTrackingList.size());
	}

}
