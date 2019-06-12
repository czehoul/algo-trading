package com.yee.trading.auto.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("tradingDayChecker")
public class TradingDayChecker {
	private String[] holidays;
	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	public TradingDayChecker(@Value("${holiday.list}") String holidayList) {
		holidays = holidayList.split(",");
	}
	
	public Date getLastTradingDate(){
		Calendar calendar = getLocalDate();
		//default to yesterday
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		while(nonTradingDay(calendar)){
			calendar.add(Calendar.DAY_OF_MONTH, -1);
		}
		return calendar.getTime();
		//check whether they are in the list, loop here until not holiday, need to consider weekend too
	}
	
	public Date getLastTradingPlusOneDate(){
		Date nonTradingDate = getLastTradingDate();
		Calendar calendar = getLocalDate();
		calendar.setTime(nonTradingDate);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
			calendar.add(Calendar.DAY_OF_MONTH, 2);
		}else if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		return calendar.getTime();
	}

	private Calendar getLocalDate(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);  
		calendar.set(Calendar.MINUTE, 0);  
		calendar.set(Calendar.SECOND, 0);  
		calendar.set(Calendar.MILLISECOND, 0); 
		return calendar;
		
	}
	
	public boolean nonTradingDay(Calendar calendar){
		String dateStr = dateFormat.format(calendar.getTime());
		for(String holiday : holidays){
			if(dateStr.equals(holiday))
				return true;
		}
		if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || 
				calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
			return true;
		}
		return false;
	}
	
}
