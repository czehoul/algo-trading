package com.yee.trading.auto.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.strategy.Strategy;

//@Transactional//add this in controller layer
@Repository
public class StrategyDaoImpl implements StrategyDao {
	@PersistenceContext
	private EntityManager em;

	@Override	
	@Transactional(rollbackFor = Exception.class)
	public Strategy getStrategyById(int strategyId) {
		
		return (Strategy)em.find(Strategy.class, new Integer(strategyId));
	}

	@Override
	public void updateStrategy(Strategy strategy) {
		 
	    em.merge(strategy);

	}

	@Override
	public Strategy loadStrategyById(int strategyId) {
		
		return (Strategy)em.getReference(Strategy.class, new Integer(strategyId));
	}

	@Override
	public List<Strategy> getAllStrategies() {		
		return em.createQuery("from Strategy").getResultList();
	}

}
