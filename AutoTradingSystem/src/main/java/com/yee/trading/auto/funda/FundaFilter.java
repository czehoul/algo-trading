package com.yee.trading.auto.funda;

public interface FundaFilter {
	
	public boolean validateFunda(String stockName) throws FundaFilterException;

}
