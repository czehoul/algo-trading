package com.yee.trading.auto.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.yee.trading.auto.broker.BrokerInterfaceException;
import com.yee.trading.auto.broker.BrokerInterfaceManager;
import com.yee.trading.auto.dao.OrderDao;
import com.yee.trading.auto.dao.PortfolioDao;
import com.yee.trading.auto.dao.StrategyDao;
import com.yee.trading.auto.event.EventProcessor;
import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.portfolio.Portfolio;
import com.yee.trading.auto.stockinfo.StockQuote;
import com.yee.trading.auto.strategy.Strategy;
import com.yee.trading.auto.util.NotificationEventUtil;
import com.yee.trading.auto.util.SpringContext;

@Component
public class EODReportGeneratorImpl implements EODReportGenerator {

	@Autowired
	private ChartGenerator chartGenerator;

	@Autowired
	private PortfolioDao portfolioDao;

	@Autowired
	private StrategyDao strategyDao;

	@Autowired
	private OrderDao orderDao;

	@Autowired
	@Qualifier("simulationBrokerInterfaceManager")
	private BrokerInterfaceManager brokerInterfaceManager;

	private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

	private final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	@Value("${chart.path}")
	private String chartPath;

	@Value("${smtp.server:smtp.gmail.com}")
	private String smtpServer;
	@Value("${smtp.server.port:587}")
	private int smtpServerPort;
	@Value("${smtp.user:czehoul}")
	private String smtpUser;
	@Value("${smtp.password:Ych55191}")
	private String smtpPassword;
	@Value("${recipients.report}")
	private String recipients;
	@Autowired
	private EventProcessor eventProcessor;

	private final Logger logger = LoggerFactory.getLogger(EODReportGeneratorImpl.class);

	@Override
	public void generateReport() {

		try {
			// strategies hold positions
			List<Strategy> strategyList = strategyDao.getAllStrategies();
			MimeMultipart multipart = new MimeMultipart("related");
			// first part (the html)
			BodyPart messageBodyPart = new MimeBodyPart();
			StringBuilder htmlText = new StringBuilder("<H1>Daily Report</H1>");
			BigDecimal hundred = new BigDecimal(100);
			for (Strategy strategy : strategyList) {
				htmlText = htmlText.append("<H2>Strategy ")
						.append(strategy.getName())
						.append("</H2>");
				htmlText = htmlText.append("<H3>Initial Equity = $")
						.append(strategy.getInitialEquity())
						.append(" Latest Equity = $")
						.append(strategy.getTotalEquity())
						.append("</H3>");
				htmlText = htmlText.append("<H3>Open positions</H3>")
						.append("<table border=\"1\"><tr><th>Stock Code</th><th>Stock Name</th><th>Quantity</th><th>Buy Date</th><th>Buy Price</th><th>Buy Value</th><th>Current Price</th><th>Current Value</th><th>Return %</th></tr>");
				List<Portfolio> activePortfolioList = portfolioDao.getPortfoliosByStrategyId(strategy.getId(), true);
				for (Portfolio portfolio : activePortfolioList) {
					BigDecimal currentValue = portfolio.getLastClosePrice()
							.multiply(new BigDecimal(portfolio.getQuantity() * 100)).setScale(2, RoundingMode.HALF_UP);
					BigDecimal returnPercent = currentValue.subtract(portfolio.getTotalAmount())
							.divide(portfolio.getTotalAmount(), 4, RoundingMode.HALF_UP).multiply(hundred)
							.setScale(2, RoundingMode.HALF_UP);
					htmlText = htmlText.append("<tr>").append("<td>").append(portfolio.getStockCode()).append("</td>")
							.append("<td>").append(portfolio.getStockName()).append("</td>").append("<td>")
							.append(portfolio.getQuantity()).append("</td>").append("<td>")
							.append(dateFormat.format(portfolio.getBuyDate())).append("</td>").append("<td>")
							.append(portfolio.getBuyPrice()).append("</td>").append("<td>")
							.append(portfolio.getTotalAmount()).append("</td>").append("<td>")
							.append(portfolio.getLastClosePrice()).append("</td>").append("<td>").append(currentValue)
							.append("</td>").append("<td>").append(returnPercent).append("</td>").append("</tr>");
				}
				htmlText = htmlText.append("</table>").append("<H3>Today's trades")
						.append("</H3>")
						.append("<table border=\"1\"><tr><th>Stock Code</th><th>Stock Name</th><th>Trade Time</th><th>Order Type</th><th>Order Price</th><th>Order Quantity</th><th>Matched Quantity</th><th>Open Price</th><th>Close Price</th></tr>");

				// today's trades, O, B, C, Quantity, including unmatch, not
				// fully match trade
				// for each of today order search for order status, search for
				// order status with quantity > than 0
				// then display the info of the order - order time, order type,
				// price, quantity, match quantity
				List<Order> orderList = orderDao.getOrdersByStrategy(strategy.getId(), getTodayStartTime(),
						getTodayEndTime());
				for (Order order : orderList) {

					StockQuote stockQuote = brokerInterfaceManager.queryStockQuote(order.getStockCode());
					htmlText = htmlText.append("<tr>").append("<td>").append(order.getStockCode()).append("</td>")
							.append("<td>").append(order.getStockName()).append("</td>").append("<td>")
							.append(timeFormat.format(order.getOrderDate())).append("</td>").append("<td>")
							.append(order.getOrderType()).append("</td>").append("<td>").append(order.getPrice())
							.append("</td>").append("<td>").append(order.getOriginalQuantity()).append("</td>")
							.append("<td>").append(order.getQuantity()).append("</td>").append("<td>")
							.append(stockQuote.getOpenPrice()).append("</td>").append("<td>")
							.append(stockQuote.getCurrentPrice()).append("</td>").append("</tr>");

				}

				htmlText = htmlText.append("</table>");
			}

			htmlText = htmlText.append("<H2>Overall Performance</H2><img src=\"cid:image\">");
					
			messageBodyPart.setContent(htmlText.toString(), "text/html");
			multipart.addBodyPart(messageBodyPart);

			// E-Curve
			chartGenerator.generateEquityChart();

			// second part (the image)
			messageBodyPart = new MimeBodyPart();
			DataSource fds = new FileDataSource(chartPath + "EquityCurveChart.jpeg");
			messageBodyPart.setDataHandler(new DataHandler(fds));
			messageBodyPart.setHeader("Content-ID", "<image>");

			// add it
			multipart.addBodyPart(messageBodyPart);
			sendReport(multipart);
			eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent("EOD report has been sent to your email, please check."));
		} catch (ChartGeneratorException e) {
			Log.error("Error generating EOD report equity curve chart. " + e.getMessage(), e);
			eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent("Error generating EOD report equity curve chart. " + e.getMessage()));
			e.printStackTrace();
		} catch (MessagingException e) {
			Log.error("Error generating EOD report multipart message. " + e.getMessage(), e);
			eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent("Error generating multipart message. " + e.getMessage()));
			e.printStackTrace();			
		} catch (BrokerInterfaceException e) {
			Log.error(e.getMessage(), e);
			e.printStackTrace();
			eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent("Error generating EOD report. " + e.getMessage()));
		}

	}

	private void sendReport(MimeMultipart mimeMultipartMsg) throws MessagingException {
		Properties props = new Properties();
		// props.put("mail.smtp.host", smtpServer);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "587");

		Session session = Session.getDefaultInstance(props, null);

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress("czehoul@gmail.com"));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
		message.setSubject("KLSE AutoTrading EOD Report");
		message.setContent(mimeMultipartMsg);
		Transport transport = session.getTransport("smtp");
		try {
			transport.connect(smtpServer, smtpUser, smtpPassword);
			transport.sendMessage(message, message.getAllRecipients());
		} finally {
			transport.close();
		}

	}

	private Date getTodayStartTime() {
		Calendar startCalc = Calendar.getInstance();
		startCalc.set(Calendar.HOUR_OF_DAY, 0);
		startCalc.set(Calendar.MINUTE, 0);
		startCalc.set(Calendar.SECOND, 0);
		startCalc.set(Calendar.MILLISECOND, 0);
		return startCalc.getTime();
	}

	private Date getTodayEndTime() {
		Calendar stopCalc = Calendar.getInstance();
		stopCalc.set(Calendar.HOUR_OF_DAY, 23);
		stopCalc.set(Calendar.MINUTE, 59);
		stopCalc.set(Calendar.SECOND, 59);
		stopCalc.set(Calendar.MILLISECOND, 999);
		return stopCalc.getTime();
	}
}
