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
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.broker.BrokerInterfaceException;
import com.yee.trading.auto.broker.BrokerInterfaceManager;
import com.yee.trading.auto.dao.StrategyDao;
import com.yee.trading.auto.event.EventProcessor;
import com.yee.trading.auto.marketdata.index.IndexDataRetriever;
import com.yee.trading.auto.marketdata.index.IndexDataRetrieverException;
import com.yee.trading.auto.marketdata.index.Quote;
import com.yee.trading.auto.util.NotificationEventUtil;
import com.yee.trading.auto.util.TradingDayChecker;

//Configure this spring bean in spring context xml
public class IntradayOpenStrategyRunner implements StrategyRunner {
	private String analysisFilePath;
	private String analysisRunFilePath;
	private String outFilePath;
	private int strategyId;
	private EventProcessor eventProcessor;
	private final Logger logger = LoggerFactory.getLogger(IntradayOpenStrategyRunner.class);
	private StrategyDao strategyDao;
	// private PortfolioDao portfolioDao;
	private BrokerInterfaceManager brokerInterfaceManager;
	private TradingDayChecker tradingDayChecker;	
	private IndexDataRetriever indexDataRetriever;
	private IntradayOpenStrategyHandler intradayOpenStrategyHandler;
	
	
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

	private boolean isMarketSafe(int bullLevel, Quote quote) throws ParseException {
		DateFormat indexDataDf = new SimpleDateFormat("M/d/yyyy");

		if (bullLevel == 4) {
			return false;
		}
		Date quoteLastTradeDate = null;
		quoteLastTradeDate = indexDataDf.parse(quote.getLastTradeDate());
		if (tradingDayChecker.getLastTradingDate().after(quoteLastTradeDate)) {
			return true;
		} else {
			Float indexChange = new Float(quote.getPercentChange().substring(0, quote.getPercentChange().length() - 1));
			if ((bullLevel == 1 && indexChange > -0.5f) || (bullLevel == 2 && indexChange > -0.25f)
					|| (bullLevel == 3 && indexChange > -0.1f)) {
				return true;
			} else {
				return false;
			}
		}
	}

	private void processSignals(Strategy strategy) throws IOException, IndexDataRetrieverException, BrokerInterfaceException, ParseException {

		BufferedReader br = new BufferedReader(new FileReader(new File(outFilePath)));
		try {
			String line = null;
			int lineNo = 0;
			List<String> signals = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				if (lineNo > 0) {
					String[] signalItems = line.split(",");
					IntradayOpenSignal intradayOpenSignal = new IntradayOpenSignal();
					intradayOpenSignal.setStockName(signalItems[0]);
					signals.add(signalItems[0]);
					intradayOpenSignal.setStockCode(brokerInterfaceManager.searchStockCode(intradayOpenSignal.getStockName()));
					intradayOpenSignal.setPreviousClosePrice(new BigDecimal(signalItems[2]));
					intradayOpenSignal.setMarketBullLevel(new Integer(signalItems[3].substring(0, signalItems[3].indexOf("."))));
					intradayOpenSignal.setStrategy(strategy);
					
					if (lineNo == 1) {

						if (intradayOpenSignal.getMarketBullLevel() < 4) {
							Quote quoteData = indexDataRetriever.retrieveIndexData("GSPC").getQuery().getResults()
									.getQuote();
							if (!isMarketSafe(intradayOpenSignal.getMarketBullLevel(), quoteData)) {
								return;
							}

						} else {
							return;
						}
					}
					
					intradayOpenStrategyHandler.handleStrategy(intradayOpenSignal);					
				}
				lineNo++;
			}
			StringBuilder notificationMsg = new StringBuilder();
			notificationMsg = notificationMsg.append("Strategy runner for strategy ").append(strategy.getName())
					.append(" completed successfully. ").append("Buy signals=" + signals).append(". ");
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
		try {			
			setAnalysisDate();
			// if(!testing){
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			// }
			logger.debug("Amibroker analysis out file generated");
			processSignals(strategy);

		} catch (IOException | InterruptedException | IndexDataRetrieverException | BrokerInterfaceException | ParseException e) {
			logger.error("Error running analysis - " + e.getMessage());
			eventProcessor.onEvent(
					NotificationEventUtil.createNotificationEvent("Error running analysis - " + e.getMessage()));
			e.printStackTrace();
		}

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

	public void setIndexDataRetriever(IndexDataRetriever indexDataRetriever) {
		this.indexDataRetriever = indexDataRetriever;
	}

	public void setBrokerInterfaceManager(BrokerInterfaceManager brokerInterfaceManager) {
		this.brokerInterfaceManager = brokerInterfaceManager;
	}

	public void setTradingDayChecker(TradingDayChecker tradingDayChecker) {
		this.tradingDayChecker = tradingDayChecker;
	}

	public void setIntradayOpenStrategyHandler(IntradayOpenStrategyHandler intradayOpenStrategyHandler) {
		this.intradayOpenStrategyHandler = intradayOpenStrategyHandler;
	}

	public void setAnalysisRunFilePath(String analysisRunFilePath) {
		this.analysisRunFilePath = analysisRunFilePath;
	}

	

	
}
