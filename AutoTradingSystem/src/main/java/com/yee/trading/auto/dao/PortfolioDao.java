package com.yee.trading.auto.dao;

import java.util.Date;
import java.util.List;

import com.yee.trading.auto.order.OrderType;
import com.yee.trading.auto.portfolio.Portfolio;

public interface PortfolioDao {

	public Portfolio getPortfolioById(int id);
	
	public void updatePortfolio(Portfolio Portfolio);
	
	public void deletePortfolio(Portfolio Portfolio);

	public Portfolio getStrategyPortfolioByStockCode(int strategyId, String code);
	
	public Portfolio getStrategyPortfolioByStockName(int strategyId, String name);

	public List<Portfolio> getPortfolioByStockCode(String code);

	public List<Portfolio> getPortfoliosByStrategyId(int strategyId);
	
	public List<Portfolio> getPortfoliosByStrategyId(int strategyId, boolean hold);
	
	public Portfolio getStrategyPortfolioByStockName(int strategyId, String name, boolean hold);

	public void cretePortfolio(Portfolio Portfolio);
	
	public List<Portfolio> getPortfolioByStockCode(String code, OrderType orderType, Date startDate, Date endDate);

	public List<Portfolio> getPortfolioWithActiveOrder(Date startDate, Date endDate);
}
