package com.yee.trading.auto.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class WhatsppNotificationSenderTest {

//	@Autowired
//	private NotificationSender notificationSender;
	@Autowired
	private SpringContext springContext;
	@Autowired
	@Qualifier("autoTradingTaskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	
	@Test
	public void sendNotificationTest(){
		//try {
			NotificationSender notificationSender = (NotificationSender) springContext.getApplicationContext().getBean("whatsappNotificationSender");
			notificationSender.addMessage("just test = [test, iiii]");
			//taskExecutor.execute(notificationSender);
			
			NotificationSender notificationSender1 = (NotificationSender) springContext.getApplicationContext().getBean("whatsappNotificationSender");
			notificationSender1.addMessage("just test2 = [test, iiii]");
			//taskExecutor.execute(notificationSender1);
			
			NotificationSender notificationSender2 = (NotificationSender) springContext.getApplicationContext().getBean("whatsappNotificationSender");
			notificationSender2.addMessage("just test3 = [test, iiii]");
			//taskExecutor.execute(notificationSender2);
			
			//check active thread, if zero then shut down the thread pool
			for (;;) {
				int count = taskExecutor.getActiveCount();
				System.out.println("Active Threads : " + count);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
//				if (count == 0) {
//					taskExecutor.shutdown();
//					break;
//				}
			}
//		} catch (NotificationSenderExeption e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
