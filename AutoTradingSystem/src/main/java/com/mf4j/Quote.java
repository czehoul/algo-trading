/*
 * Decompiled with CFR 0_117.
 */
package com.mf4j;

import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public class Quote {
    private static int decimalPrecision = 4;
    private Date date;
    private float time;
    private float high;
    private float low;
    private float open;
    private float close;
    private long openInterest;
    private long volume;
    private String dateString;

    public Quote(Date date, float time, float high, float low, float open, float close, long openInterest, long volume) {
        int power = decimalPrecision;
        float f = (float)Math.pow(10.0, power);
        this.date = date;
        this.time = time;
        this.high = (float)Math.round(high * f) / f;
        this.low = (float)Math.round(low * f) / f;
        this.open = (float)Math.round(open * f) / f;
        this.close = (float)Math.round(close * f) / f;
        this.openInterest = openInterest;
        this.volume = volume;
    }

    public Quote(Date date, float high, float low, float open, float close, long openInterest, long volume) {
        int power = decimalPrecision;
        float f = (float)Math.pow(10.0, power);
        this.date = date;
        this.high = (float)Math.round(high * f) / f;
        this.low = (float)Math.round(low * f) / f;
        this.open = (float)Math.round(open * f) / f;
        this.close = (float)Math.round(close * f) / f;
        this.openInterest = openInterest;
        this.volume = volume;
    }
    
   
   @Override    
    public boolean equals(Object other) {
        if (!(other instanceof Quote)) {
            return false;
        }

        Quote that = (Quote) other;

        // Custom equality check here.
        return DateUtils.isSameDay(this.getDate(), that.getDate());
    }
    
    public static Comparator<Quote> getCompByDate()
    {   
     Comparator<Quote> comp = new Comparator<Quote>(){
         @Override
         public int compare(Quote q1, Quote q2)
         {
             int result = 0;
             if(DateUtils.isSameDay(q1.getDate(), q2.getDate())){
            	 result = 0;
             } else if(q1.getDate().before(q2.getDate())){
            	 result = -1;
             } else {
            	 result = 1; 
             }
        	 return result;
         }        
     };
     return comp;
    }  

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getTime() {
        return this.time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public float getHigh() {
        return this.high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public float getLow() {
        return this.low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public float getOpen() {
        return this.open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    public float getClose() {
        return this.close;
    }

    public void setClose(float close) {
        this.close = close;
    }

    public long getOpenInterest() {
        return this.openInterest;
    }

    public void setOpenInterest(long openInterest) {
        this.openInterest = openInterest;
    }

    public long getVolume() {
        return this.volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public static int getDecimalPrecision() {
        return decimalPrecision;
    }

    public static void setDecimalPrecision(int decimalPrecision) {
        Quote.decimalPrecision = decimalPrecision;
    }

	public String getDateString() {
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}
    
}

