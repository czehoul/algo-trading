package com.yee.trading.auto.marketdata.index;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class YahooFinanceIndexDataRetrieverTest {

	@Autowired
	private IndexDataRetriever indexDataRetriever;
	
	@Test
	public void retrieveIndexDataTest(){
		try{
			//Time zone
			TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
			Calendar yesterday = Calendar.getInstance();
			yesterday.setTimeZone(timeZone);
			yesterday.add(Calendar.DAY_OF_MONTH, -2);
			
			Calendar today = Calendar.getInstance();
			today.setTimeZone(timeZone);
			today.add(Calendar.DAY_OF_MONTH, -2);
			IndexData indexData = indexDataRetriever.retrieveIndexData(yesterday.getTime(), today.getTime(), "GSPC");
			System.out.println(indexData.toString());
		}catch(IndexDataRetrieverException e){
			e.printStackTrace();
		}
	}
}
