package com.yee.trading.auto;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {
	
	public static void main(String[] args) {
		App app = new App();
		app.start();
	}
	
	private void start(){
		  ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("Spring-Module.xml");
		  //ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler)ctx.getBean("autoTradingScheduler");
		  //ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor)ctx.getBean("autoTradingTaskExecutor");
		  //System.out.println("----End----");
//		  for (;;) {
//				int count = scheduler.getActiveCount();
//				System.out.println("Active scheduler Threads : " + count);
//				int eCount = executor.getActiveCount();
//				System.out.println("Active executor Threads : " + eCount);
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				if (count == 0 && eCount == 0) {
//					scheduler.shutdown();
//					executor.shutdown();
//					break;
//				}
//			}
	}

}
