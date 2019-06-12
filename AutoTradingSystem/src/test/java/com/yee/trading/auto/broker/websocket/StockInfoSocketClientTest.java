package com.yee.trading.auto.broker.websocket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.yee.trading.auto.stockinfo.StockQuote;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/Spring-Module.xml")
public class StockInfoSocketClientTest {

	@Autowired
	private StockInfoService stockInfoService;

	@Test
	public void testRetrieveStockQuote() {
		 try {
		 StockQuote stockQuote = stockInfoService.retrieveStockQuote("7160");
		//tockQuote stockQuote = retrieveStockQuote();
		System.out.println("stockQuote.current price=" + stockQuote.getCurrentPrice());
		 } catch (StockInfoException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 }
//		StockQuoteThread thread1 = new StockQuoteThread("8443");
//		StockQuoteThread thread2 = new StockQuoteThread("7160");
//		StockQuoteThread thread3 = new StockQuoteThread("4715");
//		thread1.start();
//		thread2.start();
//		thread3.start();
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public class StockQuoteThread extends Thread {
		private String stockCode;

		public StockQuoteThread(String sq) {
			stockCode = sq;
			// TODO Auto-generated constructor stub
		}

		public void run() {
			StockQuote stockQuote;
			try {
				stockQuote = stockInfoService.retrieveStockQuote(stockCode);
				System.out.println("stockQuote for " + stockCode + " current price=" + stockQuote.getCurrentPrice());
			} catch (StockInfoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	@Test
	public void testSearchStockCode() {
		try {
			String stockCode = stockInfoService.searchStockCode("HEVEA");
			System.out.println("stockCode HEVEA =" + stockCode);
			stockCode = stockInfoService.searchStockCode("GHLSYS");
			System.out.println("stockCode GHLSYS =" + stockCode);
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			stockCode = stockInfoService.searchStockCode("POHUAT");
			System.out.println("stockCode POHUAT =" + stockCode);
		} catch (StockInfoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private JSONObject getJSONObjectByMT(JSONArray jsonArray, String mtType) {
		int i = 0;
		for (i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObj = jsonArray.getJSONObject(i);
			if (jsonObj.getString("mt").equals(mtType)) {
				return jsonObj;
			}
		}
		return null;
	}

	private Map<BigDecimal, Integer> createMarketQueue(JSONArray prices, JSONArray volumes) {
		Map<BigDecimal, Integer> marketQueue = new LinkedHashMap<BigDecimal, Integer>();

		for (int i = 0; i < prices.length(); i++) {
			marketQueue.put(convertPrice(prices.getInt(i)), volumes.getInt(i));
		}

		return marketQueue;

	}

	private BigDecimal convertPrice(int number) {
		// Log.debug("number to conver = " + number);
		return new BigDecimal((double) number / 1000).setScale(3, RoundingMode.HALF_UP);
	}

	private StockQuote retrieveStockQuote() {
		String respString = "[{\"data\":{\"1\":633,\"69\":3,\"249\":8191,\"184\":[20160905,20160901,20160829,20160901,20160809,20160801,20160701,20160610,20160401,20160101,20150909,20150101],\"185\":[20160907,20160907,20160902,20160907,20160907,20160831,20160907,20160907,20160630,20160907,20160907,20151231],\"66\":[4410,4440,4400,4440,4390,4290,4450,4480,4540,4380,3970,4070],\"186\":[4580,4580,4410,4580,4580,4440,4580,4580,4450,4580,4580,4380],\"187\":[3,5,4,5,21,22,46,60,63,169,245,246],\"188\":[3,5,4,5,21,22,46,60,63,169,245,246],\"189\":[12690400.0,23271400.0,20401900.0,23271400.0,78459600.0,69503400.0,169815100.0,209863800.0,199616300.0,696248700.0,968766900.0,927749700.0],\"190\":[57127395.0,103701380.0,90108225.0,103701380.0,345369622.0,304426480.0,746105642.0,921125956.0,884852102.0,3046971767.0,4209881540.0,3884786446.0],\"191\":[-325936.0,-5921838.0,-4770225.0,-5921838.0,-3329126.0,4041729.0,-2204500.0,-1195846.0,1884958.0,20081376.0,18319820.0,3815391.0],\"192\":[4600,4600,4460,4600,4600,4460,4600,4600,4630,4640,4640,4700],\"193\":[20160907,20160907,20160829,20160907,20160907,20160829,20160907,20160907,20160404,20160330,20160330,20150414],\"194\":[4370,4370,4370,4370,4280,4280,4250,4250,4230,4050,3850,3750],\"195\":[20160905,20160905,20160829,20160905,20160823,20160823,20160711,20160711,20160523,20160121,20150928,20150812],\"196\":67,\"197\":94,\"198\":92,\"199\":25,\"200\":4486,\"201\":4415,\"202\":4396,\"203\":4654280,\"204\":3827720,\"205\":3391478},\"mt\":\"SC\"},{\"data\":{\"1\":633,\"2\":\"4715\",\"82\":536870911,\"83\":5950,\"84\":3210,\"85\":4600,\"86\":4550,\"87\":4550,\"88\":1,\"89\":2,\"90\":915.0,\"91\":2,\"92\":83,\"93\":6,\"94\":14,\"95\":0,\"96\":0,\"97\":0.0,\"98\":4760,\"99\":0,\"100\":0,\"101\":0,\"102\":0.0,\"103\":0,\"104\":0,\"105\":0,\"106\":0,\"107\":460.0,\"108\":455.0,\"109\":4600,\"110\":0,\"111\":1,\"112\":1,\"113\":460.0,\"114\":0,\"115\":0,\"116\":0.0,\"117\":1,\"118\":1,\"119\":455.0,\"120\":0,\"121\":0,\"122\":0.0,\"123\":0,\"124\":0,\"125\":0.0,\"126\":0,\"127\":0,\"128\":0.0,\"129\":0,\"130\":0,\"131\":0.0,\"152\":4580,\"85\":4600,\"86\":4550,\"153\":4580,\"133\":4550,\"136\":4590},\"mt\":\"SM\"},{\"data\":{\"1\":633,\"132\":1073741823,\"133\":[4550,4530,4510,4500,4490],\"134\":[331,222,150,56,1],\"135\":[13,3,1,4,1],\"136\":[4590,4600,4610,4620,4630],\"137\":[10,711,175,207,328],\"138\":[5,18,10,9,11]},\"mt\":\"SB\"},{\"data\":{\"1\":633,\"132\":1073741823,\"133\":[4030,4020,4010,3320,3310],\"134\":[55,3,23,50,11],\"135\":[1,1,1,1,1],\"136\":[5520,0,0,0,0],\"137\":[98,0,0,0,0],\"138\":[1,0,0,0,0]},\"mt\":\"OQ\"}]";
		// parse quote
		JSONArray responseJson = new JSONArray(respString);
		JSONObject summaryPriceJsonItem = getJSONObjectByMT(responseJson, "SM");
		JSONObject queueJsonItem = getJSONObjectByMT(responseJson, "SB");
		StockQuote stockQuote = new StockQuote();
		stockQuote.setCurrentPrice(
				convertPrice(summaryPriceJsonItem.getJSONObject("data").getInt(DataKeyMapping.STOCK.LAST_DONE_PRICE)));
		stockQuote.setPreviousClosePrice(
				convertPrice(summaryPriceJsonItem.getJSONObject("data").getInt(DataKeyMapping.STOCK.GE_CLOSE_PRICE)));
		stockQuote.setStokCode("8443");
		stockQuote.setBuyQueues(createMarketQueue(
				queueJsonItem.getJSONObject("data").getJSONArray(DataKeyMapping.TOPLIST.BEST_BUY_PRICE),
				queueJsonItem.getJSONObject("data").getJSONArray(DataKeyMapping.TOPLIST.BEST_BUY_QTY)));
		stockQuote.setSellQueues(createMarketQueue(
				queueJsonItem.getJSONObject("data").getJSONArray(DataKeyMapping.TOPLIST.BEST_SELL_PRICE),
				queueJsonItem.getJSONObject("data").getJSONArray(DataKeyMapping.TOPLIST.BEST_SELL_QTY)));
		return stockQuote;
	}
}
