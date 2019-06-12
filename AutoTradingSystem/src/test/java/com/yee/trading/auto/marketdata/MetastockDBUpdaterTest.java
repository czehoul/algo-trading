package com.yee.trading.auto.marketdata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class MetastockDBUpdaterTest {
	@Autowired
	private StockDBUpdater stockDBUpdater;
	
	@Test
	public void updateStockDB(){
		try {
			stockDBUpdater.updateDB();
		} catch (StockDBUpdaterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
