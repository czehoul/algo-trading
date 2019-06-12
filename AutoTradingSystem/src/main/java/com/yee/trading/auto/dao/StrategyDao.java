package com.yee.trading.auto.dao;

import java.util.List;

import com.yee.trading.auto.strategy.Strategy;

public interface StrategyDao {
	
	public Strategy getStrategyById(int strategyId);
	
	public Strategy loadStrategyById(int strategyId);
	
	public void updateStrategy(Strategy strategy);
	
	public List<Strategy> getAllStrategies();

}
