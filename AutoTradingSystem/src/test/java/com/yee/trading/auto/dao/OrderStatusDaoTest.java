package com.yee.trading.auto.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.order.OrderExecutionType;
import com.yee.trading.auto.order.OrderType;
import com.yee.trading.auto.stockinfo.OrderStatus;
import com.yee.trading.auto.stockinfo.OrderStatus.OrderStatusType;
import com.yee.trading.auto.strategy.Strategy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class OrderStatusDaoTest {
	
	@Autowired
	private StrategyDao strategyDao;
	@Autowired
	private OrderStatusDao orderStatusDao;
	
	@Transactional
	@Test
	@Rollback(false)
	public void createOrderStatusTest(){		
		OrderStatus orderStatus = new OrderStatus();
		orderStatus.setOrderDate(new Date());		
		orderStatus.setOrderType(OrderType.Buy);
		orderStatus.setPrice(new BigDecimal(0.935).setScale(3, RoundingMode.HALF_UP));
		orderStatus.setQuantity(35);
		orderStatus.setStockCode("0012");
		orderStatus.setStockName("GHLSYS");
		orderStatus.setMatchedQuantity(35);
		orderStatus.setOrderStatusType(OrderStatusType.PARTIALLY_MATCHED);
		orderStatus.setBrokerOrderId(UUID.randomUUID().toString());
		orderStatusDao.createOrderStatus(orderStatus);
	}
	
	@Transactional
	@Test
	@Rollback(false)
	public void updateOrderStatusTest(){
		Calendar fromCalc = Calendar.getInstance();
		fromCalc.set(Calendar.HOUR_OF_DAY, 6);
		fromCalc.set(Calendar.MINUTE, 54);
		Calendar toCalc = Calendar.getInstance();
		toCalc.set(Calendar.HOUR_OF_DAY, 6);
		toCalc.set(Calendar.MINUTE, 56);
		OrderStatus orderStatus = orderStatusDao.getOrderStatusByStockName("Hevea", fromCalc.getTime(), toCalc.getTime());
		orderStatus.setMatchedQuantity(95);
		orderStatusDao.updateOrderStatus(orderStatus);
	}
	
	@Transactional
	@Test
	public void getOrderStatusByStockNameTest(){
		Calendar fromCalc = Calendar.getInstance();
		fromCalc.set(Calendar.HOUR_OF_DAY, 6);
		fromCalc.set(Calendar.MINUTE, 54);
		Calendar toCalc = Calendar.getInstance();
		toCalc.set(Calendar.HOUR_OF_DAY, 6);
		toCalc.set(Calendar.MINUTE, 56);
		OrderStatus orderStatus = orderStatusDao.getOrderStatusByStockName("Hevea", fromCalc.getTime(), toCalc.getTime());
		System.out.println(orderStatus.getStockName());
	}
	
	@Transactional
	@Test
	public void getAllOrderStatusTest(){
		Calendar fromCalc = Calendar.getInstance();
		fromCalc.set(Calendar.HOUR_OF_DAY, 0);
		fromCalc.set(Calendar.MINUTE, 0);
		Calendar toCalc = Calendar.getInstance();
		toCalc.set(Calendar.HOUR_OF_DAY, 23);
		toCalc.set(Calendar.MINUTE, 59);
		List<OrderStatus> orderStatusList  = orderStatusDao.getAllOrderStatus(fromCalc.getTime(), toCalc.getTime());
		System.out.println("orderStatusList size = " + orderStatusList.size());
	}
	
	@Transactional
	@Test
	public void getOrderStatusByOrderTest(){
		Order order = new Order();
		order.setOrderDate(new Date());		
		order.setOrderType(OrderType.Buy);
		order.setPrice(new BigDecimal(1.2).setScale(3, RoundingMode.HALF_UP));
		order.setQuantity(88);
		order.setStockCode("5095");
		order.setStockName("Hevea");
		OrderStatus orderStatus = orderStatusDao.getOrderStatusByOrder(order);
		System.out.println("orderStatus = " + orderStatus.getStockName());
	}
}
