package com.yee.trading.auto.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

@Component("orderBookAnalyzer")
public class OrderBookAnalyzer {
	public static final BigDecimal one = new BigDecimal(1);
	public static final BigDecimal largeSpread = new BigDecimal(0.01);
	public static final BigDecimal smallSpread = new BigDecimal(0.005);

	public OrderBook analyze(Map<BigDecimal, Integer> buyQueues, Map<BigDecimal, Integer> sellQueues){
		int buyQueueCounter = 0;
		int sellQueueCounter = 0;
		int buyQueueTotalQuantity = 0;
		int sellQueueTotalQuantity = 0;
		OrderBook orderBook = new OrderBook();
		
		for (Entry<BigDecimal, Integer> queue : buyQueues.entrySet()) {
			buyQueueCounter++;
			if(buyQueueCounter == 1){
				orderBook.setHighestBuyPrice(queue.getKey());
				orderBook.setHighestBuyQuantity(queue.getValue());				
			}
			if(buyQueueCounter == 2){
				orderBook.setSecondHighestBuyPrice(queue.getKey());
				orderBook.setSecondHighestBuyQuantity(queue.getValue());				
			}
			buyQueueTotalQuantity = buyQueueTotalQuantity + queue.getValue();				
		}
		orderBook.setBuyQueueTotalQuantity(buyQueueTotalQuantity);
		for (Entry<BigDecimal, Integer> queue : sellQueues.entrySet()) {
			sellQueueCounter++;
			if(sellQueueCounter == 1){
				orderBook.setLowestSellPrice(queue.getKey());
				orderBook.setLowestSellQuantity(queue.getValue());
			}
			if(sellQueueCounter == 2){
				orderBook.setSecondLowestSellPrice(queue.getKey());
				orderBook.setSecondLowestSellQuantity(queue.getValue());				
			}
			
			sellQueueTotalQuantity = sellQueueTotalQuantity + queue.getValue();				
		}
		orderBook.setSellQueueTotalQuantity(sellQueueTotalQuantity);
		//BigDecimal one = new BigDecimal(1);
		BigDecimal priceDiff = orderBook.getLowestSellPrice().subtract(orderBook.getHighestBuyPrice());
		BigDecimal divider = null;
		if(orderBook.getLowestSellPrice().compareTo(one) > 0 && orderBook.getHighestBuyPrice().compareTo(one) > 0){
			divider = largeSpread.setScale(3, RoundingMode.HALF_UP);			
		} else {
			divider = smallSpread.setScale(3, RoundingMode.HALF_UP);
		}
		orderBook.setBipsValue(divider);
		orderBook.setSpread(priceDiff.divide(divider,0, RoundingMode.HALF_UP).intValue());
		return orderBook; 
	}
}
