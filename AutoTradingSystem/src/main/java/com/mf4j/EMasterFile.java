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

public class EMasterFile {
    static Logger logger = LoggerFactory.getLogger((String)EMasterFile.class.getName());
    public static final int EMASTER_RECORD_LENGTH = 192;
    public static final int ID_CODE_OFFSET = 0;
    public static final int FILE_NAME_NUMBER_OFFSET = 2;
    public static final int RECORD_COUNT_OFFSET = 6;
    public static final int FIELDS_OFFSET = 7;
    public static final int AUTORUN_OFFSET = 9;
    public static final int SYMBOL_OFFSET = 11;
    public static final int SYMBOL_LENGTH = 13;
    public static final int ISSUE_NAME_OFFSET1 = 32;
    public static final int ISSUE_NAME_LENGTH1 = 16;
    public static final int ISSUE_NAME_OFFSET2 = 139;
    public static final int ISSUE_NAME_LENGTH2 = 45;
    public static final int TIME_PERIOD_OFFSET = 60;
    public static final int FIRST_DATE_OFFSET = 64;
    public static final int LAST_DATE_OFFSET = 72;
    private static final String FILE_PREFIX = "F";
    private static final String FILE_EXT = ".DAT";
    private int idCode;
    private int fileNumber;
    private String fileType;
    private int recordCount;
    private Date firstDate;
    private Date lastDate;
    private String symbol;
    private String issueName;
    private String timePeriod;
    private boolean autoRun;
    private Fields emFields;

    public EMasterFile(byte[] data, int recordNumber) {
        int recordBase = (recordNumber + 1) * 192;
        try {
            this.idCode = data[recordBase + 0];
            this.fileNumber = data[recordBase + 2];
            this.recordCount = data[recordBase + 6];
            this.emFields = new Fields(data[recordBase + 7]);
            this.autoRun = data[recordBase + 9] == 42;
            this.symbol = new String(data, recordBase + 11, 13).trim();
            this.issueName = new String(data, recordBase + 32, 16).trim();
            String tmp = new String(data, recordBase + 139, 45).trim();
            if (!tmp.equals("")) {
                this.issueName = tmp;
            }
            this.timePeriod = new String(data, recordBase + 60, 1);
            this.firstDate = ConvertUtils.byte2Date(data, recordBase + 64);
            this.lastDate = ConvertUtils.byte2Date(data, recordBase + 72);
        }
        catch (ConvertException e) {
            logger.error(("can't load EMASTER at #" + recordNumber));
            logger.error(MFUtils.stackTrace2String(e));
        }
    }

    public int getIdCode() {
        return this.idCode;
    }

    public void setIdCode(int idCode) {
        this.idCode = idCode;
    }

    public int getFileNumber() {
        return this.fileNumber;
    }

    public void setFileNumber(int fileNumber) {
        this.fileNumber = fileNumber;
    }

    public String getFileType() {
        return this.fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
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

    public boolean isAutoRun() {
        return this.autoRun;
    }

    public void setAutoRun(boolean autoRun) {
        this.autoRun = autoRun;
    }

    public Fields getEmFields() {
        return this.emFields;
    }

    public void setEmFields(Fields emFields) {
        this.emFields = emFields;
    }

    public String getFileName() {
        return "F" + this.fileNumber + ".DAT";
    }
}

