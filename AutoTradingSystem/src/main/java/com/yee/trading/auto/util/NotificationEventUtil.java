package com.yee.trading.auto.util;

import com.yee.trading.auto.event.EventType;
import com.yee.trading.auto.event.NotificationEvent;

public class NotificationEventUtil {

	public static NotificationEvent createNotificationEvent(String message){
		NotificationEvent event = new NotificationEvent(EventType.NOTIFICATION);
		event.setMessage(message);
		return event;
	}
}
