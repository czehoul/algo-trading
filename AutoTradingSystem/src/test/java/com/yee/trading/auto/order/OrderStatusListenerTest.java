package com.yee.trading.auto.order;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.yee.trading.auto.dao.OrderDao;
import com.yee.trading.auto.dao.OrderStatusDao;
import com.yee.trading.auto.stockinfo.OrderStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class OrderStatusListenerTest {
	
	@Autowired
	private OrderStatusListener orderStatusListener;
	
	@Autowired
	private OrderStatusDao orderStatusDao;
	
	@Autowired
	private OrderDao orderDao;
	
	@Test
	public void orderStatusUpdatedTest(){
		OrderStatus orderStatus = orderStatusDao.getOrderStatusByBrokerOrderId("654881bf-4a5e-4eed-b301-d90639d796ca");
		orderStatusListener.orderStatusUpdated(orderStatus);
	}

}
