package com.yee.trading.auto.funda;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.yee.trading.auto.broker.BrokerInterfaceException;
import com.yee.trading.auto.broker.BrokerInterfaceManager;
import com.yee.trading.auto.util.TradingDayChecker;

/**
 * Based on PE (non negative or overly high) and for warrant based on maturity
 * date (could be checking mother's PE as well)
 * 
 * @author CzeHoul
 * 
 */
@Component("basicFundaFilter")
public class BasicFundaFilter implements FundaFilter {
	@Autowired
	private FundaDataRetrieval fundaDataRetrieval;

	@Value("${pe.min:1}")
	private int minPE;
	
	@Value("${qr.release.stop:7}")
	private int qrReleaseStopPeriod;

	@Value("${pe.max:45}")
	private int maxPE;

	@Value("${pe.filter.enable:false}")
	private boolean enablePEFilter;

	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	@Value("${warrant.max.expired.days:550}")
	private int maxExpiredDays;

	@Autowired
	@Qualifier("hLeBrokingInterfaceManager")
	private BrokerInterfaceManager brokerInterfaceManager;
	
	@Autowired
	private TradingDayChecker tradingDayChecker;

	@Override
	public boolean validateFunda(String stockName) throws FundaFilterException {
		try {
			String stockCode = brokerInterfaceManager
					.searchStockCode(stockName);
			StockFundaDetails fundaDetails = fundaDataRetrieval
					.retrieveFundaData(stockCode);
			// Stock
			if (fundaDetails.getWarrant() == null) {
				boolean allowTrade = !releaseImmediateQR(fundaDetails);
				return validatePE(fundaDetails.getStock().getPE()) && allowTrade;
			} else { // Warrant
				StockFundaDetails motherFundaDetails = fundaDataRetrieval
						.retrieveFundaData(stockCode.substring(0, 4));
				boolean allowTrade = !releaseImmediateQR(motherFundaDetails);
				boolean motherValid = validatePE(motherFundaDetails.getStock()
						.getPE()) && allowTrade;

				Date maturityDate = df.parse(fundaDetails.getWarrant()
						.getWarrants().getMaturity_date());
				Date today = new Date();
				long diff = Math.abs(maturityDate.getTime() - today.getTime());
				long diffDays = diff / (24 * 60 * 60 * 1000);
				return motherValid && diffDays > maxExpiredDays;
			}

		} catch (FundaDataRetrievalException re) {
			throw new FundaFilterException("Error retrieving funda data - "
					+ re.getMessage());
		} catch (ParseException pe) {
			throw new FundaFilterException("Error processing funda data - "
					+ pe.getMessage());
		} catch (BrokerInterfaceException e) {
			throw new FundaFilterException("Error retrieving funda data - "
					+ e.getMessage());
		}
	}

	private boolean validatePE(String pe) {
		if (enablePEFilter) {
			int peInt = new Integer(pe).intValue();
			return (peInt > minPE && peInt < maxPE);
		} else {
			return true;
		}
	}
	
	private boolean releaseImmediateQR(StockFundaDetails fundaDetails) throws ParseException{
		FinancialReport[] financialReports = fundaDetails.getFinancialReport();
		if(financialReports.length > 0){
			
			Date today = new Date();
			Date announceDate = df.parse(financialReports[0].getAnnounced_date());			
			if(DateUtils.isSameDay(tradingDayChecker.getLastTradingDate(), announceDate)) {
				return true;
			} else {
				//greater than 20 days from last ann date
				//within plus minus 14 days of est amm date
				//otherwise not release immediate qr
				Calendar estNextAnnounceDate = Calendar.getInstance();
				estNextAnnounceDate.setTime(announceDate);
				estNextAnnounceDate.add(Calendar.MONTH, 3);
				
				long miliSecondFromLastQR = today.getTime() - announceDate.getTime();				
				long dayFromLastQR = miliSecondFromLastQR / (24 * 60 * 60 * 1000);
				
				long miliSecondFromEstQR = estNextAnnounceDate.getTimeInMillis() - today.getTime();				
				long dayFromEstQR = miliSecondFromEstQR / (24 * 60 * 60 * 1000);
				
				if(dayFromLastQR > 60 && dayFromEstQR < qrReleaseStopPeriod){
					return true;					
				} else {
					return false;
				}
					
			}
			
		} else {
			return false;
		}
	}
}
