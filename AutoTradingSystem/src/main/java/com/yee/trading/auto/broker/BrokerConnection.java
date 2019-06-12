package com.yee.trading.auto.broker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.yee.trading.auto.connection.HTTPConnectionFactory;

@Component
public class BrokerConnection {
	@Autowired
	private HTTPConnectionFactory httpConnectionFactory;
	private final Logger logger = LoggerFactory.getLogger(BrokerConnection.class);
	@Value("${accountid:8NY0058}")
	private String ACCOUN_TOKEN;

	public String getData(String cookie, String url) throws BrokerInterfaceException, SessionExpiredException {
		// HttpContext httpContext = HttpClientContext.create();
		String data = null;
		CloseableHttpClient httpGetClient = httpConnectionFactory.getHTTPClient();
		HttpGet httpget = new HttpGet(url);
		httpget.addHeader("cookie", cookie);
		try {
			CloseableHttpResponse response = httpGetClient.execute(httpget);
			try {
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						data = EntityUtils.toString(responseEntity);
					}
					if (data != null && data.contains("ID=\"SESSEXPD\"")) {
						throw new SessionExpiredException();
					}
				} else {
					logger.error("Broker connetion HTTP error code returned. HTTP Code = " + response.getStatusLine().getStatusCode());
					throw new BrokerInterfaceException(
							"HTTP exception response failed. HTTP Code = " + response.getStatusLine().getStatusCode());

				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			logger.error("Broker connetion client protocal exception.", e);
			e.printStackTrace();
			throw new BrokerInterfaceException("Broker connetion client protocal exception.");

		} catch (IOException e) {
			logger.error("Broker connetion IO exception.", e);
			e.printStackTrace();
			throw new BrokerInterfaceException("Broker connetion IO exception.");

		}
		return data;

	}

	public String postData(String cookie, String url, Map<String, String> params)
			throws BrokerInterfaceException, SessionExpiredException {
		String data = null;
		CloseableHttpClient httpPostClient = httpConnectionFactory.getHTTPClient();
		HttpPost post = new HttpPost(url);
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		if (cookie != null)
			post.addHeader("cookie", cookie);
		try {
			post.setEntity(new UrlEncodedFormEntity(urlParameters));
			CloseableHttpResponse response = httpPostClient.execute(post);

			try {
				HttpEntity responseEntity1 = response.getEntity();
				//String data1 = EntityUtils.toString(responseEntity1);
//				logger.error("11111111111111111111111 - " + data1);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						data = EntityUtils.toString(responseEntity);
					}
					if (data != null && data.contains("ID=\"SESSEXPD\"")) {
						throw new SessionExpiredException();
					}					
				} else {
					logger.error("Broker connetion HTTP error code returned. HTTP Code = " + response.getStatusLine().getStatusCode());
					throw new BrokerInterfaceException(
							"HTTP exception response failed. HTTP Code = " + response.getStatusLine().getStatusCode());
				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			logger.error("Broker connetion client protocal exception.", e);
			e.printStackTrace();
			throw new BrokerInterfaceException("Broker connetion client protocal exception.");

		} catch (IOException e) {
			logger.error("Broker connetion IO exception.", e);
			e.printStackTrace();
			throw new BrokerInterfaceException("Broker connetion IO exception.");

		}
		return data;
	}

	public String postLoginData(String url, Map<String, String> params)
			throws BrokerInterfaceException {
		StringBuffer data = null;
		CloseableHttpClient httpPostClient = httpConnectionFactory.getHTTPClient();
		HttpPost post = new HttpPost(url);
				
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

	
		try {
			
			
			post.setEntity(new UrlEncodedFormEntity(urlParameters));
			CloseableHttpResponse response = httpPostClient.execute(post);

			try {
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					data = new StringBuffer();
					HttpEntity responseEntity = response.getEntity();
					Header[] hearders = response.getHeaders("Set-Cookie"); 
					if (responseEntity != null ) {
						//TODO extract bid
						//<TD ID="LGFLAG">S</TD><TD ID="LGMSG"></TD><TD ID="CLNT_0">8NY0058,YEE CZE HOUL,19,002,N</TD><TD ID="TIMEOUT">20</TD><TD ID="NEXTTDAY">20160629</TD><TD ID="ALLOWGTD">N</TD>
						String result = EntityUtils.toString(responseEntity);
						if(result != null && result.trim().length() > 0 
								&& result.contains("<TD ID=\"LGFLAG\">S</TD>") && hearders.length > 0){
//							int indexStart = result.indexOf(ACCOUN_TOKEN);
//							String preResult =  result.substring(indexStart);
//							String[] accInfoItems =preResult.substring(0, preResult.indexOf("</TD>")).split(",");
//							if(accInfoItems.length > 3){
//								data.append(accInfoItems[3]);
//								data.append(",");
//							}	
							data = data.append(result)
									.append("#")
									.append(hearders[0].getValue());
							
						}else{
							logger.error("Error retrieving session id. Result = " + result);
							throw new BrokerInterfaceException(
									"Error retrieving session id. Result = " + result);
						}
					}
					
				} else {
					logger.error("Broker connetion HTTP error code returned. HTTP Code = " + response.getStatusLine().getStatusCode());
					throw new BrokerInterfaceException(
							"HTTP exception response failed. HTTP Code = " + response.getStatusLine().getStatusCode());
				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			logger.error("Broker connetion client protocal exception.", e);
			e.printStackTrace();
			throw new BrokerInterfaceException("Broker connetion client protocal exception.");

		} catch (IOException e) {
			logger.error("Broker connetion IO exception.", e);
			e.printStackTrace();
			throw new BrokerInterfaceException("Broker connetion IO exception.");
		} catch (IndexOutOfBoundsException e) {
			logger.error("Broker connetion client protocal exception. Error parsing response.", e);
			e.printStackTrace();
			throw new BrokerInterfaceException("Broker connetion client protocal exception. Error parsing response.");
		}
		return data==null?null:data.toString();
	}
}
