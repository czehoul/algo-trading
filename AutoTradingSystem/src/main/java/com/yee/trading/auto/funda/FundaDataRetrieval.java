package com.yee.trading.auto.funda;

public interface FundaDataRetrieval {
	
	public StockFundaDetails retrieveFundaData(String stockCode) throws FundaDataRetrievalException;
}
