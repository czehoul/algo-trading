/*
 * Decompiled with CFR 0_117.
 */
package com.mf4j.util;

import java.util.Calendar;
import java.util.Date;

public class MFUtils {
    public static String stackTrace2String(Exception ex) {
        StackTraceElement[] stackTraceElements = ex.getStackTrace();
        StringBuffer buffer = new StringBuffer();
        buffer.append(ex.getMessage());
        buffer.append(System.getProperty("line.separator"));
        int i = 0;
        while (i < stackTraceElements.length) {
            buffer.append("\tat ");
            buffer.append(stackTraceElements[i]);
            buffer.append(System.getProperty("line.separator"));
            ++i;
        }
        return buffer.toString();
    }

    public static Date getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(5, day);
        cal.set(2, month);
        cal.set(1, year);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        return cal.getTime();
    }
}

