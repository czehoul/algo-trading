package com.yee.trading.auto.order;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class OrderStatusManagerTest {
	
	@Autowired
	private OrderStatusManager orderStatusManager;
	
	@Test
	public void checkOrderStatusTest(){
		while(true){
			orderStatusManager.checkOrderStatus();
			System.out.println("Checked!!!");
			try {
				Thread.sleep(30*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
