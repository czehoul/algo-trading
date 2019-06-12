package com.yee.trading.auto.marketdata.index;

public class Quote
{
    private String Open;    

    private String LastTradePriceOnly;

    private String LastTradeDate;

    private String PreviousClose;
    
    private String PercentChange;
    
    private String Volume;

    private String Symbol;

	public String getOpen() {
		return Open;
	}

	public void setOpen(String open) {
		Open = open;
	}

	public String getLastTradePriceOnly() {
		return LastTradePriceOnly;
	}

	public void setLastTradePriceOnly(String lastTradePriceOnly) {
		LastTradePriceOnly = lastTradePriceOnly;
	}

	public String getLastTradeDate() {
		return LastTradeDate;
	}

	public void setLastTradeDate(String lastTradeDate) {
		LastTradeDate = lastTradeDate;
	}

	public String getPreviousClose() {
		return PreviousClose;
	}

	public void setPreviousClose(String previousClose) {
		PreviousClose = previousClose;
	}

	public String getPercentChange() {
		return PercentChange;
	}

	public void setPercentChange(String percentChange) {
		PercentChange = percentChange;
	}

	public String getVolume() {
		return Volume;
	}

	public void setVolume(String volume) {
		Volume = volume;
	}

	public String getSymbol() {
		return Symbol;
	}

	public void setSymbol(String symbol) {
		Symbol = symbol;
	}

    
}
	