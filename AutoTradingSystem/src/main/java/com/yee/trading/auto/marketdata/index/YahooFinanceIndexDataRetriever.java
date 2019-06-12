package com.yee.trading.auto.marketdata.index;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.yee.trading.auto.connection.HTTPConnectionFactory;

/**
 * Analyze market
 * Bullish level - use this for KLCI
 * - down j is just to make sure no sudden event happening over night
 * - if klci is very bullish then we can tolerate more on dow j negative move
 * 1. bullish index > MA 200 and index > MA 10 -> dow j drop < 0.5
 * 2. bullish index > MA 200 and index < MA 10 -> dow j drop < 0.25 
 * 3. bullish index < MA 200 and index > MA 10 -> dow j up > 0 
 * 4. bullish index < MA 200 and index < MA 10 -> stop buying
 * @author czey01
 *
 */
@Component("yahooIndexDataRetriever")
public class YahooFinanceIndexDataRetriever implements IndexDataRetriever{
	@Autowired
	private HTTPConnectionFactory httpConnectionFactory;
	
	private String historicalUrl = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22%5E|CODE|%22%20and%20startDate%20%3D%20%22|FROM_DATE|%22%20and%20endDate%20%3D%20%22|TO_DATE|%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
	private String quoteUrl = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22%5E|CODE|%22)&format=json&env=store://datatables.org/alltableswithkeys&callback=";
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	@Override
	public IndexData retrieveIndexData(Date fromDate, Date toDate, String code) throws IndexDataRetrieverException {
		String formattedURL = historicalUrl.replace("|CODE|", code);
		formattedURL = formattedURL.replace("|FROM_DATE|", dateFormat.format(fromDate));
		formattedURL = formattedURL.replace("|TO_DATE|", dateFormat.format(toDate));
		CloseableHttpClient httpGetClient = httpConnectionFactory.getHTTPClient();
		HttpGet httpget = new HttpGet(formattedURL);
		IndexData indexData = null;
		try {
			CloseableHttpResponse response = httpGetClient.execute(httpget);
			try {
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String json = EntityUtils.toString(response.getEntity(), "UTF-8");

		            Gson gson = new Gson();
		            indexData = gson.fromJson(json, IndexData.class);
				} else {
					//logger.error("Broker connetion HTTP error code returned. HTTP Code = " + response.getStatusLine().getStatusCode());
					throw new IndexDataRetrieverException(
							"HTTP exception response failed. HTTP Code = " + response.getStatusLine().getStatusCode());

				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			//logger.error("Broker connetion client protocal exception.", e);
			e.printStackTrace();
			throw new IndexDataRetrieverException("Yahoo finance connetion client protocal exception.");

		} catch (IOException e) {
			//logger.error("Broker connetion IO exception.", e);
			e.printStackTrace();
			throw new IndexDataRetrieverException("Yahoo finance connetion IO exception.");

		}
		return indexData;
	}

	@Override
	public IndexData retrieveIndexData(String code) throws IndexDataRetrieverException {
		String formattedURL = quoteUrl.replace("|CODE|", code);		
		CloseableHttpClient httpGetClient = httpConnectionFactory.getHTTPClient();
		HttpGet httpget = new HttpGet(formattedURL);
		IndexData indexData = null;
		try {
			CloseableHttpResponse response = httpGetClient.execute(httpget);
			try {
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String json = EntityUtils.toString(response.getEntity(), "UTF-8");

		            Gson gson = new Gson();
		            indexData = gson.fromJson(json, IndexData.class);
				} else {
					//logger.error("Broker connetion HTTP error code returned. HTTP Code = " + response.getStatusLine().getStatusCode());
					throw new IndexDataRetrieverException(
							"HTTP exception response failed. HTTP Code = " + response.getStatusLine().getStatusCode());

				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			//logger.error("Broker connetion client protocal exception.", e);
			e.printStackTrace();
			throw new IndexDataRetrieverException("Yahoo finance connetion client protocal exception.");

		} catch (IOException e) {
			//logger.error("Broker connetion IO exception.", e);
			e.printStackTrace();
			throw new IndexDataRetrieverException("Yahoo finance connetion IO exception.");

		}
		return indexData;
	}

	
}
