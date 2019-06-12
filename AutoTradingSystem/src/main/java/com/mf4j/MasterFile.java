/*
 * Decompiled with CFR 0_117.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.mf4j;

import com.mf4j.ConvertException;
import com.mf4j.MetaFile;
import com.mf4j.util.ConvertUtils;
import com.mf4j.util.MFUtils;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterFile {
    static Logger logger = LoggerFactory.getLogger((String)MetaFile.class.getName());
    public static final int MASTER_RECORD_LENGTH = 53;
    public static final int FILE_NAME_NUMBER_OFFSET = 0;
    public static final int FILE_TYPE_OFFEST = 1;
    public static final int RECORD_LENGTH_OFFSET = 3;
    public static final int RECORD_COUNT_OFFSET = 4;
    public static final int RESERVE_1_OFFSET = 5;
    public static final int ISSUE_NAME_OFFSET = 7;
    public static final int ISSUER_NAME_LENGTH = 16;
    public static final int RESERVE_2_OFFSET = 23;
    public static final int V28_FLAG_OFFSET = 24;
    public static final int FIRST_DATE_OFFSET = 25;
    public static final int LAST_DATE_OFFSET = 29;
    public static final int TIME_PERIOD_OFFSET = 33;
    public static final int IDA_TIME_OFFSET = 34;
    public static final int SYMBOL_OFFSET = 36;
    public static final int SYMBOL_LENGTH = 14;
    private static final String FILE_PREFIX = "F";
    private static final String FILE_EXT = ".DAT";
    private int fileNumber;
    private String fileType;
    private int recordLength;
    private int recordCount;
    private Date firstDate;
    private Date lastDate;//only this has to be update it seems
    private String symbol;
    private String issueName;
    private boolean v28;
    private String timePeriod;
    private int idaTime;
    private boolean autoRun;

    public MasterFile(byte[] data, int recordNumber) {
        int recordBase = (recordNumber + 1) * 53; //ignore first record
        try {
            this.fileNumber = data[recordBase + 0] & 0xFF;
            this.fileType = String.valueOf(ConvertUtils.byte2Int(data, recordBase + 1, 2));
            this.recordLength = data[recordBase + 3] & 0xFF;
            this.recordCount = data[recordBase + 4] & 0xFF;
            this.issueName = new String(data, recordBase + 7, 16).trim();
            this.v28 = data[recordBase + 24] == 89;
            this.autoRun = this.v28;
            this.firstDate = ConvertUtils.msbByte2Date(data, recordBase + 25);
            this.lastDate = ConvertUtils.msbByte2Date(data, recordBase + 29);
            this.timePeriod = new String(data, recordBase + 33, 1);
            this.idaTime = ConvertUtils.byte2Int(data, recordBase + 34, 2);
            this.symbol = new String(data, recordBase + 36, 14).trim();
        }
        catch (ConvertException e) {
            logger.error(("can't load MASTER at #" + recordNumber));
            logger.error(MFUtils.stackTrace2String(e));
        }
    }

    public int getFileNumber() {
        return this.fileNumber;
    }

    public void setFileNumber(int fileNumber) {
        this.fileNumber = fileNumber;
    }

    public String getFileName() {
        return "F" + this.fileNumber + ".DAT";
    }

    public String getFileType() {
        return this.fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getRecordLength() {
        return this.recordLength;
    }

    public void setRecordLength(int recordLength) {
        this.recordLength = recordLength;
    }

    public int getRecordCount() {
        return this.recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public Date getFirstDate() {
        return this.firstDate;
    }

    public void setFirstDate(Date firstDate) {
        this.firstDate = firstDate;
    }

    public Date getLastDate() {
        return this.lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getIssueName() {
        return this.issueName;
    }

    public void setIssueName(String issueName) {
        this.issueName = issueName;
    }

    public boolean isV28() {
        return this.v28;
    }

    public void setV28(boolean v28) {
        this.v28 = v28;
    }

    public String getTimePeriod() {
        return this.timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public int getIdaTime() {
        return this.idaTime;
    }

    public void setIdaTime(int idaTime) {
        this.idaTime = idaTime;
    }

    public boolean isAutoRun() {
        return this.autoRun;
    }

    public void setAutoRun(boolean autoRun) {
        this.autoRun = autoRun;
    }
}

