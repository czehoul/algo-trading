package com.yee.trading.auto.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.stockinfo.OrderStatus;

@Repository
public class OrderStatusDaoImpl implements OrderStatusDao {

	@PersistenceContext
	private EntityManager em;
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public OrderStatus updateOrderStatus(OrderStatus orderStatus) {
		OrderStatus result = em.merge(orderStatus);
		em.flush();
		return result;

	}

	@Override
	public void deleteOrderStatus(OrderStatus orderStatus) {
		em.remove(orderStatus);

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public OrderStatus createOrderStatus(OrderStatus orderStatus) {
		em.persist(orderStatus);
		em.flush();
		//em.getTransaction().commit();
		return orderStatus;

	}

//	@Override
//	public List<OrderStatus> getActiveOrderStatusByStrategy(int strategyId, Date fromDate, Date toDate) {
//		Query query = em.createQuery("from OrderStatus where strategy.id = :strategyId and orderDate between :fromDate and :toDate and orderStatusType != 'ALL_MATCHED'");
//		
//		query.setParameter("strategyId", strategyId);
//		query.setParameter("fromDate", fromDate);
//		query.setParameter("toDate", toDate);
//		return query.getResultList();
//	}

	@Override
	public OrderStatus getOrderStatusByStockName(String stockName, Date fromDate, Date toDate) {
		Query query = em.createQuery("from OrderStatus where stockName = :stockName and orderDate between :fromDate and :toDate ");
		
		query.setParameter("stockName", stockName);
		query.setParameter("fromDate", fromDate);
		query.setParameter("toDate", toDate);
		return (OrderStatus)query.getSingleResult();
	}

	@Override
	public List<OrderStatus> getAllOrderStatus(Date fromDate, Date toDate) {
		Query query = em.createQuery("from OrderStatus where orderDate between :fromDate and :toDate ");
		query.setParameter("fromDate", fromDate);
		query.setParameter("toDate", toDate);
		return query.getResultList();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public OrderStatus getOrderStatusByOrder(Order order) {
		Calendar startTime = Calendar.getInstance();
		startTime.setTime(order.getOrderDate());
		startTime.add(Calendar.MINUTE, -1);
		Calendar endTime = Calendar.getInstance();
		endTime.setTime(order.getOrderDate());
		endTime.add(Calendar.MINUTE, 1);
		
		Query query = em.createQuery("from OrderStatus where stockName = :stockName and quantity = :quantity and price = :price and orderDate between :fromDate and :toDate ");
		
		query.setParameter("stockName", order.getStockName());
		query.setParameter("quantity", order.getQuantity());
		query.setParameter("price", order.getPrice());
		query.setParameter("fromDate", startTime.getTime());
		query.setParameter("toDate", endTime.getTime());
		return (OrderStatus)query.getSingleResult();
	}

	@Override
	public OrderStatus getOrderStatusByBrokerOrderId(String brokerOrderId) {
		return (OrderStatus)em.find(OrderStatus.class, new String(brokerOrderId));
		
	}

}
