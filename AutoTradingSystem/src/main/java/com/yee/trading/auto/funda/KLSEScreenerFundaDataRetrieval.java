package com.yee.trading.auto.funda;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.yee.trading.auto.connection.HTTPConnectionFactory;

@Component
public class KLSEScreenerFundaDataRetrieval implements FundaDataRetrieval {
	@Autowired
	private HTTPConnectionFactory httpConnectionFactory;
	
	@Value("${funda.data.url:http://www.klsescreener.com/v2/stocks/view/}")
	private String fundaDataUrl;
	
	@Override
	public StockFundaDetails retrieveFundaData(String stockCode)
			throws FundaDataRetrievalException {
		String requestURL = fundaDataUrl.concat(stockCode);
		CloseableHttpClient httpGetClient = httpConnectionFactory.getHTTPClient();
		HttpGet httpget = new HttpGet(requestURL);
		StockFundaDetails fundaDetails = null;
		try {
			httpget.addHeader("Accept", "application/json");
			CloseableHttpResponse response = httpGetClient.execute(httpget);
			try {
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String json = EntityUtils.toString(response.getEntity(), "UTF-8");

		            Gson gson = new Gson();
		            fundaDetails = gson.fromJson(json, StockFundaDetails.class);
				} else {
					//logger.error("Broker connetion HTTP error code returned. HTTP Code = " + response.getStatusLine().getStatusCode());
					throw new FundaDataRetrievalException(
							"HTTP exception response failed. HTTP Code = " + response.getStatusLine().getStatusCode());

				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			//logger.error("Broker connetion client protocal exception.", e);
			e.printStackTrace();
			throw new FundaDataRetrievalException("KLSE Screener connetion client protocal exception.");

		} catch (IOException e) {
			//logger.error("Broker connetion IO exception.", e);
			e.printStackTrace();
			throw new FundaDataRetrievalException("KLSE Screener connetion IO exception.");

		}
		return fundaDetails;
	}
	
}
