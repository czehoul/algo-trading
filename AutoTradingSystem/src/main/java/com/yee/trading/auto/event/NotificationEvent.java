package com.yee.trading.auto.event;

public class NotificationEvent extends Event {
	private String message;
	
	public NotificationEvent(EventType eventType) {
		super(eventType);		
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	

}
