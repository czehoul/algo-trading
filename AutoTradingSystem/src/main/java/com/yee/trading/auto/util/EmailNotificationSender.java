package com.yee.trading.auto.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//will be call by event processor
@Component("notificationSender")
// @Scope("prototype")
public class EmailNotificationSender implements NotificationSender {
	private final Logger logger = LoggerFactory.getLogger(EmailNotificationSender.class);
	@Value("${smtp.server:smtp.gmail.com}")
	private String smtpServer;
	@Value("${smtp.server.port:587}")
	private int smtpServerPort;
	@Value("${smtp.user:czehoul}")
	private String smtpUser;
	@Value("${smtp.password:xxxxxxx}")
	private String smtpPassword;
	@Value("${recipients}")
	private String recipients;

	private Properties props;
	Session session;

	private static final List<String> messageList = new ArrayList<String>();

	public EmailNotificationSender() {
		props = new Properties();
		// props.put("mail.smtp.host", smtpServer);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "587");
		session = Session.getDefaultInstance(props, null);
	}

	@Override
	public void sendMessage() {
		try {
			if (messageList.size() > 0) {
				String notificationMsg = messageList.remove(0);
				try {

					Message message = new MimeMessage(session);
					// message.setFrom(new
					// InternetAddress("czehoul@gmail.com"));
					message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
					message.setSubject("System Notification");
					message.setFrom(InternetAddress.parse("czehoul@gmail.com")[0]);
					message.setText(notificationMsg);

					Transport transport = session.getTransport("smtp");
					try {
						transport.connect(smtpServer, smtpUser, smtpPassword);
						transport.sendMessage(message, message.getAllRecipients());
					} finally {
						transport.close();
					}

				} catch (MessagingException e) {
					e.printStackTrace();
					logger.error("Erro sending notification.", e);
				}
			}
		} catch (RuntimeException re) {
			logger.error("Runtime exception occured in sendMessage", re);

		}

	}

	public void addMessage(String message) {
		messageList.add(message);
	}

}
