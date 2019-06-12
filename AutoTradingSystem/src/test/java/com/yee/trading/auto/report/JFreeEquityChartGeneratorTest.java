package com.yee.trading.auto.report;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class JFreeEquityChartGeneratorTest {

	@Autowired
	private ChartGenerator chartGenerator;
	
	@Test
	@Transactional
	public void generateEquityChartTest(){
		try {
			chartGenerator.generateEquityChart();
		} catch (ChartGeneratorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
