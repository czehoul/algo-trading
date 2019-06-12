package com.yee.trading.auto.marketdata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class BizFunDataUpdaterTest {
	@Autowired
	private BizFunDataUpdater bizFunDataUpdater;
	
	@Test
	public void updateDataTest(){
		bizFunDataUpdater.updateData();
	}
	

}
