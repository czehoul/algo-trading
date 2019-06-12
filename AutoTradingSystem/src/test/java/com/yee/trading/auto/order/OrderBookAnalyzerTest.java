package com.yee.trading.auto.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class OrderBookAnalyzerTest {
	@Autowired
	private OrderBookAnalyzer orderBookAnalyzer;
	
	@Test
	public void analyzeTest(){
//		StockQuote stockQuote = new StockQuote();
//		stockQuote.setStokCode("5095");
		Map<BigDecimal, Integer> buyQueues = new LinkedHashMap<>();
		buyQueues.put(new BigDecimal(1.2).setScale(3, RoundingMode.HALF_UP), 20);
		buyQueues.put(new BigDecimal(1.19).setScale(3, RoundingMode.HALF_UP), 100);
		buyQueues.put(new BigDecimal(1.18).setScale(3, RoundingMode.HALF_UP), 300);
		//stockQuote.setBuyQueues(buyQueues);
		Map<BigDecimal, Integer> sellQueues = new LinkedHashMap<>();
		sellQueues.put(new BigDecimal(1.21).setScale(3, RoundingMode.HALF_UP), 100);
		sellQueues.put(new BigDecimal(1.22).setScale(3,RoundingMode.HALF_UP), 100);
		sellQueues.put(new BigDecimal(1.23).setScale(3, RoundingMode.HALF_UP), 300);
		//stockQuote.setSellQueues(sellQueues);
		OrderBook orderBook = orderBookAnalyzer.analyze(buyQueues, sellQueues);
		System.out.println(orderBook.getHighestBuyQuantity());
	}

}
