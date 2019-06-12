package com.yee.trading.auto.strategy;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.broker.BrokerInterfaceException;
import com.yee.trading.auto.broker.BrokerInterfaceManager;
import com.yee.trading.auto.dao.PortfolioDao;
import com.yee.trading.auto.dao.StrategyDao;
import com.yee.trading.auto.event.EventProcessor;
import com.yee.trading.auto.event.EventType;
import com.yee.trading.auto.event.OrderEvent;
import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.order.OrderExecutionType;
import com.yee.trading.auto.order.OrderType;
import com.yee.trading.auto.util.NotificationEventUtil;
import com.yee.trading.auto.util.OrderQueueContainer;
import com.yee.trading.auto.util.TradingDayChecker;

//define multiple spring bean base on this class with different param
public class GenericAmibrokerStrategyRunner implements StrategyRunner {

	private String analysisFilePath;
	private String outFilePath;// =
								// "C:\\automatedTrading\\IRichAnalysisOut.csv";
	private int strategyId;
	private EventProcessor eventProcessor;
	private final Logger logger = LoggerFactory.getLogger(GenericAmibrokerStrategyRunner.class);
	private StrategyDao strategyDao;
	private PortfolioDao portfolioDao;
	private BrokerInterfaceManager brokerInterfaceManager;
	private TradingDayChecker tradingDayChecker;
	// private boolean testing=true;

	// private String strate
	@Override
	public void run() {
		try {
			String[] cmd = { "cscript.exe", analysisFilePath };
			Strategy strategy = getStrategy(strategyId);
			Process p;
			try {
				// if(!testing){
				p = Runtime.getRuntime().exec(cmd);
				p.waitFor();

				// }
				logger.debug("Amibroker analysis out file generated");
				// extract signal
				ReversedLinesFileReader reader = new ReversedLinesFileReader(new File(outFilePath));
				List<String> outFileLines = new ArrayList<String>();
				try {

					boolean proceed = true;
					while (proceed) {
						String line = reader.readLine();
						// System.out.println(line);
						outFileLines.add(line);
						if (line.length() >= 8 && line.matches("([0-9]{1,2})/([0-9]{2})/([0-9]{4})")) {
							proceed = false;
						}
					}
				} finally {
					reader.close();
				}
				Collections.reverse(outFileLines);
				// 1. check date is today date position 0
				DateFormat df = new SimpleDateFormat("d/MM/yyyy");
				String today = df.format(tradingDayChecker.getLastTradingPlusOneDate());
				// String today =
				// df.format(tradingDayChecker.getLastTradingDate());
				// String today = "27/06/2016";
				logger.debug("Extracting signal for " + today);
				List<String> buySignals = null;
				List<String> sellSignals = null;
				if (outFileLines.get(0).equals(today)) {
					// 2. read and extract each entry signals position 1
					// null if no signal
					buySignals = extractSignals(outFileLines.get(1), "=Buy");
					if (buySignals != null)
						generateOrderEvent(strategy, OrderType.Buy, buySignals);
					logger.debug("Buy signals = " + buySignals);
					// 3. read and extract each exit signals position 2
					// null if no signal
					sellSignals = extractSignals(outFileLines.get(2), "=Sell");
					if (sellSignals != null)
						generateOrderEvent(strategy, OrderType.Sell, sellSignals);
					logger.debug("Sell signals = " + sellSignals);
				}

				StringBuilder notificationMsg = new StringBuilder();
				notificationMsg = notificationMsg.append("Strategy runner for strategy ").append(strategy.getName())
						.append(" completed successfully. ").append("Buy signals=" + buySignals).append(". ")
						.append("Sell signals=" + sellSignals);
				eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(notificationMsg.toString()));

			} catch (BrokerInterfaceException e) {

				e.printStackTrace();
				logger.error("Error searching stock code - " + e.getMessage());
				eventProcessor.onEvent(NotificationEventUtil
						.createNotificationEvent("Error searching stock code - " + e.getMessage()));

			} catch (IOException e) {
				logger.error("Error running analysis - " + e.getMessage());
				eventProcessor.onEvent(
						NotificationEventUtil.createNotificationEvent("Error running analysis - " + e.getMessage()));
				e.printStackTrace();
			} catch (InterruptedException e) {
				logger.error("Error running analysis - " + e.getMessage());
				eventProcessor.onEvent(
						NotificationEventUtil.createNotificationEvent("Error running analysis - " + e.getMessage()));
				e.printStackTrace();
			}
		} catch (RuntimeException re) {
			logger.error("Runtime exception occured in GenericAmibrokerStrategy runner", re);
			eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
					"Runtime exception occured in GenericAmibrokerStrategy" + re.getMessage()));
		}
	}

	@Transactional(rollbackFor = Exception.class)
	private Strategy getStrategy(int stratgeyId) {
		return strategyDao.getStrategyById(stratgeyId);
	}

	@Transactional(rollbackFor = Exception.class)
	private void generateOrderEvent(Strategy strategy, OrderType orderType, List<String> signals)
			throws BrokerInterfaceException {
		
		Calendar today = Calendar.getInstance();
		if(tradingDayChecker.nonTradingDay(today)){
			logger.info("Today is a holiday, order will not be submitted.");
			eventProcessor.onEvent(
					NotificationEventUtil.createNotificationEvent("Today is a holiday, order will not be submitted."));
			return;
		}
		
		int availablePosition = strategy.getAvailablePosition();

		for (String signal : signals) {
			if (orderType == OrderType.Buy) {
				if (availablePosition > 0) {
					triggerOrderEvent(strategy, orderType, signal);
				} else {
					// Queue the buy order
					OrderQueueContainer.getInstance().enQueueOrder(createOrder(strategy, orderType, signal));
					logger.debug("Send order to queue. No avalialbe position to buy " + signal);
				}
				availablePosition--;
			} else {
				// should check whether it is existing poaition too
				try {
					portfolioDao.getStrategyPortfolioByStockName(strategy.getId(), signal, true);
					triggerOrderEvent(strategy, orderType, signal);
				} catch (NoResultException e) {
					logger.debug("There is not current position to sell " + signal);
				}
			}

		}
	}

	private Order createOrder(Strategy strategy, OrderType orderType, String signal) throws BrokerInterfaceException {
		String code = brokerInterfaceManager.searchStockCode(signal);
		if (code == null || code.trim().length() == 0)
			throw new BrokerInterfaceException("Null result searching stock code for " + signal);
		Order order = new Order();
		order.setOrderType(orderType);
		order.setStockCode(code);
		order.setStockName(signal);
		order.setLive(strategy.isLive());
		OrderExecutionType orderExecutionType = orderType == OrderType.Buy ? strategy.getBuyOrderExecutionType()
				: strategy.getSellOrderExecutionType();
		order.setOrderExecutionType(orderExecutionType);
		order.setStrategy(strategy);
		return order;
	}

	private void triggerOrderEvent(Strategy strategy, OrderType orderType, String signal)
			throws BrokerInterfaceException {
		Log.debug(String.format("Triggering %s order event for stock %s", orderType.toString(), signal));
		OrderEvent orderEvent = new OrderEvent(EventType.ORDER_GENERATED);

		orderEvent.setOrder(createOrder(strategy, orderType, signal));
		eventProcessor.onEvent(orderEvent);
	}

	private List<String> extractSignals(String signalLine, String token) {
		List<String> signalList = null;
		String extractedLine = signalLine.substring(signalLine.indexOf(":") + 1);
		if (extractedLine.indexOf(token) > -1) {
			String[] signals = extractedLine.split(",");
			signalList = new ArrayList<String>(signals.length - 1);
			for (String signal : signals) {
				if (signal.trim().length() > 0)
					signalList.add(signal.substring(0, signal.indexOf("=")).trim());
			}
		}
		return signalList;
	}

	public void setAnalysisFilePath(String analysisFilePath) {
		this.analysisFilePath = analysisFilePath;
	}

	public void setOutFilePath(String outFilePath) {
		this.outFilePath = outFilePath;
	}

	public void setStrategyId(int strategyId) {
		this.strategyId = strategyId;
	}

	public void setEventProcessor(EventProcessor eventProcessor) {
		this.eventProcessor = eventProcessor;
	}

	public void setStrategyDao(StrategyDao strategyDao) {
		this.strategyDao = strategyDao;
	}

	public void setBrokerInterfaceManager(BrokerInterfaceManager brokerInterfaceManager) {
		this.brokerInterfaceManager = brokerInterfaceManager;
	}

	public void setTradingDayChecker(TradingDayChecker tradingDayChecker) {
		this.tradingDayChecker = tradingDayChecker;
	}

	public static void main(String[] args) {
		new GenericAmibrokerStrategyRunner().run();
	}

	public PortfolioDao getPortfolioDao() {
		return portfolioDao;
	}

	public void setPortfolioDao(PortfolioDao portfolioDao) {
		this.portfolioDao = portfolioDao;
	}

}
