package com.yee.trading.auto.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.yee.trading.auto.report.EquityTracking;

@Repository
public class EquityTrackingDaoImpl implements EquityTrackingDao {

	@PersistenceContext
	private EntityManager em;
	
	@Override
	public void createEquityTracking(EquityTracking equityTracking) {
		em.persist(equityTracking);
	}

	@Override
	public List<EquityTracking> getEquityTrackingByStrategy(int strategyId, Date fromDate,
			Date toDate) {
		Query query = em.createQuery("from EquityTracking where strategy.id = :strategyId and closeDate between :fromDate and :toDate ");
		query.setParameter("strategyId", strategyId);
		query.setParameter("fromDate", fromDate);
		query.setParameter("toDate", toDate);
		return query.getResultList();
	}

}
