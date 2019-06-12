package com.yee.trading.auto.portfolio;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.dao.OrderDao;
import com.yee.trading.auto.dao.OrderStatusDao;
import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.order.OrderExecutionType;
import com.yee.trading.auto.order.OrderType;
import com.yee.trading.auto.stockinfo.OrderStatus;
import com.yee.trading.auto.strategy.Strategy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class KLSEPortfolioManagerTest {

	@Autowired
	private PortfolioManager portfolioManager;
	@Autowired
	private OrderStatusDao orderStatusDao;
	@Autowired
	private OrderDao orderDao;
	@Autowired
	@Qualifier("autoTradingTaskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	
	//use this for manual portfolio creation
	@Test
	@Transactional
	@Rollback(false)
	public void createPortfolioTest(){
		Order order = new Order();
		order.setStockCode("7204");
		order.setStockName("D&O");
		Strategy strategy = new Strategy();
		strategy.setId(1);
		order.setStrategy(strategy);
		order.setQuantity(117);
		order.setPrice(new BigDecimal(0.81));
		order.setOrderType(OrderType.Buy);
		order.setOrderExecutionType(OrderExecutionType.OPEN);
		order.setOrderDate(new Date());
		order.setDone(true);
		
		try {
			portfolioManager.createPortfolio(order);
		} catch (PortfolioManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	@Rollback(false)
	public void updatePortfolioByOrderStatusTest(){
		portfolioManager.updatePortfolioByOrderStatus();
		
	}
	
	@Test
	@Transactional
	@Rollback(false)
	public void createOrderTest(){
		Order order = new Order();
		order.setStockCode("7277");
		order.setStockName("DIALOG");
		Strategy strategy = new Strategy();
		strategy.setId(1);
		order.setStrategy(strategy);
		order.setQuantity(29);
		order.setPrice(new BigDecimal(3.32));
		order.setOrderType(OrderType.Sell);
		order.setOrderExecutionType(OrderExecutionType.OPEN);
		order.setOrderDate(new Date());
		portfolioManager.createOrder(order);
	}
	
	@Test
	@Transactional
	@Rollback(false)
	public void reCreateOrderTest(){
		
		try {
			OrderStatus orderStatus = orderStatusDao.getOrderStatusByBrokerOrderId("92505daa-c24e-4f36-971b-73c09aa09825");
			Order prevOrder = orderDao.getOrderById(145);
			Order newOrder = (Order) prevOrder.clone();
			newOrder.setOrderDate(new Date());
			newOrder.setPrice(new BigDecimal(0.935));
			portfolioManager.recreateOrder(prevOrder, orderStatus, newOrder);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PortfolioManagerException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
