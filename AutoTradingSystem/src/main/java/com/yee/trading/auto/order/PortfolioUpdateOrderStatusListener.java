package com.yee.trading.auto.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.dao.OrderDao;
import com.yee.trading.auto.event.EventProcessor;
import com.yee.trading.auto.event.EventType;
import com.yee.trading.auto.event.OrderEvent;
import com.yee.trading.auto.portfolio.PortfolioManager;
import com.yee.trading.auto.stockinfo.OrderStatus;
import com.yee.trading.auto.util.OrderQueueContainer;

@Component("portfolioUpdateOrderStatusListener")
public class PortfolioUpdateOrderStatusListener implements OrderStatusListener {
	@Autowired
	private EventProcessor eventProcessor;
	@Autowired
	@Qualifier("klsePortfolioManager")
	private PortfolioManager portfolioManager;
	@Autowired
	private OrderDao orderDao;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void orderStatusUpdated(OrderStatus orderStatus) {
		portfolioManager.updatePortfolioByOrderStatus(orderStatus);
		if (orderStatus.getOrderType() == OrderType.Sell) {
			int strategyId = orderDao.getOrderByOrderStatus(orderStatus)
					.getStrategy().getId();
			Order order = OrderQueueContainer.getInstance().deQueueOrder(
					strategyId);
			if (order != null) {
				OrderEvent orderEvent = new OrderEvent(
						EventType.ORDER_GENERATED);
				orderEvent.setOrder(order);
				eventProcessor.onEvent(orderEvent);
			}
		}

	}

}
