package com.yee.trading.auto.broker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.yee.trading.auto.dao.OrderStatusDao;
import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.stockinfo.OrderStatus;
import com.yee.trading.auto.stockinfo.OrderStatus.OrderStatusType;
import com.yee.trading.auto.stockinfo.StockQuote;
import com.yee.trading.auto.util.SpringContext;

@Component("simulationBrokerInterfaceManager")
public class SimulationBrokerInterfaceManager implements BrokerInterfaceManager {
	@Autowired
	private OrderStatusDao orderStatusDao;
	@Autowired
	@Qualifier("autoTradingTaskExecutor")
	private TaskExecutor taskExecutor;
	@Autowired
	private SpringContext springContext;
	@Autowired
	@Qualifier("hLeBrokingInterfaceManager")
	private BrokerInterfaceManager klseBrokerInterfaceManager;
	
	//TODO log to different log file
	
	private final Logger logger = LoggerFactory
			.getLogger(SimulationBrokerInterfaceManager.class);
	@Override
	public String searchStockCode(String stockName)
			throws BrokerInterfaceException {
		//if(1==2)
		return klseBrokerInterfaceManager.searchStockCode(stockName);
		//return "5095";
	}

	@Override
	public void submitOrder(Order order) throws BrokerInterfaceException {
		// TODO Auto-generated method stub
		// start the order status simulator thread here
		OrderStatusSimulator orderStatusSimulator = (OrderStatusSimulator)springContext.getApplicationContext().getBean("orderStatusSimulator");
		orderStatusSimulator.setOrder(order);
		taskExecutor.execute(orderStatusSimulator);
	}

	
	@Override
	public OrderStatus queryOrdersStatus(Order order)
			throws BrokerInterfaceException {
		// TODO Auto-generated method stub
		// get single order status from DB
		return orderStatusDao.getOrderStatusByOrder(order);
	}

	@Override
	public StockQuote queryStockQuote(String stockCode)
			throws BrokerInterfaceException {
		//if(1==2)
		return klseBrokerInterfaceManager.queryStockQuote(stockCode);
//		StockQuote stockQuote = new StockQuote();
//		stockQuote.setStokCode("5095");
//		Map<BigDecimal, Integer> buyQueues = new LinkedHashMap<>();
//		buyQueues.put(new BigDecimal(1.2).setScale(3, RoundingMode.HALF_UP), 90);
//		buyQueues.put(new BigDecimal(1.19).setScale(3, RoundingMode.HALF_UP), 100);
//		buyQueues.put(new BigDecimal(1.18).setScale(3, RoundingMode.HALF_UP), 300);
//		stockQuote.setBuyQueues(buyQueues);
//		Map<BigDecimal, Integer> sellQueues = new LinkedHashMap<>();
//		sellQueues.put(new BigDecimal(1.21).setScale(3, RoundingMode.HALF_UP), 500);
//		sellQueues.put(new BigDecimal(1.22).setScale(3, RoundingMode.HALF_UP), 200);
//		sellQueues.put(new BigDecimal(1.23).setScale(3, RoundingMode.HALF_UP), 200);
//		stockQuote.setSellQueues(sellQueues);
//		return stockQuote;
	}

	@Override
	public void reduceOrder(String brokerOrderId, BigDecimal price, int quatityToReduce)
			throws BrokerInterfaceException {
		// TODO Auto-generated method stub
		// need to reduce the quantity in status table and set to all match (simulate the actual behaviour)
		logger.info(String.format("Order with status id %s has been reduced for %d lot.", brokerOrderId, quatityToReduce));
		OrderStatus orderStatus = orderStatusDao.getOrderStatusByBrokerOrderId(brokerOrderId);
		orderStatus.setQuantity(orderStatus.getQuantity() - quatityToReduce);
		if(orderStatus.getQuantity() == orderStatus.getMatchedQuantity()){
			orderStatus.setOrderStatusType(OrderStatusType.ALL_MATCHED);
		}		
		orderStatusDao.updateOrderStatus(orderStatus);
	}

	@Override
	public List<OrderStatus> querySummaryOrdersStatus()
			throws BrokerInterfaceException {
		Calendar fromCalc = Calendar.getInstance();
		fromCalc.set(Calendar.HOUR_OF_DAY, 0);
		fromCalc.set(Calendar.MINUTE, 0);
		Calendar toCalc = Calendar.getInstance();
		toCalc.set(Calendar.HOUR_OF_DAY, 23);
		toCalc.set(Calendar.MINUTE, 59);
		return orderStatusDao.getAllOrderStatus(fromCalc.getTime(), toCalc.getTime());
	}

}
