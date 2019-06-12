package com.yee.trading.auto.event;

public class Event {
	
	private EventType eventType;
	
	public Event(EventType eventType) {
		this.eventType = eventType;
	}
	
	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
	
	

}
