package com.yee.trading.auto.util;

public interface NotificationSender{
	public void addMessage(String message);
	
	public void sendMessage();
	
	//public void setMessage(MimeMultipart message);
	//public void sendNotification(String message) throws NotificationSenderExeption;
}
