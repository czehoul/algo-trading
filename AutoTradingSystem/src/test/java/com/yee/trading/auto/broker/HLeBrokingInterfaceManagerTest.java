package com.yee.trading.auto.broker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.order.OrderType;
import com.yee.trading.auto.stockinfo.OrderStatus;
import com.yee.trading.auto.stockinfo.StockQuote;
import com.yee.trading.auto.strategy.Strategy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class HLeBrokingInterfaceManagerTest {

	@Autowired
	@Qualifier("hLeBrokingInterfaceManager")
	private BrokerInterfaceManager brokerInterfaceManager;
	
	@Test
	public void searchStockCodeTest(){
		try {
			String code = brokerInterfaceManager.searchStockCode("L&G");
			System.out.println("Code = " + code);
		} catch (BrokerInterfaceException e) {
			
			e.printStackTrace();
		}
	}
	
	@Test
	public void queryStockQuoteTest(){
		try {
			StockQuote stockQuote = brokerInterfaceManager.queryStockQuote("5095");
			System.out.println("StockQuote = " + stockQuote.getValueTraded());
		} catch (BrokerInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void submitOrderTest(){
		Order order = new Order();
		order.setStockCode("0021");
		order.setStockName("GHLSYS");
		Strategy strategy = new Strategy();
		strategy.setId(1);
		order.setStrategy(strategy);
		order.setLive(true);
		order.setQuantity(1);
		order.setPrice(new BigDecimal(0.800).setScale(3, RoundingMode.HALF_UP));
		order.setOrderType(OrderType.Buy);
		try {
			brokerInterfaceManager.submitOrder(order);
		} catch (BrokerInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void queryStockQuoteDelayTest(){
		try {
			StockQuote stockQuote = brokerInterfaceManager.queryStockQuote("0021");
			System.out.println("StockQuote = " + stockQuote.getCurrentPrice());
			try {
				Thread.sleep(20*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			StockQuote stockQuote1 = brokerInterfaceManager.queryStockQuote("0021");
			System.out.println("StockQuote = " + stockQuote1.getCurrentPrice());
		} catch (BrokerInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void querySingleOrderStatusTest(){
		Order order = new Order();
		order.setStockCode("5218");
		order.setStockName("SKPETRO");
		order.setPrice(new BigDecimal("1.55").setScale(3, RoundingMode.HALF_UP));
		order.setOrderType(OrderType.Sell);
		order.setQuantity(66);
		Calendar startTime = Calendar.getInstance();
		
		//startTime.setTime(order.getOrderDate());
		startTime.set(Calendar.HOUR_OF_DAY, 12);
		startTime.set(Calendar.MINUTE, 21);
		
		order.setOrderDate(startTime.getTime());
		try {
			OrderStatus status = brokerInterfaceManager.queryOrdersStatus(order);
			System.out.println(status.getStockName());
		} catch (BrokerInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void submitReduceOrderTest(){		
		try {
			List<OrderStatus> orderStatusList = brokerInterfaceManager.querySummaryOrdersStatus();
			OrderStatus orderStatus = orderStatusList.get(0);
			brokerInterfaceManager.reduceOrder(orderStatus.getBrokerOrderId(), orderStatus.getPrice(), orderStatus.getQuantity());
		} catch (BrokerInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void querySummaryOrdersStatusTest(){
		try {
			List<OrderStatus> orderStatusList = brokerInterfaceManager.querySummaryOrdersStatus();
			System.out.println(orderStatusList.size());
		} catch (BrokerInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
