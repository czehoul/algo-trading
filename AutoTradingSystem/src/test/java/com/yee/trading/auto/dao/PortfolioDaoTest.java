package com.yee.trading.auto.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.portfolio.Portfolio;
import com.yee.trading.auto.strategy.Strategy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class PortfolioDaoTest {

	@Autowired
	private PortfolioDao portfolioDao;
	
	@Autowired
	private StrategyDao strategyDao;
	
	@Test
	@Transactional
	@Rollback(false)
	public void createPortfolioTest(){
		Strategy strategy = strategyDao.loadStrategyById(new Integer(1));
		Portfolio portfolio = new Portfolio();
		portfolio.setBuyDate(new Date());
		portfolio.setBuyPrice(new BigDecimal(2.4));
		portfolio.setHold(true);
		portfolio.setQuantity(39);
		portfolio.setStockCode("8869WC");
		portfolio.setStockName("PMETAL-WC");
		portfolio.setStrategy(strategy);
		portfolio.setTotalAmount(new BigDecimal(9360));
		portfolio.setTotalAmountIncCost(new BigDecimal(9382.9));
		portfolioDao.cretePortfolio(portfolio);
	}
	
	@Test
	@Transactional
	public void getStrategyPortfolioByStockCodeTest(){
		Portfolio portfolio = portfolioDao.getStrategyPortfolioByStockCode(1, "0018");
		System.out.println("Portfolio " + portfolio.getStockName() + " retrieve. Quantity = " + portfolio.getQuantity());
		
	}
	
	@Test
	@Transactional
	public void getStrategyPortfolioByStockNameTest(){
		Portfolio portfolio = portfolioDao.getStrategyPortfolioByStockName(1, "KLK", true);
		System.out.println("Portfolio " + portfolio.getStockName() + " retrieve. Quantity = " + portfolio.getQuantity());
		
	}
	
	@Test
	@Transactional(readOnly = true)
	public void getPortfoliosByStrategyIdTest(){
		List<Portfolio> portfolio = portfolioDao.getPortfoliosByStrategyId(1);
	}
	
	@Test
	@Transactional
	public void updatePortfolioTest(){
		Portfolio portfolio = portfolioDao.getStrategyPortfolioByStockCode(1, "0021");
		portfolio.setSellDate(new Date());
		portfolio.setSellPrice(new BigDecimal(1.51));
		portfolio.setHold(false);
		portfolioDao.updatePortfolio(portfolio);
	}
	
	@Test
	@Transactional
	@Rollback(false)
	public void deletePortfolioTest(){
		Portfolio portfolio = portfolioDao.getPortfolioById(283);
		portfolioDao.deletePortfolio(portfolio);
	}
	
}
