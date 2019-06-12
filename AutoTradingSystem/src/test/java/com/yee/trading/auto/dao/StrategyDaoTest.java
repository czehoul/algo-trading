package com.yee.trading.auto.dao;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.strategy.Strategy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class StrategyDaoTest {

	@Autowired
	private StrategyDao strategyDao;
	
	@Test
	@Transactional
	public void getStrategyByIdTest(){
		Strategy strategy = strategyDao.getStrategyById(1);
		System.out.println("Strategy " + strategy.getName() + " loaded.");
		//System.out.println("Strategy portfolio " + strategy.getPortfolios().size() + " loaded.");
	}
	@Test
	@Transactional
	public void updateStrategyTest(){
		Strategy strategy = strategyDao.getStrategyById(1);
		strategy.setTotalEquity(new BigDecimal(55000));
		strategyDao.updateStrategy(strategy);
	}
}
