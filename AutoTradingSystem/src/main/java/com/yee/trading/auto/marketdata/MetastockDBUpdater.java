package com.yee.trading.auto.marketdata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.mf4j.DataUpdateException;
import com.mf4j.DataUpdateFile;
import com.mf4j.MetaFileManager;
import com.mf4j.Quote;
import com.yee.trading.auto.event.EventProcessor;

public class MetastockDBUpdater implements StockDBUpdater {
	
	private Map<String, String> sourceToDestinationMap;
	private String dataSourceLocation;
	private String dataDestinationLocation;
	private boolean addSymbolSupport;
	private EventProcessor eventProcessor;
	
	
	
	@Override
	public void updateDB() throws StockDBUpdaterException {
		Iterator<Entry<String, String>> it = sourceToDestinationMap.entrySet().iterator();
		while (it.hasNext()) {	
			Entry<String, String> entry = it.next();
			MetaFileManager manager = new MetaFileManager(dataDestinationLocation.concat(entry.getValue()));
			manager.setAddSymbolSupport(addSymbolSupport);
			manager.setEventProcessor(eventProcessor);
			String pattern = "yyyyMMdd";
		    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			try{
				manager.initialize();
				DataUpdateFile dataUpdateFile = new DataUpdateFile();
				BufferedReader br = new BufferedReader(new FileReader(dataSourceLocation.concat(entry.getKey())));
				try {			   
				    String line = br.readLine();
				    int lineNo = 0;			    

				    while (line != null) {
				        //process
				    	if(lineNo > 0){//skip header
					    	String[] lineItems = line.split(",");				    	
					    	if(lineItems.length >= 0){
					    		Quote quote = new Quote(simpleDateFormat.parse(lineItems[2].trim()), new Float(lineItems[4].trim()), new Float(lineItems[5].trim()), new Float(lineItems[3].trim()), new Float(lineItems[6].trim()), new Long(lineItems[8].trim()), new Long(lineItems[7].trim()));
					    		quote.setDateString(lineItems[2]);
					    		dataUpdateFile.addRecord(lineItems[0], quote);
					    	} else {
					    		throw new StockDBUpdaterException("Wrong line length in file ".concat(entry.getKey()));
					    	}
				    	}
				        line = br.readLine();
				        lineNo++;
				    }
				    
				} finally {
				    br.close();
				}
				manager.updateData(dataUpdateFile);
			}catch(DataUpdateException | IOException | ParseException e){
				throw new StockDBUpdaterException("MetaStock DB update error - ".concat(e.getMessage()));
			}
			
		}

	}

	public void setSourceToDestinationMap(Map<String, String> sourceToDestinationMap) {
		this.sourceToDestinationMap = sourceToDestinationMap;
	}

	public void setDataSourceLocation(String dataSourceLocation) {
		this.dataSourceLocation = dataSourceLocation;
	}

	public void setDataDestinationLocation(String dataDestinationLocation) {
		this.dataDestinationLocation = dataDestinationLocation;
	}

	public boolean isAddSymbolSupport() {
		return addSymbolSupport;
	}

	public void setAddSymbolSupport(boolean addSymbolSupport) {
		this.addSymbolSupport = addSymbolSupport;
	}

	public void setEventProcessor(EventProcessor eventProcessor) {
		this.eventProcessor = eventProcessor;
	}
	
}
