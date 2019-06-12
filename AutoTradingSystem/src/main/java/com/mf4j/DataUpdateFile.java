package com.mf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent the individual EOD update file structure
 * @author czey01
 *
 */
public class DataUpdateFile {
	
	private Map<String, List<Quote>> grouppedRecords;
	
    public DataUpdateFile() {
    	grouppedRecords = new HashMap<String, List<Quote>>();
    }
    
    public void addRecord(String symbol, Quote quote){
    	if(grouppedRecords.containsKey(symbol)){
    		List<Quote> quoteList = grouppedRecords.get(symbol);
    		quoteList.add(quote);
    	}else{
    		List<Quote> quoteList = new ArrayList<Quote>();
    		quoteList.add(quote);
    		grouppedRecords.put(symbol, quoteList);
    	}
    }
    
    public Map<String, List<Quote>> getGrouppedRecords(){
    	return grouppedRecords;
    }
	
}
