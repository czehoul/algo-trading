package com.yee.trading.auto.funda;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class BasicFundaFilterTest {

	@Autowired
	@Qualifier("basicFundaFilter")
	private FundaFilter fundaFilter;
	
	@Test
	public void validateFundaTest(){
		try{
			//0119
			System.out.println(fundaFilter.validateFunda("HEVEA-WB"));
		}catch(FundaFilterException e){
			e.printStackTrace();
		}
	}
}
