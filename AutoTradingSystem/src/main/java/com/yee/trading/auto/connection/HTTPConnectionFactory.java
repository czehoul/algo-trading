package com.yee.trading.auto.connection;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
//define as singleton spring bean + init method in spring context 
public class HTTPConnectionFactory {
	
	private final CloseableHttpClient httpClient;	
	
	public HTTPConnectionFactory(String brokerConnectionHost, int brokerConnectionPort) {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		// Increase max total connection to 200
		cm.setMaxTotal(30);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(25);
		// Increase max connections for broker host and port to 15
		HttpHost brokerHost = new HttpHost(brokerConnectionHost, brokerConnectionPort);
		cm.setMaxPerRoute(new HttpRoute(brokerHost), 20);

		httpClient = HttpClients.custom()
		        .setConnectionManager(cm)
		        .disableCookieManagement()
		        .build();
	}
	
	
	public CloseableHttpClient getHTTPClient(){
		return httpClient;
	}
	
	public void destroy()  {
		try {
			httpClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
