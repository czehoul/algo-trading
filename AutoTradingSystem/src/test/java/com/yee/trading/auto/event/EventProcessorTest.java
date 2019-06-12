package com.yee.trading.auto.event;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.order.OrderExecutionType;
import com.yee.trading.auto.order.OrderType;
import com.yee.trading.auto.strategy.Strategy;
import com.yee.trading.auto.util.NotificationEventUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class EventProcessorTest {
	@Autowired
	private EventProcessor eventProcessor;
	
	@Autowired
	@Qualifier("autoTradingTaskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	
	@Test
	public void onOrderEventTest(){
		OrderEvent event = new OrderEvent(EventType.ORDER_GENERATED);
		Order order = new Order();
		//order.setOrderExecutionType(OrderExecutionType.MARKET);
		Strategy strategy = new Strategy();
		strategy.setId(1);
		order.setStrategy(strategy);
		order.setOrderType(OrderType.Buy);
		order.setStockCode("5095");
		order.setStockName("HEVEA");	
		order.setOrderExecutionType(OrderExecutionType.LIMIT);
		order.setPrice(new BigDecimal(1.00));
		event.setOrder(order);
		eventProcessor.onEvent(event);
		//check active thread, if zero then shut down the thread pool
		for (;;) {
			int count = taskExecutor.getActiveCount();
			System.out.println("Active Threads : " + count);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//			if (count == 0) {
//				taskExecutor.shutdown();
//				break;
//			}
		}
	}
	
	
	@Test
	public void onNewTickEventTest(){
		
		eventProcessor.onEvent(new Event(EventType.NEW_TICK));
		//check active thread, if zero then shut down the thread pool
		for (;;) {
			int count = taskExecutor.getActiveCount();
			System.out.println("Active Threads : " + count);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (count == 0) {
				taskExecutor.shutdown();
				break;
			}
		}
	}
	
	@Test
	public void onNotificationEvent(){
		StringBuilder notificationMsg = new StringBuilder();
		List<String> buySignals = Arrays.asList("TST1", "TST2", "TST3");
		List<String> sellSignals = Arrays.asList("TST12", "TST22", "TST32");
		notificationMsg = notificationMsg.append("Strategy runner for strategy ").append("IRICH")
				.append(" completed successfully. ").append("Buy signals=" + buySignals)
				.append(". ").append("Sell signals=" + sellSignals);
		eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(notificationMsg.toString()));
		eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent("t2"));
		eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent("t3"));
		eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent("t4"));
		eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent("t5"));
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
