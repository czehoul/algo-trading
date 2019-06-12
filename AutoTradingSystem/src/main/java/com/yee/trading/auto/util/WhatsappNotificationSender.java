package com.yee.trading.auto.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("whatsappNotificationSender")
// @Scope("prototype")
public class WhatsappNotificationSender implements NotificationSender {
	private final Logger logger = LoggerFactory.getLogger(WhatsappNotificationSender.class);
	private static final List<String> messageList = new ArrayList<String>();

	@Value("${recipients.group}")
	private String recipientsGroup;

	@Override
	public void sendMessage() {

		try {

			if (messageList.size() > 0) {

				String msgToSend = messageList.remove(0);
				logger.debug("Sending notification, message - " + msgToSend);
				 String[] cmd = { "python", "C:\\Software\\yowsup-master\\yowsup-cli", "demos", "-c", "C:\\Software\\yowsup-master\\yowsup-cli.config", "-s", 
						recipientsGroup, msgToSend };
				Process process = Runtime.getRuntime().exec(cmd);
				process.waitFor();
				logger.debug("Message sent.");
			}

			// System.out.println("Done");
		} catch (IOException e) {
			logger.error("IO error sending notification.", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("Error calling Whatsapp batch service to send notification.", e);
			e.printStackTrace();
		}

	}

	@Override
	public void addMessage(String message) {
		messageList.add(message);
	}

}
