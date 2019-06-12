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
public class OrderDaoImpl implements OrderDao {
	@PersistenceContext
	private EntityManager em;

	@Override
	public void updateOrder(Order order) {
		em.merge(order);
	}

	@Override
	public void deleteOrder(Order order) {
		em.remove(order);

	}

	@Override
	public Order createOrder(Order order) {
		em.persist(order);		
		return order;
	}

	@Override
	public List<Order> getActiveOrders(Date fromDate, Date toDate) {
		Query query = em
				.createQuery("from Order where done = :done and orderDate between :fromDate and :toDate ");

		query.setParameter("done", false);
		query.setParameter("fromDate", fromDate);
		query.setParameter("toDate", toDate);
		return query.getResultList();
	}

	@Override
	public List<Order> getOrdersByStrategy(int strategyId, Date fromDate,
			Date toDate) {
		Query query = em
				.createQuery("from Order where strategy.id = :strategyId and orderDate between :fromDate and :toDate order by orderDate");

		query.setParameter("strategyId", strategyId);
		query.setParameter("fromDate", fromDate);
		query.setParameter("toDate", toDate);
		return query.getResultList();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Order getOrderById(int id) {
		return (Order)em.find(Order.class, new Integer(id));
		
	}

	@Override
	public Order getOrderByOrderStatus(OrderStatus orderStatus) {
		Calendar startTime = Calendar.getInstance();
		startTime.setTime(orderStatus.getOrderDate());
		startTime.add(Calendar.MINUTE, -1);
		Calendar endTime = Calendar.getInstance();
		endTime.setTime(orderStatus.getOrderDate());
		endTime.add(Calendar.MINUTE, 1);
		
		Query query = em.createQuery("from Order where stockName = :stockName and quantity = :quantity and price = :price and orderDate between :fromDate and :toDate ");
		
		query.setParameter("stockName", orderStatus.getStockName());
		query.setParameter("quantity", orderStatus.getQuantity());
		query.setParameter("price", orderStatus.getPrice());
		query.setParameter("fromDate", startTime.getTime());
		query.setParameter("toDate", endTime.getTime());
		return (Order)query.getSingleResult();
	}

}
