package com.yee.trading.auto.funda;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class KLSEScreenerFundaDataRetrieverTest {

	@Autowired
	private FundaDataRetrieval fundaDataRetriever;
	
	@Test
	public void retrieveFundaDataTest(){
		try{			
			StockFundaDetails fundaData = fundaDataRetriever.retrieveFundaData("8311");
			System.out.println(fundaData.getStock().getPE());
		}catch(FundaDataRetrievalException e){
			e.printStackTrace();
		}
	}
}
