package com.yee.trading.auto.order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.yee.trading.auto.broker.BrokerInterfaceException;
import com.yee.trading.auto.broker.BrokerInterfaceManager;
import com.yee.trading.auto.event.EventProcessor;
import com.yee.trading.auto.event.EventType;
import com.yee.trading.auto.event.OrderStatusEvent;
import com.yee.trading.auto.portfolio.KLSEPortfolioManager;
import com.yee.trading.auto.stockinfo.OrderStatus;
import com.yee.trading.auto.stockinfo.OrderStatus.OrderStatusType;
import com.yee.trading.auto.util.NotificationEventUtil;

/**
 * This class only fire event order complete event 
 * @author czey01
 *
 */
@Component("genericOrderStatusManager")
public class GenericOrderStatusManager implements OrderStatusManager {

	@Autowired
	@Qualifier("simulationBrokerInterfaceManager") 
	private BrokerInterfaceManager simulationBrokerInterfaceManager;
	
	@Autowired
	@Qualifier("hLeBrokingInterfaceManager") 
	private BrokerInterfaceManager brokerInterfaceManager;
	
	@Autowired
	private EventProcessor eventProcessor;
	
	private static final Map<String, OrderStatus> orderStatusMap = new HashMap<String, OrderStatus>();
	private final Logger logger = LoggerFactory
			.getLogger(GenericOrderStatusManager.class);
	
	@Override
	public void checkOrderStatus() {
		try {
			logger.debug("CheckOrderStatus is triggered.");
			List<OrderStatus> simulationOrderStatusList = simulationBrokerInterfaceManager.querySummaryOrdersStatus();
			List<OrderStatus> orderStatusList = brokerInterfaceManager.querySummaryOrdersStatus();
			orderStatusList.addAll(simulationOrderStatusList);
			for(OrderStatus orderStatus : orderStatusList ){
				if(orderStatusMap.containsKey(orderStatus.getBrokerOrderId())) { 
					//existing order, check any change on matched amount
					OrderStatus oldOrderStatus = orderStatusMap.get(orderStatus.getBrokerOrderId());
					//make sure it is not canceled order
					if(oldOrderStatus.getQuantity() > 0 && oldOrderStatus.getQuantity() == orderStatus.getQuantity()){
						//if(orderStatus.getMatchedQuantity() > oldOrderStatus.getMatchedQuantity()){
							//publish events
							if(orderStatus.getOrderStatusType() == OrderStatusType.ALL_MATCHED &&
									oldOrderStatus.getOrderStatusType() != OrderStatusType.ALL_MATCHED	){
								OrderStatusEvent event = new OrderStatusEvent(EventType.ORDER_MATCHED);
								event.setOrderStatus(orderStatus);
								//remove comment after test
								eventProcessor.onEvent(event);
								//System.out.println("--->Order complete event generated");
								//System.out.println("Name = " + orderStatus.getStockName() + " Price=" + orderStatus.getPrice());
								orderStatusMap.put(orderStatus.getBrokerOrderId(), orderStatus);
							}
							
						//} 
					}
				}else{
					//put only first order status
					orderStatusMap.put(orderStatus.getBrokerOrderId(), orderStatus);
					
					//make sure it is a completely match order
					if(orderStatus.getOrderStatusType() == OrderStatusType.ALL_MATCHED && orderStatus.getQuantity() > 0){
						OrderStatusEvent event = new OrderStatusEvent(EventType.ORDER_MATCHED);
						event.setOrderStatus(orderStatus);
						//remove comment after test
						eventProcessor.onEvent(event);
						//System.out.println("--->Order complete event generated");
						//System.out.println("Name = " + orderStatus.getStockName() + " Price=" + orderStatus.getPrice());
					}
					
					
				}
				
				
			}
		} catch (BrokerInterfaceException e) {			
			logger.error("Exception occured in checkOrderStatus", e);
			eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
					"Runtime exception occured in checkOrderStatus - " + e.getMessage()));
		} catch (RuntimeException re) {
			logger.error("Runtime exception occured in checkOrderStatus", re);
			eventProcessor.onEvent(NotificationEventUtil.createNotificationEvent(
					"Runtime exception occured in checkOrderStatus - " + re.getMessage()));
		}
		
	}

}
