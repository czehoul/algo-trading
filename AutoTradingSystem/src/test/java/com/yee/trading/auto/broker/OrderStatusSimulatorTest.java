package com.yee.trading.auto.broker;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.yee.trading.auto.dao.OrderDao;
import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.util.SpringContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class OrderStatusSimulatorTest {
	
	@Autowired
	@Qualifier("autoTradingTaskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	@Autowired
	private SpringContext springContext;
	@Autowired
	private OrderDao orderDao;
	
	@Test
	public void runOrderStatusSimulatorTest(){
		Order order = orderDao.getOrderById(145);
		
		OrderStatusSimulator orderStatusSimulator = (OrderStatusSimulator)springContext.getApplicationContext().getBean("orderStatusSimulator");
		orderStatusSimulator.setOrder(order);
		taskExecutor.execute(orderStatusSimulator);
		while(true){
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
