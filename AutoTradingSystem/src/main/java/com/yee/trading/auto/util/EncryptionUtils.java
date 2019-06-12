package com.yee.trading.auto.util;
import java.nio.charset.Charset;

public class EncryptionUtils
{
    public static final String EN_KEY = "ExcelForce";
    
    public static String EncryptPin(String s) {
        final String s2 = "";
        final String upperCase = s.toUpperCase();
        final int length = upperCase.length();
        int i = 0;
        s = s2;
        while (i < length) {
            final String value = String.valueOf(upperCase.charAt(i) + '\u0007');
            System.out.println("hex: " + value);
            final String s3 = s += value;
            if (value.length() < 3) {
                s = s3 + "*";
            }
            ++i;
        }
        final int length2 = s.length();
        int n2;
        final int n = n2 = length2 / 2;
        if (length2 % 2 != 0) {
            n2 = n;
            if (n % 2 != 0) {
                n2 = n + 1;
            }
        }
        final int n3 = n2 - 1;
        final String string = new StringBuilder(left(s, n3)).reverse().toString();
        s = new StringBuilder(mid(s, n3)).reverse().toString();
        return string + s;
    }
    
    public static String EncryptStr(String s, String s2) {
        try {
            final String upperCase = s.toUpperCase();
            final Charset forName = Charset.forName("US-ASCII");
            final byte[] bytes = s2.getBytes(forName);
            s = "";
            for (int length = bytes.length, i = 0; i < length; ++i) {
                s += Byte.toString(Byte.valueOf(bytes[i]));
            }
            final byte[] bytes2 = s.getBytes(forName);
            final byte[] bytes3 = upperCase.getBytes(forName);
            s = "";
            for (int j = 0; j < bytes3.length; ++j) {
                final String s3 = s2 = Integer.toHexString(Integer.parseInt(Byte.toString(bytes2[j])) ^ Integer.parseInt(Byte.toString(bytes3[j])));
                if (s3.length() < 2) {
                    s2 = "0" + s3;
                }
                s += s2;
            }
            s = s.toUpperCase();
            return s;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static String left(final String s, final int n) {
        return s.substring(0, Math.min(n, s.length()));
    }
    
    public static String mid(final String s, final int n) {
        return s.substring(n, s.length());
    }
}
