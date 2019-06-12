package com.yee.trading.auto.marketdata.index;

import java.util.Date;

public interface IndexDataRetriever {
	
	
	public IndexData retrieveIndexData(Date fromDate, Date toDate, String code) throws IndexDataRetrieverException ;
	
	public IndexData retrieveIndexData(String code) throws IndexDataRetrieverException ;

}
