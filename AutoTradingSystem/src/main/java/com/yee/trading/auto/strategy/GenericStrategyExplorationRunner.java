package com.yee.trading.auto.strategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

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
import com.yee.trading.auto.funda.FundaFilter;
import com.yee.trading.auto.funda.FundaFilterException;
import com.yee.trading.auto.marketdata.index.IndexDataRetrieverException;
import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.order.OrderExecutionType;
import com.yee.trading.auto.order.OrderType;
import com.yee.trading.auto.util.NotificationEventUtil;
import com.yee.trading.auto.util.OrderQueueContainer;
import com.yee.trading.auto.util.TradingDayChecker;

//Configure this spring bean in spring context xml
public class GenericStrategyExplorationRunner implements StrategyRunner {
	private String analysisFilePath;
	private String analysisRunFilePath;
	private String outFilePath;
	private int strategyId;
	private EventProcessor eventProcessor;
	private final Logger logger = LoggerFactory.getLogger(GenericStrategyExplorationRunner.class);
	private StrategyDao strategyDao;
	private PortfolioDao portfolioDao;
	private BrokerInterfaceManager brokerInterfaceManager;
	private TradingDayChecker tradingDayChecker;
	private FundaFilter fundaFilter; // inject from spring config

	@Transactional
	private Strategy getStrategy(int stratgeyId) {
		return strategyDao.getStrategyById(stratgeyId);
	}

	private void setAnalysisDate() throws IOException {
		// read file
		DateFormat df = new SimpleDateFormat("d/MM/yyyy");
		String lastTradingday = df.format(tradingDayChecker.getLastTradingDate());
		String today = df.format(tradingDayChecker.getLastTradingPlusOneDate());

		Path path = Paths.get(analysisFilePath);
		Charset charset = StandardCharsets.UTF_8;

		String content = new String(Files.readAllBytes(path), charset);
		content = content.replaceAll("<FromDate>.*?</FromDate>", "<FromDate>" + lastTradingday + " 00:00:00</FromDate>")
				.replaceAll("<ToDate>.*?</ToDate>", "<ToDate>" + today + "</ToDate>");

		Files.write(path, content.getBytes(charset));
	}

	private String getSignalsString(List<StrategySignal> signals){
		StringBuilder signalStringBuilder = new StringBuilder();
		for(StrategySignal signal : signals){
			signalStringBuilder = signalStringBuilder.append(signal.getStockName()).append(", ");
		}
		if(signalStringBuilder.length() > 0){
			signalStringBuilder = signalStringBuilder.delete(signalStringBuilder.length()-2, signalStringBuilder.length());
		}
		return signalStringBuilder.toString();
	}
	
	private StrategySignal createStrategySignal(String[] signalItems) {
		StrategySignal signal = new StrategySignal();
		signal.setStockName(signalItems[0]);
		signal.setClosePrice(new BigDecimal(signalItems[2]));
		if (("1.00".equals(signalItems[5]))) {
			if(("1.00".equals(signalItems[6]))){
				signal.setOrderExecutionType(OrderExecutionType.LIMIT);				
			} else {
				signal.setOrderExecutionType(OrderExecutionType.OPEN);
			}
		} else {
			signal.setOrderExecutionType(OrderExecutionType.CLOSE);
		}
		return signal;
	}
	
	private void processSignals(Strategy strategy) throws IOException, IndexDataRetrieverException,
			BrokerInterfaceException, ParseException, FundaFilterException {

		BufferedReader br = new BufferedReader(new FileReader(new File(outFilePath)));
		try {
			String line = null;
			int lineNo = 0;
			int buyNo = 0;
			List<StrategySignal> buySignals = new ArrayList<StrategySignal>();
			List<StrategySignal> exceededBuySignals = new ArrayList<StrategySignal>();
			List<StrategySignal> nonFundaBuySignals = new ArrayList<StrategySignal>();
			List<StrategySignal> sellSignals = new ArrayList<StrategySignal>();
			while ((line = br.readLine()) != null) {
				if (lineNo > 0) {
					String[] signalItems = line.split(",");
					if (signalItems.length >= 5) {
						if (signalItems[3].equals("1.00")) {

							if (fundaFilter.validateFunda(signalItems[0])) {
								buyNo++;

								if (buyNo <= 5) {									
									buySignals.add(createStrategySignal(signalItems));
								} else {									
									exceededBuySignals.add(createStrategySignal(signalItems));
								}
							} else {								
								nonFundaBuySignals.add(createStrategySignal(signalItems));
							}
						} else {
							// check is this in portfolio before adding to sell
							// signal list
							// it is handled in generateOrderEvent							
							sellSignals.add(createStrategySignal(signalItems));
						}
					}

				}
				lineNo++;
			}
			if (buySignals != null)
				generateOrderEvent(strategy, OrderType.Buy, buySignals);
			
			if (sellSignals != null)
				generateOrderEvent(strategy, OrderType.Sell, sellSignals);
			
			StringBuilder notificationMsg = new StringBuilder();
			notificationMsg = notificationMsg.append("Strategy runner for strategy ").append(strategy.getName())
					.append(" completed successfully. ").append("Buy signals=" + getSignalsString(buySignals)).append(". ")
					.append("Exceeded Buy signals=" + getSignalsString(exceededBuySignals)).append(". ")
					.append("Non Funda Buy signals=" + getSignalsString(nonFundaBuySignals)).append(". ")
					.append("Sell signals=" + getSignalsString(sellSignals));
			logger.info("Notification message = " + notificationMsg);
			eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(notificationMsg.toString()));

		} finally {
			br.close();
		}
	}

	@Override
	public void run() {
		String[] cmd = { "cscript.exe", analysisRunFilePath };
		Strategy strategy = getStrategy(strategyId);
		Process p;
		// DateFormat df = new SimpleDateFormat("d/MM/yyyy");
		try {
			Date lastTradingDate = tradingDayChecker.getLastTradingDate();
			Calendar lastTradingMaxDate = Calendar.getInstance();
			lastTradingMaxDate.setTime(lastTradingDate);
			lastTradingMaxDate.set(Calendar.HOUR_OF_DAY, 23);
			lastTradingMaxDate.set(Calendar.MINUTE, 59);
			File outFile = new File(outFilePath);
			Date outFileLastModifiedDate = new Date(outFile.lastModified());
			// String today =
			// df.format(tradingDayChecker.getLastTradingPlusOneDate());
			if (!outFileLastModifiedDate.after(lastTradingMaxDate.getTime())) {
				setAnalysisDate();
				// if(!testing){
				p = Runtime.getRuntime().exec(cmd);
				p.waitFor();
				logger.debug("Amibroker analysis out file generated");
			} else {
				logger.debug("Reusing detected Amibroker analysis out file");
			}
			// }

			processSignals(strategy);

		} catch (IOException | InterruptedException | IndexDataRetrieverException | BrokerInterfaceException
				| ParseException | FundaFilterException e) {
			logger.error("Error running analysis - " + e.getMessage());
			eventProcessor.onEvent(
					NotificationEventUtil.createNotificationEvent("Error running analysis - " + e.getMessage()));
			e.printStackTrace();
		}

	}

	@Transactional(rollbackFor = Exception.class)
	private void generateOrderEvent(Strategy strategy, OrderType orderType, List<StrategySignal> signals)
			throws BrokerInterfaceException {
		Calendar today = Calendar.getInstance();
		if (tradingDayChecker.nonTradingDay(today)) {
			logger.info("Today is a holiday, order will not be submitted.");
			eventProcessor.onEvent(
					NotificationEventUtil.createNotificationEvent("Today is a holiday, order will not be submitted."));
			return;
		}
		int availablePosition = strategy.getAvailablePosition();

		for (StrategySignal signal : signals) {
			if (orderType == OrderType.Buy) {
				if (availablePosition > 0) {
					triggerOrderEvent(strategy, orderType, signal);
				} else {
					// Queue the buy order
					OrderQueueContainer.getInstance().enQueueOrder(createOrder(strategy, orderType, signal));
					logger.debug("Send order to queue. No avalialbe position to buy " + signal.getStockName());
				}
				availablePosition--;
			} else {
				// should check whether it is existing poaition too
				try {
					portfolioDao.getStrategyPortfolioByStockName(strategy.getId(), signal.getStockName(), true);
					triggerOrderEvent(strategy, orderType, signal);
				} catch (NoResultException e) {
					logger.debug("There is not current position to sell " + signal);
				}
			}

		}
	}

	private Order createOrder(Strategy strategy, OrderType orderType, StrategySignal signal) throws BrokerInterfaceException {
		String code = brokerInterfaceManager.searchStockCode(signal.getStockName());
		if (code == null || code.trim().length() == 0)
			throw new BrokerInterfaceException("Null result searching stock code for " + signal);
		Order order = new Order();
		order.setOrderType(orderType);
		order.setStockCode(code);
		order.setStockName(signal.getStockName());
		order.setLive(strategy.isLive());
		if(signal.getOrderExecutionType() != null){
			order.setOrderExecutionType(signal.getOrderExecutionType());
			if(order.getOrderExecutionType() == OrderExecutionType.LIMIT){
				order.setPrice(signal.getClosePrice());
			}
		} else {
			OrderExecutionType orderExecutionType = orderType == OrderType.Buy ? strategy.getBuyOrderExecutionType()
					: strategy.getSellOrderExecutionType();
			order.setOrderExecutionType(orderExecutionType);
		}
		
		order.setStrategy(strategy);
		return order;
	}

	private void triggerOrderEvent(Strategy strategy, OrderType orderType, StrategySignal signal)
			throws BrokerInterfaceException {
		Log.info(String.format("Triggering %s order event for stock %s", orderType.toString(), signal));
		OrderEvent orderEvent = new OrderEvent(EventType.ORDER_GENERATED);

		orderEvent.setOrder(createOrder(strategy, orderType, signal));
		eventProcessor.onEvent(orderEvent);
	}

	public void setAnalysisFilePath(String analysisFilePath) {
		this.analysisFilePath = analysisFilePath;
	}

	public void setAnalysisRunFilePath(String analysisRunFilePath) {
		this.analysisRunFilePath = analysisRunFilePath;
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

	public void setPortfolioDao(PortfolioDao portfolioDao) {
		this.portfolioDao = portfolioDao;
	}

	public void setBrokerInterfaceManager(BrokerInterfaceManager brokerInterfaceManager) {
		this.brokerInterfaceManager = brokerInterfaceManager;
	}

	public void setTradingDayChecker(TradingDayChecker tradingDayChecker) {
		this.tradingDayChecker = tradingDayChecker;
	}

	public void setFundaFilter(FundaFilter fundaFilter) {
		this.fundaFilter = fundaFilter;
	}

}
