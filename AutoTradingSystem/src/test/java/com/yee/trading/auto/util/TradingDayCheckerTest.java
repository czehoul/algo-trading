package com.yee.trading.auto.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class TradingDayCheckerTest {
	
	@Autowired
	private TradingDayChecker tradingDayChecker;
	
	@Test
	public void getLastTradingDateTest(){
		System.out.println(tradingDayChecker.getLastTradingDate());
	}
	
	@Test
	public void getLastTradingPlusOneDateTest(){
		System.out.println(tradingDayChecker.getLastTradingPlusOneDate());
	}

}
