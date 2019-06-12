package com.yee.trading.auto.strategy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class GenericStrategyExplorationRunnerTest {
	@Autowired
	@Qualifier("iRichStrategyExplorationRunner")
	private StrategyRunner strategyRunner;
	@Autowired
	@Qualifier("autoTradingTaskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	@Test
	public void runTest(){
		strategyRunner.run();
		//check active thread, if zero then shut down the thread pool
				for (;;) {
					int count = taskExecutor.getActiveCount();
					System.out.println("Active Threads : " + count);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (count == 0) {
						//taskExecutor.shutdown();
						//break;
					}
				}
	}

}
