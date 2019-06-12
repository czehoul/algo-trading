/*
 * Decompiled with CFR 0_117.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.mf4j;

import com.mf4j.ConvertException;
import com.mf4j.Fields;
import com.mf4j.util.ConvertUtils;
import com.mf4j.util.MFUtils;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMasterFile {
    static Logger logger = LoggerFactory.getLogger((String)XMasterFile.class.getName());
    public static final int XMASTER_RECORD_LENGTH = 150;
    public static final int FILE_NAME_NUMBER_OFFSET = 65;
    public static final int ISSUE_NAME_OFFSET = 16;
    public static final int ISSUER_NAME_LENGTH = 23;
    public static final int FIRST_DATE_OFFSET = 104;
    public static final int LAST_DATE_OFFSET = 108;
    public static final int TIME_PERIOD_OFFSET = 62;
    public static final int SYMBOL_OFFSET = 1;
    public static final int FIELDS_OFFSET = 70;
    public static final int SYMBOL_LENGTH = 14;
    private static final String FILE_PREFIX = "F";
    private static final String FILE_EXT = ".MWD";
    private int fileNumber;
    private int recordLength;
    private int recordCount;
    private Date firstDate;
    private Date lastDate;
    private String symbol;
    private String issueName;
    private String timePeriod;
    private Fields xmFields;

    public XMasterFile(byte[] data, int recordNumber) {
        int recordBase = (recordNumber + 1) * 150;
        try {
            this.fileNumber = ConvertUtils.byte2Int(data, recordBase + 65, 2);
            this.xmFields = new Fields(data[recordBase + 70]);
            this.recordLength = 28;
            this.recordCount = 7;
            //this.issueName = new String(data, recordBase + 16, 23).trim();
            this.issueName = ConvertUtils.byte2StringDelimeterX(data, recordBase + 16, 23).trim();
            this.firstDate = ConvertUtils.byte2DateX(data, recordBase + 104);
            this.lastDate = ConvertUtils.byte2DateX(data, recordBase + 108);
            this.timePeriod = new String(data, recordBase + 62, 1);
            this.symbol = ConvertUtils.byte2StringDelimeterX(data, recordBase + 1, 14).trim();            
            
        }
        catch (ConvertException e) {
            logger.error(("can't load XMASTER at #" + recordNumber));
            logger.error(MFUtils.stackTrace2String(e));
        }
    }

    public String getFileName() {
        return "F" + this.fileNumber + ".MWD";
    }

    public int getFileNumber() {
        return this.fileNumber;
    }

    public void setFileNumber(int fileNumber) {
        this.fileNumber = fileNumber;
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

    public String getTimePeriod() {
        return this.timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public Fields getXmFields() {
        return this.xmFields;
    }

    public void setXmFields(Fields xmFields) {
        this.xmFields = xmFields;
    }
}

