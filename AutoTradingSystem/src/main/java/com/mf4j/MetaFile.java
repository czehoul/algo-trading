/*
 * Decompiled with CFR 0_117.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.mf4j;

import com.mf4j.ConvertException;
import com.mf4j.EMasterFile;
import com.mf4j.Fields;
import com.mf4j.MasterFile;
import com.mf4j.Quote;
import com.mf4j.UnsupportedMetaFileException;
import com.mf4j.XMasterFile;
import com.mf4j.util.ConvertUtils;
import com.mf4j.util.MFUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaFile {
    static Logger logger = LoggerFactory.getLogger((String)MetaFile.class.getName());
    public static final int CSV_DATE = 1;
    public static final int CSV_NAME = 2;
    public static final int CSV_SYMBOL = 3;
    public static final int CSV_OPEN = 4;
    public static final int CSV_CLOSE = 5;
    public static final int CSV_HIGH = 6;
    public static final int CSV_LOW = 7;
    public static final int CSV_OPENINTEREST = 8;
    public static final int CSV_VOLUME = 9;
    public static final int TIME_PERIOD_IDA = 1;
    public static final int TIME_PERIOD_DAY = 2;
    public static final int TIME_PERIOD_WEEK = 3;
    public static final int TIME_PERIOD_MONTH = 4;
    public static final int TIME_PERIOD_QUARTER = 5;
    public static final int TIME_PERIOD_YEAR = 6;
    private String fileName = "";
    private String name = "";
    private String symbol = "";
    private int quoteCount = -1;
    private Date firstDate;
    private Date lastDate;
    private int fileNumber;
    private ArrayList<Quote> quotes;
    private int recordLength = 0;
    private int recordCount = -1;
    private int timePeriod;
    private Fields fields;
    private int masterFileIndex;
    private MasterFileType masterFileType;
    private boolean newRecord;

    public MetaFile(String fileName, MasterFile masterFile, EMasterFile emasterFile) {
        this.fileName = fileName;
        if(emasterFile != null)
        	this.name = emasterFile.getIssueName();
        else
        	this.name = masterFile.getIssueName();
        this.symbol = masterFile.getSymbol();
        this.firstDate = masterFile.getFirstDate();
        this.lastDate = masterFile.getLastDate();
        this.recordLength = masterFile.getRecordLength();
        this.recordCount = masterFile.getRecordCount();
        this.fileNumber = masterFile.getFileNumber();
        switch (masterFile.getTimePeriod().charAt(0)) {
            case 'I': {
                this.timePeriod = 1;
                break;
            }
            case 'W': {
                this.timePeriod = 3;
                break;
            }
            case 'Q': {
                this.timePeriod = 5;
                break;
            }
            case 'D': {
                this.timePeriod = 2;
                break;
            }
            case 'M': {
                this.timePeriod = 4;
                break;
            }
            case 'Y': {
                this.timePeriod = 6;
            }
        }
        if(emasterFile != null) {
        	this.fields = emasterFile.getEmFields();
        } else {
        	this.fields = new Fields((byte)127);
        }
    }

    public MetaFile(String fileName, XMasterFile xmasterFile) {
        this.fileName = fileName;
        this.name = xmasterFile.getIssueName();
        this.symbol = xmasterFile.getSymbol();
        this.firstDate = xmasterFile.getFirstDate();
        this.lastDate = xmasterFile.getLastDate();
        this.recordLength = xmasterFile.getRecordLength();
        this.recordCount = xmasterFile.getRecordCount();
        this.fileNumber = xmasterFile.getFileNumber();
        switch (xmasterFile.getTimePeriod().charAt(0)) {
            case 'I': {
                this.timePeriod = 1;
                break;
            }
            case 'W': {
                this.timePeriod = 3;
                break;
            }
            case 'Q': {
                this.timePeriod = 5;
                break;
            }
            case 'D': {
                this.timePeriod = 2;
                break;
            }
            case 'M': {
                this.timePeriod = 4;
                break;
            }
            case 'Y': {
                this.timePeriod = 6;
            }
        }
        this.fields = xmasterFile.getXmFields();
    }

    public void loadQuotes() throws Exception {
        try {
        	File dataFile = new File(this.fileName);
        	if(!dataFile.exists()){
        		// dataFile = new File(permuteFileName(this.fileName));
        		// if(!dataFile.exists()){
        			 logger.info("File ".concat(this.fileName).concat(" not found."));
        			 throw new IOException("File ".concat(this.fileName).concat(" not found."));        			 
        		// }
        	}
            byte[] buffer = new byte[(int)dataFile.length()];            
           
            BufferedInputStream f = new BufferedInputStream(new FileInputStream(dataFile));
            f.read(buffer);
            f.close();
            this.quoteCount = buffer.length / this.recordLength - 1;
            float[] data = new float[Math.min(this.recordCount, 8)];
            if (this.quoteCount > 0) {
                this.quotes = new ArrayList();
                int i = this.recordLength;
                while (i <= buffer.length - 1) {//for each row
                    int c = 0;
                    while (c <= this.recordCount - 1) {//populate each column in a row
                        data[c] = ConvertUtils.msbByte2Float(buffer, i + c * 4);
                        ++c;
                    }
                    c = this.recordCount;//just set the rest of column value to 0 
                    while (c <= data.length - 1) {
                        data[c] = 0.0f;
                        ++c;
                    }
                    if (this.timePeriod == 2 || this.timePeriod == 3) { //daily/weekly data
                        if (this.fields.isHighField() && this.fields.isLowField() && this.fields.isOpenInterest() && this.fields.isOpenPrice()) {
                            this.quotes.add(new Quote(ConvertUtils.float2Date(data[0]), data[2], data[3], data[1], data[4], (long)data[6], (long)data[5]));
                        } else if (this.fields.isHighField() && this.fields.isLowField() && !this.fields.isOpenInterest() && this.fields.isOpenPrice()) {
                            this.quotes.add(new Quote(ConvertUtils.float2Date(data[0]), data[2], data[3], data[1], data[4], 0, (long)data[5]));
                        } else if (this.fields.isHighField() && this.fields.isLowField() && !this.fields.isOpenInterest() && !this.fields.isOpenPrice()) {
                            this.quotes.add(new Quote(ConvertUtils.float2Date(data[0]), data[1], data[2], 0.0f, data[3], 0, (long)data[4]));
                        }
                    } else {
                        if (this.timePeriod == 1) {
                            throw new UnsupportedMetaFileException("NOT SUPPORT INTRADAY;[symbol=" + this.symbol + ", file=" + this.fileName + ", offset=" + i + ", recordLength=" + this.recordLength);
                        }
                        throw new UnsupportedMetaFileException("NOT SUPPORT TIMEFRAME;[symbol=" + this.symbol + ", file=" + this.fileName + ", timePeriod=" + this.timePeriod);
                    }
                    i += this.recordLength;
                }
            }
        }
        catch (FileNotFoundException e) {
            logger.error(MFUtils.stackTrace2String(e));
	    throw e;
        }
        catch (IOException e) {
            logger.error(MFUtils.stackTrace2String(e));
   	    throw e;
        }
        catch (ConvertException e) {
            logger.error(MFUtils.stackTrace2String(e));
	    throw e;
        }
    }
    
    private void setArrayItems(byte[] targetArray, int offset, byte[] sourceArray){    	
    	System.arraycopy(sourceArray, 0, targetArray, offset, sourceArray.length);    	
    }
    
    private byte[] quoteToBytes(Quote quote) throws UnsupportedMetaFileException{    	
    	byte[] bytes = new byte[recordLength];
    	if (this.timePeriod == 2 || this.timePeriod == 3) { //daily/weekly data
            if (this.fields.isHighField() && this.fields.isLowField() && this.fields.isOpenInterest() && this.fields.isOpenPrice()) {
            	setArrayItems(bytes, 0 * 4, ConvertUtils.float2MsbByte(ConvertUtils.Date2Float(quote.getDate())));
            	setArrayItems(bytes, 1 * 4, ConvertUtils.float2MsbByte(quote.getOpen()));
            	setArrayItems(bytes, 2 * 4, ConvertUtils.float2MsbByte(quote.getHigh()));
            	setArrayItems(bytes, 3 * 4, ConvertUtils.float2MsbByte(quote.getLow()));
            	setArrayItems(bytes, 4 * 4, ConvertUtils.float2MsbByte(quote.getClose()));
            	setArrayItems(bytes, 5 * 4, ConvertUtils.float2MsbByte((float)quote.getVolume()));
            	setArrayItems(bytes, 6 * 4, ConvertUtils.float2MsbByte((float)quote.getOpenInterest()));            	
            	
            } else if (this.fields.isHighField() && this.fields.isLowField() && !this.fields.isOpenInterest() && this.fields.isOpenPrice()) {
            	setArrayItems(bytes, 0 * 4, ConvertUtils.float2MsbByte(ConvertUtils.Date2Float(quote.getDate())));
            	setArrayItems(bytes, 1 * 4, ConvertUtils.float2MsbByte(quote.getOpen()));
            	setArrayItems(bytes, 2 * 4, ConvertUtils.float2MsbByte(quote.getHigh()));
            	setArrayItems(bytes, 3 * 4, ConvertUtils.float2MsbByte(quote.getLow()));
            	setArrayItems(bytes, 4 * 4, ConvertUtils.float2MsbByte(quote.getClose()));
            	setArrayItems(bytes, 5 * 4, ConvertUtils.float2MsbByte((float)quote.getVolume()));            	
            	
            } else if (this.fields.isHighField() && this.fields.isLowField() && !this.fields.isOpenInterest() && !this.fields.isOpenPrice()) {
            	setArrayItems(bytes, 0 * 4, ConvertUtils.float2MsbByte(ConvertUtils.Date2Float(quote.getDate())));
            	setArrayItems(bytes, 1 * 4, ConvertUtils.float2MsbByte(quote.getHigh()));
            	setArrayItems(bytes, 2 * 4, ConvertUtils.float2MsbByte(quote.getLow()));
            	setArrayItems(bytes, 3 * 4, ConvertUtils.float2MsbByte(quote.getClose()));
            	setArrayItems(bytes, 4 * 4, ConvertUtils.float2MsbByte((float)quote.getVolume()));
            	
            }
            return bytes;
        } else {
            if (this.timePeriod == 1) {
                throw new UnsupportedMetaFileException("NOT SUPPORT INTRADAY;[symbol=" + this.symbol + ", file=" + this.fileName + ", recordLength=" + this.recordLength);
            }
            throw new UnsupportedMetaFileException("NOT SUPPORT TIMEFRAME;[symbol=" + this.symbol + ", file=" + this.fileName + ", timePeriod=" + this.timePeriod);
        }
    }

//    private static String permuteFileName(String fileNameStr){
//    	int filePathIndex = fileNameStr.lastIndexOf("\\");
//        String localFileName  = fileNameStr.substring(filePathIndex+1);
//        if(localFileName.indexOf("-") > 0)
//        	localFileName = localFileName.replace("-", "");
//        else{//add "-"
//        	localFileName = localFileName.substring(0, 1).concat("-").concat(localFileName.substring(1));
//        }
//        return fileNameStr.substring(0, filePathIndex + 1).concat(localFileName);
//    }
    
    public void updateQuotes(List<Quote> quotes) throws IOException, UnsupportedMetaFileException, ConvertException { //add seperate exception like filenotfound, io
    	File dataFile = new File(this.fileName);
    	if(!dataFile.exists()){
    		// dataFile = new File(permuteFileName(this.fileName));
    		 //if(!dataFile.exists()){
    		logger.info("File ".concat(this.fileName).concat(" not found. Create new."));
    		//throw new IOException("File ".concat(this.fileName).concat(" not found."));
    		// }
    	}
    	//2 scenarios - complete rewrite and append
    	
    	RandomAccessFile randomAccessFile = new RandomAccessFile(dataFile, "rw");		
		//for each quote item convert to byte array		
		try{
			byte[] header = new byte[recordLength];
			if(newRecord){
				//create empty header				
				setArrayItems(header, 2, ConvertUtils.shortInt2Byte(quotes.size() + 1));				
				randomAccessFile.seek(0);
				randomAccessFile.write(header);
			} else {
				randomAccessFile.seek(0);
				randomAccessFile.read(header);
				int recordSize = ConvertUtils.byte2Int(header, 2, 2);
				recordSize = recordSize + 1;
				randomAccessFile.seek(2);
				randomAccessFile.write(ConvertUtils.shortInt2Byte(recordSize));
			}
	    	if(quotes.size() == 1){ //append
				randomAccessFile.seek(randomAccessFile.length());
				randomAccessFile.write(quoteToBytes(quotes.get(0)));
			} else { //complete rewrite 
				
				byte[] quotesBytes = new byte[recordLength*quotes.size()];
				
				randomAccessFile.seek((long)this.recordLength);
				int i = 0;
				for(Quote quote : quotes){
					setArrayItems(quotesBytes, recordLength*i, quoteToBytes(quote));
					i++;
				}
				randomAccessFile.write(quotesBytes);
			}
		}finally{
			randomAccessFile.close();
		}
		
    }
    
    public void exportCSV(String fileName, String delimiter) throws Exception {
        this.exportCSV(new File(fileName), delimiter, null, null);
    }

    public void exportCSV(File file, String delimiter) throws Exception {
        this.exportCSV(file, delimiter, null, null);
    }

    public void exportCSV(String fileName, String delimiter, Date startDate, Date stopDate) throws Exception {
        this.exportCSV(new File(fileName), delimiter, startDate, stopDate);
    }

    public void exportCSV(File file, String delimiter, Date startDate, Date stopDate) throws Exception {
        int[] selectedColumn = new int[]{1, 4, 5, 6, 7, 9};
        this.exportCSV(file, delimiter, startDate, stopDate, selectedColumn);
    }

    public void exportCSV(String fileName, String delimiter, Date startDate, Date stopDate, int[] selectedColumn) throws Exception {
        this.exportCSV(new File(fileName), delimiter, startDate, stopDate, selectedColumn);
    }

    public void exportCSV(File file, String delimiter, Date startDate, Date stopDate, int[] selectedColumn) throws Exception {
        if (this.quoteCount == -1) {
            this.loadQuotes();
        }
        if (this.quoteCount > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            if (file.canWrite()) {
                for (Quote quote : this.quotes) {
                    Date d = quote.getDate();
                    if (startDate != null && d.compareTo(startDate) < 0 || stopDate != null && d.compareTo(stopDate) > 0) continue;
                    int j = 0;
                    while (j < selectedColumn.length) {
                        switch (selectedColumn[j]) {
                            case 1: {
                                writer.print(dateFormat.format(quote.getDate()));
                                break;
                            }
                            case 5: {
                                writer.printf("%.2f", Float.valueOf(quote.getClose()));
                                break;
                            }
                            case 6: {
                                writer.printf("%.2f", Float.valueOf(quote.getHigh()));
                                break;
                            }
                            case 7: {
                                writer.printf("%.2f", Float.valueOf(quote.getLow()));
                                break;
                            }
                            case 2: {
                                writer.print(this.name);
                                break;
                            }
                            case 4: {
                                writer.printf("%.2f", Float.valueOf(quote.getOpen()));
                                break;
                            }
                            case 8: {
                                writer.printf("%.2f", quote.getOpenInterest());
                                break;
                            }
                            case 3: {
                                writer.print(this.symbol);
                                break;
                            }
                            case 9: {
                                writer.printf("%d", quote.getVolume());
                            }
                        }
                        if (j == selectedColumn.length - 1) {
                            writer.println();
                        } else {
                            writer.print(delimiter);
                        }
                        ++j;
                    }
                }
            }
            writer.close();
        }
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getQuoteCount() {
        return this.quoteCount;
    }

    public void setQuoteCount(int quoteCount) {
        this.quoteCount = quoteCount;
    }

    public int getFileNumber() {
        return this.fileNumber;
    }

    public void setFileNumber(int fileNumber) {
        this.fileNumber = fileNumber;
    }

    public ArrayList<Quote> getQuotes() {
        return this.quotes;
    }

    public void setQuotes(ArrayList<Quote> quotes) {
        this.quotes = quotes;
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

    public int getTimePeriod() {
        return this.timePeriod;
    }

    public void setTimeFrame(int timePeriod) {
        this.timePeriod = timePeriod;
    }

    public boolean hasOpeningPrice() {
        return this.fields.isOpenPrice();
    }

    public boolean hasOpenInterest() {
        return this.fields.isOpenInterest();
    }

	public int getMasterFileIndex() {
		return masterFileIndex;
	}

	public void setMasterFileIndex(int masterFileIndex) {
		this.masterFileIndex = masterFileIndex;
	}

	public MasterFileType getMasterFileType() {
		return masterFileType;
	}

	public void setMasterFileType(MasterFileType masterFileType) {
		this.masterFileType = masterFileType;
	}

	public boolean isNewRecord() {
		return newRecord;
	}

	public void setNewRecord(boolean newRecord) {
		this.newRecord = newRecord;
	}
    
    
}

