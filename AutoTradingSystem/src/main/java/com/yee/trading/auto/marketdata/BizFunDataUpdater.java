package com.yee.trading.auto.marketdata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.FileNameMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jacob.com.LibraryLoader;
import com.yee.trading.auto.connection.HTTPConnectionFactory;
import com.yee.trading.auto.event.Event;
import com.yee.trading.auto.event.EventProcessor;
import com.yee.trading.auto.event.EventType;
import com.yee.trading.auto.util.NotificationEventUtil;
import com.yee.trading.auto.util.TradingDayChecker;

import autoitx4java.AutoItX;

@Component("bizFunDataUpdater")
public class BizFunDataUpdater implements DataUpdater {
	private final Logger logger = LoggerFactory.getLogger(BizFunDataUpdater.class);
	@Value("${pre.login.location:http://www.bizfun.cc/index.php/user-profile?view=login}")
	private String preLoginLocation;
	@Value("${login.location:http://www.bizfun.cc/index.php/user-profile?task=user.login}")
	private String loginLocation;
	@Value("${file.locaton.root:http://www.bizfun.cc/index.php/m-user-my-downloads}")
	private String fileLocationRoot;
	@Value("${index.file.locaton.root:http://www.charting.xyz/MetaStock%20FT%20Index.exe}")
	private String indexFileLocation;
	@Value("${download.destination:C:\\Users\\CzeHoul\\Desktop\\Amibroker\\BizFun\\}")
	private String downloadDestination;
	@Value("${updater.executable:C:\\Users\\CzeHoul\\Desktop\\Amibroker\\BizFun\\updatev2_x86.exe}")
	private String updaterExecutable;
	@Value("${username:czehoul}")
	private String userName;
	@Value("${dataupdate.password:ych55190}")
	private String password;
	@Value("${jacob.dll:C:\\Users\\CzeHoul\\Desktop\\hleHack\\jacob-1.18\\jacob-1.18-x64.dll}")
	private String jacodDLLPath;
	@Value("${extract.location:c:\\bizfun}")
	private String extractLocation;
	@Value("${data.location:C:\\MetaStock Data}")
	private String dataLocation;
	@Value("${index.extract.location:c:\\}")
	private String indexExtractLocation;
	private String dummySession = "PHPSESSID=2e4d359e100bd5c4ea93c045b3ab0aad";
	private String cmid = "__zlcmid=aLfVI9MAyOYCWZ; ";
	@Autowired
	private EventProcessor eventProcessor;
	@Autowired
	private HTTPConnectionFactory httpConnectionFactory;
	@Autowired
	private TradingDayChecker tradingDayChecker;
	@Autowired
	private StockDBUpdater stockDBUpdater;
	
	private static final int BUFFER_SIZE = 4096;
	
	@Value("${historyfile.retrieval:false}")
	private boolean enableHistoryFile;

	private void processUpdateData()
			throws ClientProtocolException, IOException, DataUpdaterException, InterruptedException {
		HttpContext httpContext = HttpClientContext.create();
		String fileLocation = downloadFile(httpContext);
		String fileName = fileLocation.substring(fileLocation.lastIndexOf("\\") + 1);
		if (fileName.startsWith("H")) {
			File dataDirectory = new File(dataLocation);
			delete(dataDirectory);
			extractHistoryFile(fileName);
			logger.info("History file extracted successfully.");
		} else {
			extractExeFile(fileLocation, extractLocation);
			runUpdate();
		}
	}
	
//	private void processUpdateData()
//			throws ClientProtocolException, IOException, DataUpdaterException, InterruptedException {
//		HttpContext httpContext = HttpClientContext.create();
//		String fileLocation = downloadFile(httpContext);
//		String fileName = fileLocation.substring(fileLocation.lastIndexOf("\\") + 1);
//		if (fileName.startsWith("H")) {
//			File dataDirectory = new File(dataLocation);
//			delete(dataDirectory);
//			extractHistoryFile(fileName);
//			logger.info("History file extracted successfully.");
//		} else {
//			extractFile(fileLocation, extractLocation);
//			runUpdate();
	//	}
//	}

	private void processUpdateIndexData()
			throws ClientProtocolException, IOException, DataUpdaterException, InterruptedException {
		String fileLocation = downloadIndexFile(indexFileLocation);
		extractExeFile(fileLocation, indexExtractLocation);
	}

	public void updateData() {
		// login and get cookie

		logger.debug("Update started .....");
		try {
			try {
				// run updater
				//new update format index is included
				//processUpdateIndexData();
				processUpdateData();

				eventProcessor.onEvent(
						NotificationEventUtil.createNotificationEvent("Market data update completed successfully"));
				logger.debug("Update completed .....");
				eventProcessor.onEvent(new Event(EventType.NEW_TICK));
			} catch (IOException | DataUpdaterException | InterruptedException e) {
				logger.error("Market data update failed - " + e.getMessage());
				eventProcessor.onEvent(
						NotificationEventUtil.createNotificationEvent("Market data update failed - " + e.getMessage()));
				e.printStackTrace();
				try {
					// retry
					//processUpdateIndexData();
					processUpdateData();
					eventProcessor.onEvent(
							NotificationEventUtil.createNotificationEvent("Market data update completed successfully"));
					logger.debug("Update completed .....");
					eventProcessor.onEvent(new Event(EventType.NEW_TICK));
				} catch (IOException | DataUpdaterException | InterruptedException e1) {
					logger.error("Market data update retry failed - " + e.getMessage());
					eventProcessor.onEvent(NotificationEventUtil
							.createNotificationEvent("Market data update retry failed - " + e.getMessage()));
					e.printStackTrace();
				}
			}
		} catch (RuntimeException re) {
			logger.error("Runtime exception occured in update market data", re);
			eventProcessor.onEvent(NotificationEventUtil
					.createNotificationEvent("Runtime exception occured in update market data" + re.getMessage()));
		}
		// download file

	}

	private void extractExeFile(String fileLocation, String extractLoc) throws IOException, InterruptedException {
		String[] cmd = { fileLocation, "/auto", extractLoc };
		Process p;

		p = Runtime.getRuntime().exec(cmd);
		p.waitFor();
		logger.debug("File " + fileLocation + " extracted.");

	}

	private void unzip(String zipFilePath, String destDirectory) throws IOException {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			String filePath = destDirectory + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				extractFile(zipIn, filePath);
			} else {
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	/**
	 * Extracts a zip entry (file entry)
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}

	private void extractFile(String fileLocation, String extractLoc) throws IOException, InterruptedException {
		
		unzip(fileLocation, extractLoc);
		logger.debug("File " + fileLocation + " extracted.");

	}

	private String login(HttpContext httpContext) throws ClientProtocolException, IOException, DataUpdaterException {
		String cookie = "";
		String loginCookie = "";
		String token = "";
		CloseableHttpResponse postResponse = null;
		CloseableHttpResponse getResponse = null;
		try {
			CloseableHttpClient httpPostClient = httpConnectionFactory.getHTTPClient();
			HttpGet get = new HttpGet(preLoginLocation);
			getResponse = httpPostClient.execute(get, httpContext);
			if (getResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Header[] headers = getResponse.getAllHeaders();
				for (Header header : headers) {
					if (header.getName().equals("Set-Cookie")
							&& header.getValue().indexOf("2343c7267a61eba901c025a423ae227c") != -1) {
						loginCookie = header.getValue();
						break;
					}

				}
				loginCookie = loginCookie.substring(0, loginCookie.indexOf(";")).trim();
				Document doc = Jsoup.parse(EntityUtils.toString(getResponse.getEntity()));
				Elements inputs = doc.getElementsByTag("input");
				Element tokenElement = inputs.last();
				token = tokenElement.attr("name");
			} else {
				throw new DataUpdaterException(
						"Fail to get cookie - HTTP response code = " + getResponse.getStatusLine().getStatusCode());
			}

			HttpPost post = new HttpPost(loginLocation);
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair("username", userName));
			urlParameters.add(new BasicNameValuePair("password", password));
			urlParameters.add(new BasicNameValuePair("return", "aW5kZXgucGhwP29wdGlvbj1jb21fdXNlcnMmdmlldz1wcm9maWxl"));
			urlParameters.add(new BasicNameValuePair(token, "1"));
			HttpParams params = new BasicHttpParams();
			params.setParameter("http.protocol.handle-redirects", false);
			post.setParams(params);
			post.setEntity(new UrlEncodedFormEntity(urlParameters));
			post.addHeader("Content-Type", "application/x-www-form-urlencoded");
			post.addHeader("Cookie", cmid + "fc71a7ca30b5f9790ba9f98858181aff=en-GB; " + loginCookie);
			post.addHeader("Referer", preLoginLocation);
			postResponse = httpPostClient.execute(post);

			if (postResponse.getStatusLine().getStatusCode() == HttpStatus.SC_SEE_OTHER) {
				Header[] headers = postResponse.getAllHeaders();
				for (Header header : headers) {
					if (header.getName().equals("Set-Cookie")) {
						cookie = header.getValue();
						break;
					}

				}
				//
				cookie = cookie.substring(0, cookie.indexOf(";")).trim().concat("; joomla_user_state=logged_in");
				String postRespStr = EntityUtils.toString(postResponse.getEntity());
			} else {
				throw new DataUpdaterException(
						"Fail to login - HTTP response code = " + postResponse.getStatusLine().getStatusCode());
			}
		} finally {
			if (postResponse != null)
				postResponse.close();
			if (getResponse != null)
				getResponse.close();
		}
		return cookie;

	}

	// download world index file too //MetaStock FT Index.exe
	private String downloadIndexFile(String fileLocation)
			throws ClientProtocolException, IOException, DataUpdaterException {
		CloseableHttpClient httpGetClient = httpConnectionFactory.getHTTPClient();
		HttpGet httpget = new HttpGet(fileLocation);
		httpget.addHeader("Referer", "http://www.charting.xyz/");
		CloseableHttpResponse response = httpGetClient.execute(httpget);
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String date = df.format(tradingDayChecker.getLastTradingDate());
		String destFileName = "MetaStockFTIndex" + date + ".exe";
		String downloadedFileStr = downloadDestination + destFileName;
		File downloadedFile = new File(downloadedFileStr);
		try {
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				BufferedInputStream bis = new BufferedInputStream(entity.getContent());

				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(downloadedFile));
				int inByte;
				while ((inByte = bis.read()) != -1)
					bos.write(inByte);
				bis.close();
				bos.close();
			} else {
				throw new DataUpdaterException(
						"Fail to download file - HTTP response code = " + response.getStatusLine().getStatusCode());
			}
		} finally {
			response.close();
		}
		return downloadedFileStr;
	}

	private String downloadFile(HttpContext httpContext)
			throws ClientProtocolException, IOException, DataUpdaterException {
		CloseableHttpClient httpGetClient = httpConnectionFactory.getHTTPClient();
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String date = df.format(tradingDayChecker.getLastTradingDate());
		String fileName = "D" + date + "new.exe";
		String historyFileName = "H" + date + ".exe";
		// String fileName = "D20160705.exe";
		String fileLocation = downloadDestination + fileName;
		File downloadedFile = new File(fileLocation);
		File historyDownloadedFile = new File(downloadDestination + historyFileName);
		boolean historyFileExists = false;
		if (!downloadedFile.exists() && !historyDownloadedFile.exists()) {
			// String cookie = getCookie(httpContext);
			String cookie = login(httpContext);
			// http get download file root directory and use jsoup to get eod
			// file url
			HttpGet httpgetFileRoot = new HttpGet(fileLocationRoot);
			httpgetFileRoot.addHeader("Cookie", cookie);
			httpgetFileRoot.addHeader("Referer", "http://www.bizfun.cc/index.php/user-profile/profile");
			// httpget.addHeader("Upgrade-Insecure-Requests", "1");
			CloseableHttpResponse fileRootResponse = null;
			CloseableHttpResponse response = null;
			String fileURL = "";

			try {
				fileRootResponse = httpGetClient.execute(httpgetFileRoot);
				if (fileRootResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// fileURL = "";
					Document doc = Jsoup.parse(EntityUtils.toString(fileRootResponse.getEntity()));
					Elements tds = doc.getElementsByTag("td");
					Iterator<Element> iterator = tds.iterator();
					int i = 0;
					while (iterator.hasNext()) {
						Element tdElement = iterator.next();
						if(enableHistoryFile) {
							if (historyFileName.equals(tdElement.text())) {
								historyFileExists = true;
								break;
							}
						}
						if (fileName.equals(tdElement.text()))
							break;
						i++;
					}
					Element href = tds.get(i - 1).child(0);
					fileURL = "http://www.bizfun.cc/index.php".concat(href.attr("href"));

				} else {
					throw new DataUpdaterException("Fail to get file url - HTTP response code = "
							+ fileRootResponse.getStatusLine().getStatusCode());
				}

				HttpGet httpget = new HttpGet(fileURL);
				httpget.addHeader("Cookie", cookie);
				httpget.addHeader("Referer", fileLocationRoot);
				// httpget.addHeader("Upgrade-Insecure-Requests", "1");
				response = httpGetClient.execute(httpget);

				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					BufferedInputStream bis = new BufferedInputStream(entity.getContent());

					BufferedOutputStream bos = null;
					if (historyFileExists)
						bos = new BufferedOutputStream(new FileOutputStream(historyDownloadedFile));
					else
						bos = new BufferedOutputStream(new FileOutputStream(downloadedFile));
					int inByte;
					while ((inByte = bis.read()) != -1)
						bos.write(inByte);
					bis.close();
					bos.close();
				} else {
					throw new DataUpdaterException(
							"Fail to download file - HTTP response code = " + response.getStatusLine().getStatusCode());
				}
			} finally {
				if (response != null)
					response.close();
				if (fileRootResponse != null)
					fileRootResponse.close();
			}
		}
		if (historyDownloadedFile.exists())
			return downloadDestination + historyFileName;
		else
			return fileLocation;
	}

	private void runUpdate() throws DataUpdaterException {
		try{
			stockDBUpdater.updateDB();
		} catch (StockDBUpdaterException e) {
			e.printStackTrace();
			throw new DataUpdaterException(e.getMessage());
			
		}
	}

	private void extractHistoryFile(String fileName) throws DataUpdaterException {
		System.setProperty(LibraryLoader.JACOB_DLL_PATH, jacodDLLPath);
		AutoItX x = new AutoItX();
		x.run(downloadDestination + fileName + " /auto C:\\");
		x.winActivate("WinZip Self-Extractor");
		x.winWaitActive("WinZip Self-Extractor");
		x.controlClick("WinZip Self-Extractor", "", "[CLASS:Button; TEXT:&Yes; INSTANCE:1]");
		x.winWaitActive("WinZip Self-Extractor - " + fileName);
		boolean success = x.winWaitClose("WinZip Self-Extractor - " + fileName);
		if (!success)
			throw new DataUpdaterException("Updater failed to complete.");
		else
			logger.info("Updater completed successfully.");
	}

	public void delete(File file) throws IOException {

		if (file.isDirectory()) {

			// directory is empty, then delete it
			if (file.list().length == 0) {
				file.delete();
				//System.out.println("Directory is deleted : " + file.getAbsolutePath());

			} else {

				// list all the directory contents
				String files[] = file.list();

				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);

					// recursive delete
					delete(fileDelete);
				}

				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
					//System.out.println("Directory is deleted : " + file.getAbsolutePath());
				}
			}

		} else {
			// if file, then delete it
			file.delete();
			//System.out.println("File is deleted : " + file.getAbsolutePath());
		}
	}

}
