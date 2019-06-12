package com.yee.trading.auto.event;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

import com.yee.trading.auto.order.OrderManager;
import com.yee.trading.auto.order.OrderStatusListener;
import com.yee.trading.auto.strategy.StrategyRunner;
import com.yee.trading.auto.util.NotificationEventUtil;
import com.yee.trading.auto.util.NotificationSender;
import com.yee.trading.auto.util.SpringContext;

//@Component // define in spring context
public class EventProcessor {
	private final Logger logger = LoggerFactory.getLogger(EventProcessor.class);
	// set the following from spring context
	private List<StrategyRunner> strategyRunners;

	private TaskExecutor taskExecutor;

	private OrderStatusListener orderStatusListener;
	// private NotificationSender notificationSender;
	// private OrderManager orderManager;
	private SpringContext springContext;

	public void onEvent(Event event) {
		EventType eventType = event.getEventType();
		switch (eventType) {
		case NEW_TICK:
			notifyNewTickEvent();
			break;
		case ORDER_GENERATED:
			notifyOrderGeneratedEvent(event);
			break;
		case NOTIFICATION:
			notifyNotificationEvent(event);
			break;
		case ORDER_MATCHED:
			notifyOrderMatchedEvent(event);
			break;
		default:
			// Log error / notification - invalid event
			logger.error("Invalid event type.");
			break;
		}
	}

	private void notifyOrderMatchedEvent(Event event) {
		// not using task executor, should be ok, only db update and dequeue
		// anyway dequeue shouldn't be doing concurrently
		// after that order event will be generated

		OrderStatusEvent orderStatusEvent = (OrderStatusEvent) event;
		onEvent(NotificationEventUtil.createNotificationEvent(String.format("%s order all matched for %s",
				orderStatusEvent.getOrderStatus().getOrderType(), orderStatusEvent.getOrderStatus().getStockName())));
		logger.debug("Order matched event notified. Stock =" + orderStatusEvent.getOrderStatus().getStockName());
		orderStatusListener.orderStatusUpdated(orderStatusEvent.getOrderStatus());

	}

	private void notifyNewTickEvent() {
		for (StrategyRunner strategyRunner : strategyRunners) {
			taskExecutor.execute(strategyRunner);
		}
	}

	private void notifyOrderGeneratedEvent(Event event) {
		OrderEvent orderEvent = (OrderEvent) event;
		OrderManager orderManager = (OrderManager) springContext.getApplicationContext().getBean("genericOrderManager");
		orderManager.setOrder(orderEvent.getOrder());
		logger.debug("Order generated event notified. Order = " + orderEvent.getOrder());
		taskExecutor.execute(orderManager);
	}

	private void notifyNotificationEvent(Event event) {
		NotificationEvent notificationEvent = (NotificationEvent) event;
		NotificationSender notificationSender = (NotificationSender) springContext.getApplicationContext()
				.getBean("notificationSender");
		notificationSender.addMessage(notificationEvent.getMessage());

	}

	public void setStrategyRunners(List<StrategyRunner> strategyRunners) {
		this.strategyRunners = strategyRunners;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public void setSpringContext(SpringContext springContext) {
		this.springContext = springContext;
	}

	public OrderStatusListener getOrderStatusListener() {
		return orderStatusListener;
	}

	public void setOrderStatusListener(OrderStatusListener orderStatusListener) {
		this.orderStatusListener = orderStatusListener;
	}

}
