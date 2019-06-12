/*
 * Decompiled with CFR 0_117.
 */
package com.mf4j;

public class Fields {
    private boolean timeField;
    private boolean openInterest;
    private boolean openPrice;
    private boolean highField;
    private boolean lowField;

    public Fields(byte field) {
        this.timeField = (field & 128) == 128;
        this.openInterest = (field & 64) == 64;
        this.openPrice = (field & 32) == 32;
        this.highField = (field & 16) == 16;
        this.lowField = (field & 8) == 8;
    }

    public boolean isTimeField() {
        return this.timeField;
    }

    public void setTimeField(boolean timeField) {
        this.timeField = timeField;
    }

    public boolean isOpenInterest() {
        return this.openInterest;
    }

    public void setOpenInterest(boolean openInterest) {
        this.openInterest = openInterest;
    }

    public boolean isOpenPrice() {
        return this.openPrice;
    }

    public void setOpenPrice(boolean openPrice) {
        this.openPrice = openPrice;
    }

    public boolean isHighField() {
        return this.highField;
    }

    public void setHighField(boolean highField) {
        this.highField = highField;
    }

    public boolean isLowField() {
        return this.lowField;
    }

    public void setLowField(boolean lowField) {
        this.lowField = lowField;
    }
}

