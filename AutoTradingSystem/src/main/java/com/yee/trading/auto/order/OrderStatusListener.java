package com.yee.trading.auto.order;

import com.yee.trading.auto.stockinfo.OrderStatus;

public interface OrderStatusListener {
	
	public void orderStatusUpdated(OrderStatus orderStatus);

}
