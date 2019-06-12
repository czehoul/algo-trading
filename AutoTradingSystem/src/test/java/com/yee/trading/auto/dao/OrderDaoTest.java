package com.yee.trading.auto.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import com.yee.trading.auto.portfolio.Portfolio;
import com.yee.trading.auto.strategy.Strategy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class OrderDaoTest {
	
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private StrategyDao strategyDao;
	@Autowired
	private PortfolioDao portfolioDao;
	
	@Transactional
	@Test
	@Rollback(false)
	public void createOrderTest(){		
		Strategy strategy = strategyDao.loadStrategyById(new Integer(1));
		Portfolio portfolio = portfolioDao.getPortfolioById(221);
		Order order = new Order();
		order.setOrderDate(new Date());
		order.setOrderExecutionType(OrderExecutionType.OPEN);
		order.setOrderType(OrderType.Buy);
		order.setPrice(new BigDecimal(1.2).setScale(3, RoundingMode.HALF_UP));
		order.setQuantity(100);
		order.setStockCode("5095");
		order.setStockName("Hevea");
		order.setStrategy(strategy);
		order.setPortfolio(portfolio);
		order.setDone(true);
		order.setSubmitCount(2);
		
		orderDao.createOrder(order);
	}
	
	@Transactional
	@Test
	@Rollback(false)
	public void updateOrderTest(){
		Calendar fromCalc = Calendar.getInstance();
		fromCalc.set(Calendar.HOUR_OF_DAY, 0);
		fromCalc.set(Calendar.MINUTE, 0);
		Calendar toCalc = Calendar.getInstance();
		toCalc.set(Calendar.HOUR_OF_DAY, 23);
		toCalc.set(Calendar.MINUTE, 59);
		List<Order> orders = orderDao.getOrdersByStrategy(1, fromCalc.getTime(), toCalc.getTime());
		Order order = orders.get(0);
		order.setQuantity(90);
		orderDao.updateOrder(order);
	}
	
	@Transactional
	@Test
	@Rollback(false)
	public void deleteOrderTest(){
		Calendar fromCalc = Calendar.getInstance();
		fromCalc.set(Calendar.HOUR_OF_DAY, 0);
		fromCalc.set(Calendar.MINUTE, 0);
		Calendar toCalc = Calendar.getInstance();
		toCalc.set(Calendar.HOUR_OF_DAY, 23);
		toCalc.set(Calendar.MINUTE, 59);
		List<Order> orders = orderDao.getOrdersByStrategy(1, fromCalc.getTime(), toCalc.getTime());
		orderDao.deleteOrder(orders.get(0));
	}
		
	@Transactional
	@Test
	public void getOrdersByStrategyTest(){
		Calendar fromCalc = Calendar.getInstance();
		fromCalc.set(Calendar.HOUR_OF_DAY, 0);
		fromCalc.set(Calendar.MINUTE, 0);
		Calendar toCalc = Calendar.getInstance();
		toCalc.set(Calendar.HOUR_OF_DAY, 23);
		toCalc.set(Calendar.MINUTE, 59);
		List<Order> orders = orderDao.getOrdersByStrategy(1, fromCalc.getTime(), toCalc.getTime());
		System.out.println(orders.size());
		
	}
	
	@Test
	@Transactional
	public void getActiveOrderTest(){
		Calendar fromCalc = Calendar.getInstance();
		fromCalc.set(Calendar.HOUR_OF_DAY, 0);
		fromCalc.set(Calendar.MINUTE, 0);
		Calendar toCalc = Calendar.getInstance();
		toCalc.set(Calendar.HOUR_OF_DAY, 23);
		toCalc.set(Calendar.MINUTE, 59);
		List<Order> orders = orderDao.getActiveOrders(fromCalc.getTime(), toCalc.getTime());
		System.out.println("order size =" + orders.size());
		
	}

}
