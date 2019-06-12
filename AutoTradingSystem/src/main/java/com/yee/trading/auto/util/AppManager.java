package com.yee.trading.auto.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.yee.trading.auto.order.GenericOrderManager;

@Component("appManager")
public class AppManager {
	@Autowired
	@Qualifier("autoTradingScheduler")
	private ThreadPoolTaskScheduler taskScheduler;
	@Autowired
	@Qualifier("autoTradingTaskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	
	private final Logger logger = LoggerFactory.getLogger(AppManager.class);
	
	public void shutDown(){
		logger.info("Shutting down AutoTrading System...");
		taskScheduler.shutdown();
		taskExecutor.shutdown();
		System.exit(0);
		logger.info("Shutting down completed");
	}
}
