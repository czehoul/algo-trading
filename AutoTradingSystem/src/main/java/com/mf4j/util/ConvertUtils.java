/*
 * Decompiled with CFR 0_117.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.mf4j.util;

import com.mf4j.ConvertException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvertUtils {
    static Logger logger = LoggerFactory.getLogger((String)ConvertUtils.class.getName());
    protected static final int BYTE_MASK = 255;
    protected static final int MANTISSA_MASK = 8388607;
    protected static final int EXPONENT_MASK = 8454143;
    protected static final int SIGN_MASK = 128;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    public static int byte2Int(byte[] b, int offset, int length) throws ConvertException {
        int result = 0;
        if (offset < 0) {
            throw new ConvertException("offset must greater than or equal zero;[offset=" + offset + "]");
        }
        if (length < 1 || length > 4) {
            throw new ConvertException("length must between 1 and 4;[length=" + length + "]");
        }
        if (offset + length > b.length) {
            throw new ConvertException("offset+length exceed size of b[];[offset=" + offset + ", length=" + length + ", b.length=" + b.length + "]");
        }
        int i = 0;
        while (i < length) {
            int shift = 8 * i;
            result = (int)((long)result + ((long)(b[offset + i] & 255) << shift));
            ++i;
        }
        return result;
    }
    
    public static byte[] shortInt2Byte(int data){
    	return new byte[]{
    		(byte)data,
    		(byte)(data >>> 8)  		
    	};
    	
    }

    public static byte[] int2Byte(int data){
    	return new byte[]{
    		(byte)data,
    		(byte)(data >>> 8),
    		(byte)(data >>> 16),
    		(byte)(data >>> 24)    		
    	};
    	
    }
    
    public static float msbByte2Float(byte[] b, int offset) throws ConvertException {
        if (offset < 0) {
            throw new ConvertException("offset must greater than or equal zero;[offset=" + offset + "]");
        }
        if (offset + 4 > b.length) {
            throw new ConvertException("offset exceed size of b[];[offset=" + offset + ", b.length=" + b.length + "]");
        }
        long intOne = b[offset] & 255;
        long intTwo = b[offset + 1] & 255;
        long intThree = b[offset + 2] & 255;
        long intFour = b[offset + 3] & 255;
        if (intOne == 0 && intTwo == 0 && intThree == 0 && intFour == 0) {
            return 0.0f;
        }
        long msf = intFour << 24 | intThree << 16 | intTwo << 8 | intOne;
        int mantissa = (int)(msf & 0x7FFFFF);
        int exponent = (int)(msf >> 24 & 8454143) - 2;
        int sign = (int)(msf >> 16) & 128;
        return Float.intBitsToFloat(mantissa |= exponent << 23 | sign << 24);
    }

    public static final byte[] float2MsbByte(float value) {
        byte[] result = new byte[4];
        int bits = Float.floatToIntBits(value);
        String bitStr = Integer.toBinaryString(bits);
        bitStr = StringUtils.leftPad(bitStr, bitStr.length() + (32 - bitStr.length()), '0');
        String mbf = String.valueOf(bitStr.substring(1, 9)) + bitStr.substring(0, 1) + bitStr.substring(9);
        long bitLong = Long.parseLong(mbf, 2);
        bitStr = Long.toBinaryString(bitLong);
        bitStr = StringUtils.leftPad(bitStr, bitStr.length() + (32 - bitStr.length()), '0');
        result[0] = (byte)Long.parseLong(bitStr.substring(24, 32), 2);
        result[1] = (byte)Long.parseLong(bitStr.substring(16, 24), 2);
        result[2] = (byte)Long.parseLong(bitStr.substring(8, 16), 2);
        result[3] = (byte)Long.parseLong(bitStr.substring(0, 8), 2);
        byte[] arrby = result;
        arrby[3] = (byte)(arrby[3] + 2);
        return result;
    }

    public static byte[] float2Byte(float afloat) throws ConvertException {
    	int intData = (int)afloat;
    	return int2Byte(intData);
    	
    }
    public static final float byte2Float(byte[] b, int offset) throws ConvertException {
        if (offset < 0) {
            throw new ConvertException("offset must greater than or equal zero;[offset=" + offset + "]");
        }
        if (offset + 4 > b.length) {
            throw new ConvertException("offset exceed size of b[];[offset=" + offset + ", b.length=" + b.length + "]");
        }
        byte[] tmp = new byte[4];
        int i = 0;
        while (i < 4) {
            tmp[i] = b[offset + i];
            ++i;
        }
        int accum = 0;
        int i2 = 0;
        int shiftBy = 0;
        while (shiftBy < 32) {
            accum = (int)((long)accum | (long)(tmp[i2] & 255) << shiftBy);
            ++i2;
            shiftBy += 8;
        }
        return Float.intBitsToFloat(accum);
    }

    public static Date msbByte2Date(byte[] b, int offset) throws ConvertException {
        if (offset < 0) {
            throw new ConvertException("offset must greater than or equal zero;[offset=" + offset + "]");
        }
        if (offset + 4 > b.length) {
            throw new ConvertException("offset exceed size of b[];[offset=" + offset + ", b.length=" + b.length + "]");
        }
        return ConvertUtils.float2Date(ConvertUtils.msbByte2Float(b, offset));
    }

    public static Date float2Date(float f) {
        int year;
        int month;
        int date;
        int d = (int)f;
        if (f > 0.0f) {
            date = d % 100;
            month = (d /= 100) % 100 - 1;
            year = 1900 + (d /= 100);            
        } else {
            date = (d += 1000000) % 100;
            month = (d /= 100) % 100 - 1;
            d = (int)f;
            d /= 100;
            year = 1900 + (d /= 100);
        }
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, date, 0, 0, 0);
        return cal.getTime();
    }
    
    public static float Date2Float(Date date) {
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(date);
    	int day = cal.get(Calendar.DAY_OF_MONTH);
    	int month = cal.get(Calendar.MONTH) + 1;
    	int year = cal.get(Calendar.YEAR)-1900;
    	String floatStr = String.format("%d%02d%02d.0", year, month, day);
    	return new Float(floatStr);
    }

    public static Date int2DateX(int input) {
        if (input == 0) {
            return null;
        }
        int d = input;
        int date = d % 100;
        int month = (d /= 100) % 100 - 1;
        int year = d /= 100;
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, date, 0, 0);
        return cal.getTime();
    }
    
    public static int Date2IntX(Date date) {
    	return Integer.parseInt(dateFormat.format(date));    	
    }
    
    public static byte[] date2Byte(Date date) throws ConvertException {
    	return ConvertUtils.float2Byte(ConvertUtils.Date2Float(date));
    }

    public static Date byte2Date(byte[] b, int offset) throws ConvertException {
        if (offset < 0) {
            throw new ConvertException("offset must greater than or equal zero;[offset=" + offset + "]");
        }
        if (offset + 4 > b.length) {
            throw new ConvertException("offset exceed size of b[];[offset=" + offset + ", b.length=" + b.length + "]");
        }
        return ConvertUtils.float2Date(ConvertUtils.byte2Float(b, offset));
    }

    public static Date byte2DateX(byte[] b, int offset) throws ConvertException {
        if (offset < 0) {
            throw new ConvertException("offset must greater than or equal zero;[offset=" + offset + "]");
        }
        if (offset + 4 > b.length) {
            throw new ConvertException("offset exceed size of b[];[offset=" + offset + ", b.length=" + b.length + "]");
        }
        return ConvertUtils.int2DateX(ConvertUtils.byte2Int(b, offset, 4));
    }

    public static byte[] date2ByteX(Date date) throws ConvertException {
        if (date == null) {
            throw new ConvertException("Date is null");
        }
        
        return ConvertUtils.int2Byte(Date2IntX(date));
        		
    }
    
    public static String byte2StringX(byte[] b, int offset, int length) throws ConvertException {
        if (offset < 0) {
            throw new ConvertException("offset must greater than or equal zero;[offset=" + offset + "]");
        }
        if (offset + length > b.length) {
            throw new ConvertException("offset exceed size of b[];[offset=" + offset + ", b.length=" + b.length + "]");
        }
        StringBuffer result = new StringBuffer();
        int c = 0;
        while (c < length) {
            if (b[offset + c] != 0) {
                result.append(b[offset + c]);
            }
            ++c;
        }
        return result.toString().trim();
    }
    
    public static String byte2StringDelimeterX(byte[] b, int offset, int length) throws ConvertException {
        if (offset < 0) {
            throw new ConvertException("offset must greater than or equal zero;[offset=" + offset + "]");
        }
        if (offset + length > b.length) {
            throw new ConvertException("offset exceed size of b[];[offset=" + offset + ", b.length=" + b.length + "]");
        }
        byte[] destBytes = new byte[length];
        int c = 0;
        while (c < length) {
            if (b[offset + c] != 0) {
            	destBytes[c] = b[offset + c];
            } else {
            	break;
            }
            ++c;
        }        
        return new String(destBytes, 0, c);
    }
}

