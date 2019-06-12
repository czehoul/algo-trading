package com.yee.trading.auto.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.yee.trading.auto.order.OrderType;
import com.yee.trading.auto.portfolio.Portfolio;

//@Transactional //add this in controller layer
@Repository
public class PortfolioDaoImpl implements PortfolioDao {
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public void updatePortfolio(Portfolio portfolio) {
	    
	    em.merge(portfolio);

	}

	@Override
	public Portfolio getStrategyPortfolioByStockCode(int strategyId, String code) {
		
		Query query = em.createQuery("from Portfolio where strategy.id = :strategyId and stockCode = :code and hold = true");
		query.setParameter("code", code);
		query.setParameter("strategyId", strategyId); 
		return (Portfolio)query.getSingleResult();
		
	}
	
	@Override
	public Portfolio getStrategyPortfolioByStockName(int strategyId, String name) {
		
		Query query = em.createQuery("from Portfolio where strategy.id = :strategyId and stockName = :name");
		query.setParameter("name", name);
		query.setParameter("strategyId", strategyId); 
		return (Portfolio)query.getSingleResult();
		
	}

	@Override
	public List<Portfolio> getPortfolioByStockCode(String code) {		 
		Query query = em.createQuery("from Portfolio where stockCode = :code ");
		query.setParameter("code", code);
		return query.getResultList();
		
	}

	@Override
	public List<Portfolio> getPortfoliosByStrategyId(int strategyId) {		 
		Query query = em.createQuery("from Portfolio where strategy.id = :strategyId ");
		query.setParameter("strategyId", strategyId);  
		return query.getResultList();
	}

	@Override
	public void cretePortfolio(Portfolio portfolio) {
		
		em.persist(portfolio);

	}

	@Override
	public List<Portfolio> getPortfoliosByStrategyId(int strategyId, boolean hold) {		
		Query query = em.createQuery("from Portfolio where strategy.id = :strategyId and hold = :hold");
		query.setParameter("strategyId", strategyId);  
		query.setParameter("hold", hold);
		return query.getResultList();
	}

	@Override
	public List<Portfolio> getPortfolioByStockCode(String code, OrderType orderType, Date startDate, Date endDate) {
		
		Query query = null;
		if(orderType == OrderType.Buy) {
			query =  em.createQuery("from Portfolio where stockCode = :code and buyDate between :date and :ceilDate ");
		}else{
			query =  em.createQuery("from Portfolio where stockCode = :code and sellDate between :date and :ceilDate ");
		}
		query.setParameter("code", code);
		query.setParameter("date", startDate);
		query.setParameter("ceilDate", endDate);
		return query.getResultList();
	}

	@Override
	public void deletePortfolio(Portfolio portfolio) {
		
		em.remove(portfolio);
		
	}

	@Override
	public Portfolio getPortfolioById(int id) {
		return (Portfolio)em.find(Portfolio.class, new Integer(id));
	}

	@Override
	public List<Portfolio> getPortfolioWithActiveOrder(Date startDate, Date endDate) {

		Query query =  em.createQuery("from Portfolio where buyDate between :date and :ceilDate or sellDate between :date and :ceilDate");
		query.setParameter("date", startDate);
		query.setParameter("ceilDate", endDate);
		return query.getResultList();
	}

	@Override
	public Portfolio getStrategyPortfolioByStockName(int strategyId, String name, boolean hold) {
		Query query = em.createQuery("from Portfolio where strategy.id = :strategyId and stockName = :name and hold = :hold");
		query.setParameter("name", name);
		query.setParameter("strategyId", strategyId); 
		query.setParameter("hold", hold);
		return (Portfolio)query.getSingleResult();
		
	}

}
