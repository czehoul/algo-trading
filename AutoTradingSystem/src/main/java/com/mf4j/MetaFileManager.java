/*
 * Decompiled with CFR 0_117.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.mf4j;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mf4j.util.ConvertUtils;
import com.yee.trading.auto.event.EventProcessor;
import com.yee.trading.auto.util.NotificationEventUtil;

public class MetaFileManager {
	static Logger logger = LoggerFactory.getLogger((String) MetaFileManager.class.getName());
	private Hashtable<String, MetaFile[]> metaFiles = new Hashtable();
	private String metaFilePath;
	private int stockCount;
	private boolean addSymbolSupport;
	private EventProcessor eventProcessor;

	public MetaFileManager(String metafilePath) {
		this.metaFilePath = !metafilePath.endsWith(File.separator) ? String.valueOf(metafilePath) + File.separator
				: metafilePath;
	}

	public void create() {
	}

	public void initialize() throws IOException {
		int i;
		MetaFile[] metaFile;
		MetaFile[] newMetaFile;
		int oldLength;
		MetaFile[] oldMetaFile;
		this.stockCount = 0;
		String masterFileName = String.valueOf(this.metaFilePath) + "MASTER";
		byte[] buffer = new byte[(int) new File(masterFileName).length()];
		BufferedInputStream f = null;
		f = new BufferedInputStream(new FileInputStream(masterFileName));
		f.read(buffer);
		f.close();
		int recordMasterCount = buffer.length / 53 - 1; // first record is not
														// data, ignore
		logger.debug(("master has " + recordMasterCount + " records."));
		ArrayList<MasterFile> masterFileRecords = new ArrayList<MasterFile>();
		int i2 = 0;
		while (i2 < recordMasterCount) {
			MasterFile masterFileRecord = new MasterFile(buffer, i2);
			masterFileRecords.add(masterFileRecord);
			++i2;
		}
		this.stockCount = recordMasterCount;
		String emasterFileName = String.valueOf(this.metaFilePath) + "EMASTER";
		buffer = new byte[(int) new File(emasterFileName).length()];
		f = new BufferedInputStream(new FileInputStream(emasterFileName));
		f.read(buffer);
		f.close();
		recordMasterCount = buffer.length / 192 - 1;
		ArrayList<EMasterFile> emasterFileRecords = new ArrayList<EMasterFile>();
		int i3 = 0;
		while (i3 < recordMasterCount) {
			EMasterFile emasterFileRecord = new EMasterFile(buffer, i3);
			emasterFileRecords.add(emasterFileRecord);
			++i3;
		}
		String xmasterFileName = String.valueOf(this.metaFilePath) + "XMASTER";
		File xmasterFile = new File(xmasterFileName);
		ArrayList<XMasterFile> xmasterFileRecords = new ArrayList<XMasterFile>();
		int recordXMasterCount = 0;
		if (xmasterFile.exists()) {
			buffer = new byte[(int) xmasterFile.length()];
			f = new BufferedInputStream(new FileInputStream(xmasterFileName));
			f.read(buffer);
			f.close();
			recordXMasterCount = buffer.length / 150 - 1;
			i = 0;
			while (i < recordXMasterCount) {
				XMasterFile xmasterFileRecord = new XMasterFile(buffer, i);
				xmasterFileRecords.add(xmasterFileRecord);
				++i;
			}
		}
		logger.debug(("xmaster has " + recordXMasterCount + " records."));
		this.stockCount += recordXMasterCount;
		i = 0;
		while (i < recordMasterCount) {
			metaFile = new MetaFile[] { new MetaFile(
					String.valueOf(this.metaFilePath) + ((MasterFile) masterFileRecords.get(i)).getFileName(),
					(MasterFile) masterFileRecords.get(i), (EMasterFile) emasterFileRecords.get(i)) };
			metaFile[0].setMasterFileIndex(i);
			metaFile[0].setMasterFileType(MasterFileType.Master);
			if (this.metaFiles.containsKey(metaFile[0].getSymbol())) {// add
																		// meta
																		// file
																		// to
																		// array,
																		// one
																		// symbol
																		// can
																		// have
																		// more
																		// than
																		// one
																		// metafile
																		// (one
																		// or
																		// more
																		// record
																		// in
																		// master
																		// file
																		// for a
																		// symbol)
				oldMetaFile = this.metaFiles.get(metaFile[0].getSymbol());
				oldLength = oldMetaFile.length;
				newMetaFile = new MetaFile[oldMetaFile.length + 1];
				System.arraycopy(oldMetaFile, 0, newMetaFile, 0, oldLength);
				newMetaFile[oldLength] = metaFile[0];
				this.metaFiles.put(metaFile[0].getSymbol(), newMetaFile);
			} else {
				this.metaFiles.put(metaFile[0].getSymbol(), metaFile);
			}
			++i;
		}
		i = 0;
		while (i < recordXMasterCount) {
			metaFile = new MetaFile[] { new MetaFile(
					String.valueOf(this.metaFilePath) + ((XMasterFile) xmasterFileRecords.get(i)).getFileName(),
					(XMasterFile) xmasterFileRecords.get(i)) };
			metaFile[0].setMasterFileIndex(i);
			metaFile[0].setMasterFileType(MasterFileType.XMaster);
			if (this.metaFiles.containsKey(metaFile[0].getSymbol())) {
				oldMetaFile = this.metaFiles.get(metaFile[0].getSymbol());
				oldLength = oldMetaFile.length;
				newMetaFile = new MetaFile[oldMetaFile.length + 1];
				System.arraycopy(oldMetaFile, 0, newMetaFile, 0, oldLength);
				newMetaFile[oldLength] = metaFile[0];
				this.metaFiles.put(metaFile[0].getSymbol(), newMetaFile);
			} else {
				this.metaFiles.put(metaFile[0].getSymbol(), metaFile);
			}
			++i;
		}
	}

	public MetaFile[] getMetaFileBySymbol(String symbol) {
		if (this.metaFiles.containsKey(symbol)) {
			return this.metaFiles.get(symbol);
		}
		return null;
	}

	public MetaFile[] getMetaFileByName(String name) {
		Enumeration<MetaFile[]> enums = this.metaFiles.elements();
		while (enums.hasMoreElements()) {
			MetaFile[] metaFile = enums.nextElement();
			if (!metaFile[0].getName().equals(name))
				continue;
			return metaFile;
		}
		return null;
	}

	public MetaFile[] getAllMetaFiles() {
		if (this.stockCount <= 0) {
			return null;
		}
		MetaFile[] metaFile = new MetaFile[this.stockCount];
		int i = 0;
		Enumeration<MetaFile[]> enums = this.metaFiles.elements();
		while (enums.hasMoreElements()) {
			MetaFile[] mf = enums.nextElement();
			int j = 0;
			while (j < mf.length) {
				metaFile[i++] = mf[j];
				++j;
			}
		}
		return metaFile;
	}

	public String[] getAllSymbols() {
		String[] symbols = new String[this.metaFiles.size()];
		Enumeration<String> enums = this.metaFiles.keys();
		int i = 0;
		while (enums.hasMoreElements()) {
			symbols[i++] = enums.nextElement();
		}
		return symbols;
	}

	public String[] getAllNames() {
		String[] names = new String[this.metaFiles.size()];
		Enumeration<MetaFile[]> enums = this.metaFiles.elements();
		int i = 0;
		while (enums.hasMoreElements()) {
			MetaFile[] metaFile = enums.nextElement();
			names[i++] = metaFile[0].getName();
		}
		return names;
	}

	public int getQuoteDecimalPrecision() {
		return Quote.getDecimalPrecision();
	}

	public void setQuoteDecimalPrecision(int decimalPrecision) {
		Quote.setDecimalPrecision(decimalPrecision);
	}

	public int getStockCount() {
		return this.stockCount;
	}

	private List<Quote> removeDuplicates(List<Quote> list) {
		// Store unique items in result.
		List<Quote> result = new ArrayList<>();

		// Record encountered Strings in HashSet.
		HashSet<String> set = new HashSet<String>();

		// Loop over argument list.
		for (Quote item : list) {
			// If String is not in set, add it to the list and the set.
			if (!set.contains(item.getDateString())) {
				result.add(item);
				set.add(item.getDateString());
			}
		}
		return result;
	}

	private int getMaxFileNumber(String filePath) {
		File folder = new File(filePath);
		File[] listOfFiles = folder.listFiles();

		int maxFileNumber = 0;
		for (int i = 0; i < listOfFiles.length; i++) {
			String fileName = listOfFiles[i].getName();
			if (fileName.endsWith(".DAT") || fileName.endsWith(".MWD")) {
				int fileNumber = Integer.parseInt(fileName.substring(1, fileName.indexOf(".")));
				if (fileNumber > maxFileNumber) {
					maxFileNumber = fileNumber;
				}
			}
		}
		return maxFileNumber;
	}

	private byte[] createMasterRecord(int fileNumber, Date firstDate, Date lastDate, String symbol,
			byte[] previousRecord) {
		byte[] newRecordByte = new byte[53];
		if (previousRecord.length == 53) {
			System.arraycopy(previousRecord, 0, newRecordByte, 0, 53);// clone
																		// the
																		// record
		}
		newRecordByte[0] = (byte) fileNumber;// file number
		// issuer name
		byte[] issuerNameColumnByte = new byte[16];
		byte[] issuerNameByte = symbol.getBytes();
		System.arraycopy(issuerNameByte, 0, issuerNameColumnByte, 0, issuerNameByte.length);
		System.arraycopy(issuerNameColumnByte, 0, newRecordByte, 7, issuerNameColumnByte.length);
		// first date
		byte[] firstDateByte = ConvertUtils.float2MsbByte(ConvertUtils.Date2Float(firstDate));
		System.arraycopy(firstDateByte, 0, newRecordByte, 25, firstDateByte.length);
		// last date
		byte[] lastDateByte = ConvertUtils.float2MsbByte(ConvertUtils.Date2Float(lastDate));
		System.arraycopy(lastDateByte, 0, newRecordByte, 29, lastDateByte.length);
		// symbol
		byte[] symbolColumnByte = new byte[14];
		System.arraycopy(issuerNameByte, 0, symbolColumnByte, 0, issuerNameByte.length);
		System.arraycopy(symbolColumnByte, 0, newRecordByte, 36, symbolColumnByte.length);
		return newRecordByte;
	}

	private byte[] createEMasterRecord(int fileNumber, Date firstDate, Date lastDate, String symbol,
			byte[] previousRecord) throws ConvertException {
		byte[] newRecordByte = new byte[192];
		if (previousRecord.length == 192) {
			System.arraycopy(previousRecord, 0, newRecordByte, 0, 192);// clone
																		// the
																		// record
		}
		newRecordByte[2] = (byte) fileNumber;// file number
		// issuer name
		byte[] issuerNameColumnByte = new byte[16];
		byte[] issuerNameByte = symbol.getBytes();
		System.arraycopy(issuerNameByte, 0, issuerNameColumnByte, 0, issuerNameByte.length);
		System.arraycopy(issuerNameColumnByte, 0, newRecordByte, 32, issuerNameColumnByte.length);
		// first date
		byte[] firstDateByte = ConvertUtils.date2Byte(firstDate);
		System.arraycopy(firstDateByte, 0, newRecordByte, 64, firstDateByte.length);
		// last date
		byte[] lastDateByte = ConvertUtils.date2Byte(lastDate);
		System.arraycopy(lastDateByte, 0, newRecordByte, 72, lastDateByte.length);
		// symbol
		byte[] symbolColumnByte = new byte[13];
		System.arraycopy(issuerNameByte, 0, symbolColumnByte, 0, issuerNameByte.length);
		System.arraycopy(symbolColumnByte, 0, newRecordByte, 11, symbolColumnByte.length);
		return newRecordByte;
	}

	private byte[] createXMasterRecord(int fileNumber, Date firstDate, Date lastDate, String symbol,
			byte[] previousRecord) throws ConvertException {
		byte[] newRecordByte = new byte[150];
		if (previousRecord.length == 150) {
			System.arraycopy(previousRecord, 0, newRecordByte, 0, 150);// clone
																		// the
																		// record
		}
		String period = new String(newRecordByte, 62, 1);
		XMasterFile xmasterFileRecord = new XMasterFile(previousRecord, -1);
		// file number
		newRecordByte[65] = (byte) fileNumber;
		newRecordByte[66] = (byte) (fileNumber >>> 8);
		// newRecordByte[0] = (byte)fileNumber;
		// issuer name
		byte[] issuerNameColumnByte = new byte[23];
		byte[] issuerNameByte = symbol.getBytes();
		System.arraycopy(issuerNameByte, 0, issuerNameColumnByte, 0, issuerNameByte.length);
		System.arraycopy(issuerNameColumnByte, 0, newRecordByte, 16, issuerNameColumnByte.length);
		// first date
		byte[] firstDateByte = ConvertUtils.date2ByteX(firstDate);

		System.arraycopy(firstDateByte, 0, newRecordByte, 104, firstDateByte.length);
		// last date
		byte[] lastDateByte = ConvertUtils.date2ByteX(lastDate);
		System.arraycopy(lastDateByte, 0, newRecordByte, 108, lastDateByte.length);
		// symbol
		byte[] symbolColumnByte = new byte[14];
		System.arraycopy(issuerNameByte, 0, symbolColumnByte, 0, issuerNameByte.length);
		System.arraycopy(symbolColumnByte, 0, newRecordByte, 1, symbolColumnByte.length);

		return newRecordByte;
	}

	/**
	 * update metastock data
	 * 
	 * @param dataUpdateFile
	 */
	public void updateData(DataUpdateFile dataUpdateFile) throws DataUpdateException {
		// Open Master file
		// 1. update master file for first / last date
		String masterFileName = String.valueOf(this.metaFilePath) + "MASTER";
		String xMasterFileName = String.valueOf(this.metaFilePath) + "XMASTER";
		RandomAccessFile masterFile = null;
		RandomAccessFile xMasterFile = null;
		RandomAccessFile emasterRaf = null;
		try {
			masterFile = new RandomAccessFile(masterFileName, "rw");
			File dataFile = new File(xMasterFileName);
			if (dataFile.exists()) {
				xMasterFile = new RandomAccessFile(xMasterFileName, "rw");
			}
			// for each groupped records(symbol)
			Iterator<Entry<String, List<Quote>>> it = dataUpdateFile.getGrouppedRecords().entrySet().iterator();
			while (it.hasNext()) {
				boolean toUpdateMetaFile = false;
				Entry<String, List<Quote>> entry = it.next();
				String symbol = entry.getKey();
				MetaFile[] metaFiles = getMetaFileBySymbol(symbol);
				List<Quote> quotes = entry.getValue();
				if (quotes == null || quotes.size() == 0) {
					throw new DataUpdateException("No quotes found for symbol ".concat(symbol));
				}
				boolean newSymbol = false;
				// 2. update meta file quotes
				if (metaFiles == null || metaFiles.length == 0) {
					// it should be logged instead of throwing error because
					// the master file might not contain entry for new ticker
					// since
					// last complete update
					// throw new DataUpdateException(
					// "Invalid meta file size for symbol ".concat(symbol));
					// if metaFiles = null it meant no entry in master file
					// entry will be created in master/xmaster file (max 255
					// file for master otherwise xmaster)
					if (metaFiles == null && addSymbolSupport) {
						newSymbol = true;
						int newMetaNumber = getMaxFileNumber(this.metaFilePath) + 1;
						if (newMetaNumber <= 255) {
							int rowCount = (int) (masterFile.length() / 53 - 1);
							// create master new record
							masterFile.seek(masterFile.length() - 53);
							byte[] previousRecord = new byte[53];
							masterFile.read(previousRecord, 0, 53);
							masterFile.seek(masterFile.length());
							byte[] masterRecordByte = createMasterRecord(newMetaNumber, quotes.get(0).getDate(),
									quotes.get(quotes.size() - 1).getDate(), symbol, previousRecord);
							masterFile.write(masterRecordByte);
							// update master header
							byte[] masterHeader = new byte[53];
							masterFile.seek(0);
							masterFile.read(masterHeader);
							int totalFile = ConvertUtils.byte2Int(masterHeader, 0, 2) + 1;
							int nextFileNumber = ConvertUtils.byte2Int(masterHeader, 2, 2) + 1;
							masterFile.write(ConvertUtils.shortInt2Byte(totalFile));
							masterFile.seek(2);
							masterFile.write(ConvertUtils.shortInt2Byte(nextFileNumber));
							// create emaster new record
							emasterRaf = new RandomAccessFile(String.valueOf(this.metaFilePath) + "EMASTER", "rw");
							byte[] previousEmasterRecord = new byte[192];
							emasterRaf.seek(emasterRaf.length() - 192);
							emasterRaf.read(previousEmasterRecord, 0, 192);
							byte[] eMasterRecordByte = createEMasterRecord(newMetaNumber, quotes.get(0).getDate(),
									quotes.get(quotes.size() - 1).getDate(), symbol, previousEmasterRecord);
							emasterRaf.seek(emasterRaf.length());
							emasterRaf.write(eMasterRecordByte);
							// update emaster header
							byte[] emasterHeader = new byte[192];
							emasterRaf.seek(0);
							emasterRaf.read(emasterHeader);
							int etotalFile = ConvertUtils.byte2Int(emasterHeader, 0, 2) + 1;
							int enextFileNumber = ConvertUtils.byte2Int(emasterHeader, 2, 2) + 1;
							emasterRaf.write(ConvertUtils.shortInt2Byte(etotalFile));
							emasterRaf.seek(2);
							emasterRaf.write(ConvertUtils.shortInt2Byte(enextFileNumber));
							// create master and emaster object
							MasterFile masterFileRecord = new MasterFile(masterRecordByte, -1);
							EMasterFile eMasterFileRecord = new EMasterFile(eMasterRecordByte, -1);
							// Create meta file
							metaFiles = new MetaFile[] {
									new MetaFile(
											String.valueOf(this.metaFilePath).concat("F")
													.concat(String.valueOf(newMetaNumber)).concat(".DAT"),
											masterFileRecord, eMasterFileRecord) };
							metaFiles[0].setMasterFileIndex(rowCount + 1);
							metaFiles[0].setMasterFileType(MasterFileType.Master);
							metaFiles[0].setNewRecord(true);
							logger.info("Added to master file for symbol ".concat(symbol));
						} else {
							// add a record to xmaster
							int rowCount = (int) (xMasterFile.length() / 150 - 1);
							xMasterFile.seek(xMasterFile.length() - 150);
							byte[] previousRecord = new byte[150];
							xMasterFile.read(previousRecord, 0, 150);
							xMasterFile.seek(xMasterFile.length());
							byte[] xmasterRecordByte = createXMasterRecord(newMetaNumber, quotes.get(0).getDate(),
									quotes.get(quotes.size() - 1).getDate(), symbol, previousRecord);
							xMasterFile.write(xmasterRecordByte);
							// create xmaster file record
							XMasterFile xmasterFileRecord = new XMasterFile(xmasterRecordByte, -1);
							// create metafile
							metaFiles = new MetaFile[] {
									new MetaFile(String.valueOf(this.metaFilePath) + (xmasterFileRecord).getFileName(),
											xmasterFileRecord) };
							metaFiles[0].setMasterFileIndex(rowCount + 1);
							metaFiles[0].setMasterFileType(MasterFileType.XMaster);
							metaFiles[0].setNewRecord(true);
							logger.info("Added to xmaster file for symbol ".concat(symbol));
						}

					}
					
					if (metaFiles == null && !addSymbolSupport) {
						logger.info("New symbol detected. Symbol will not be added because addSymbolSupport flag is disabled. Symbol - ".concat(symbol));
					}
					//moving symbol to new industry (yongtai case on 12/05/17) suspected
					//send email notification
					if(quotes.size() > 1){
						String message  = "Multiple eod data for a new symbol detected, moving symbol? Symbol - ".concat(symbol);
						logger.info(message);
						eventProcessor.onEvent(NotificationEventUtil
								.createNotificationEvent(message));
					}
					if(metaFiles != null && metaFiles.length == 0) {
						// log
						logger.info("Invalid meta file size for symbol ".concat(symbol));
					}
				} // remove else
				if (metaFiles != null && metaFiles.length > 0) {
					int masterFileIndex = metaFiles[metaFiles.length - 1].getMasterFileIndex();
					if (metaFiles[0].getMasterFileType() == MasterFileType.Master) {
						byte[] oriLastDate = new byte[4];
						masterFile.seek((masterFileIndex) * 53 + 29);
						masterFile.read(oriLastDate, 0, 4);
						if (DateUtils.isSameDay(ConvertUtils.msbByte2Date(oriLastDate, 0),
								quotes.get(quotes.size() - 1).getDate())) {
							if (newSymbol)
								toUpdateMetaFile = true;
							else
								toUpdateMetaFile = false;
						} else {
							byte[] lastDateByte = ConvertUtils
									.float2MsbByte(ConvertUtils.Date2Float(quotes.get(quotes.size() - 1).getDate()));
							masterFile.seek((masterFileIndex) * 53 + 29);
							masterFile.write(lastDateByte);
							toUpdateMetaFile = true;
						}
					} else {
						byte[] oriLastDateByte = new byte[4];
						xMasterFile.seek((masterFileIndex) * 150 + 108);
						xMasterFile.read(oriLastDateByte, 0, 4);
						int oriLastDate = ConvertUtils.byte2Int(oriLastDateByte, 0, 4);
						int newLastDate = ConvertUtils.Date2IntX(quotes.get(quotes.size() - 1).getDate());
						if (oriLastDate == newLastDate) {
							if (newSymbol)
								toUpdateMetaFile = true;
							else
								toUpdateMetaFile = false;
						} else {
							byte[] lastDateByte = ConvertUtils.int2Byte(newLastDate);
							xMasterFile.seek((masterFileIndex) * 150 + 108);
							xMasterFile.write(lastDateByte);
							toUpdateMetaFile = true;
						}

					}
					try {
						// if there is only one quote then update to last meta
						// file, otherwise
						// update meta file quote based on range of the fist
						// last date and update the range
						if (toUpdateMetaFile == true) {
							if (quotes.size() <= 10) {								
								metaFiles[metaFiles.length - 1].updateQuotes(quotes.subList(quotes.size()-1, quotes.size()));
							} else {
								// remove duplicate
								quotes = removeDuplicates(quotes);
								// sort
								Collections.sort(quotes, Quote.getCompByDate());

								// TODO - it must be price adjustment if it
								// contains more than 1 quote
								// can send notification so that quantity of
								// share can be adjust in DB

								if (metaFiles.length == 1) {
									metaFiles[0].updateQuotes(quotes);
								} else {
									for (int i = 0; i < metaFiles.length; i++) {
										MetaFile metaFileRecord = metaFiles[i];

										List<Quote> subQuoteList = new ArrayList<Quote>();
										// boolean quoteStart = false;
										int quoteIndex = 0;
										for (Quote quote : quotes) {
											if (DateUtils.isSameDay(metaFileRecord.getFirstDate(), quote.getDate())
													|| (quote.getDate().after(metaFileRecord.getFirstDate())
															&& quote.getDate().before(metaFileRecord.getLastDate()))
													|| DateUtils.isSameDay(metaFileRecord.getLastDate(),
															quote.getDate())
													|| (i == (metaFiles.length - 1)
															&& quoteIndex == (quotes.size() - 1))) {
												subQuoteList.add(quote);
											}
											quoteIndex++;
										}
										metaFileRecord.updateQuotes(subQuoteList);
									}
								}
								String message  = "Multiple eod data for a existing symbol detected, price adjusted? Symbol - ".concat(symbol);
								logger.info(message);
								eventProcessor.onEvent(NotificationEventUtil
										.createNotificationEvent(message));
							}
						}

					} catch (IOException | UnsupportedMetaFileException e) {
						throw new DataUpdateException(e.getMessage());
					}
				}

			}
		} catch (IOException e) {
			throw new DataUpdateException("Update failed, master file IO Error "
					.concat(String.valueOf(this.metaFilePath) + "MASTER").concat(" - ").concat(e.getMessage()));

		} catch (ConvertException e) {
			throw new DataUpdateException("Update failed, reading last date in master file failed "
					.concat(String.valueOf(this.metaFilePath) + "MASTER").concat(" - ").concat(e.getMessage()));

		} finally {
			if (masterFile != null) {
				try {
					masterFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (xMasterFile != null) {
				try {
					xMasterFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (emasterRaf != null) {
				try {
					emasterRaf.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public boolean isAddSymbolSupport() {
		return addSymbolSupport;
	}

	public void setAddSymbolSupport(boolean addSymbolSupport) {
		this.addSymbolSupport = addSymbolSupport;
	}

	public void setEventProcessor(EventProcessor eventProcessor) {
		this.eventProcessor = eventProcessor;
	}

	
}
