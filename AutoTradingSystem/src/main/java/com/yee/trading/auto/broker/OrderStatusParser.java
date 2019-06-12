package com.yee.trading.auto.broker;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.yee.trading.auto.order.OrderType;
import com.yee.trading.auto.stockinfo.OrderStatus;

import java.util.List;

public class OrderStatusParser {
	private static final int msgLength = 20;
	private static final int startIdx = 2;
	private int idx;
	private List<OrderStatus> orderStatusModelList;
	private String response;
	private int total;

	public OrderStatusParser(final String response) {
		this.idx = -1;
		this.total = 0;
		this.response = response;
	}

	public List<OrderStatus> getOrderStatusModelList() {
		return this.orderStatusModelList;
	}

	public int getTotal() {
		return this.total;
	}

	public void parse() throws BrokerInterfaceException {
		this.orderStatusModelList = new ArrayList<OrderStatus>();
		try {
			if (this.response != null && this.response.length() > 0) {
				final String[] split = this.response.trim().split("\\*");
				final String[] split2 = split[0].split("\\,");
				if (split2[0].equals("DC")) {
					this.idx = Integer.parseInt(split2[1]);
					this.total = Integer.parseInt(split[1]);
					OrderStatus orderStatusModel = new OrderStatus();
					int n = 0;
					int n2 = msgLength;
					int n3;
					OrderStatus orderStatusModel2;
					int n4;
					for (int i = startIdx; i < split.length; ++i, n2 = n3, orderStatusModel = orderStatusModel2, n = n4) {
						switch (i - n * msgLength) {
						case 2: {
							orderStatusModel.setBrokerOrderId(split[i]);
							break;
						}
						case 3: {
							orderStatusModel.setEntryDate(split[i]);
							break;
						}
						case 6: {
							orderStatusModel.setEntryTime(split[i]);
							break;
						}
						case 8: {
							if (split[i].equals("B")) {
								orderStatusModel.setOrderType(OrderType.Buy);
							} else if (split[i].equals("S")) {
								orderStatusModel.setOrderType(OrderType.Sell);
							} else {
								orderStatusModel.setOrderType(OrderType.Reduce);
							}

							break;
						}
						case 9: {
							orderStatusModel.setStockCode(split[i]);
							break;
						}
						case 10: {
							orderStatusModel.setStockName(split[i]);
							break;
						}
						case 12: {
							orderStatusModel
									.setQuantity(new Integer(split[i]) / 100);
							break;
						}
						case 13: {
							orderStatusModel.setMatchedQuantity(new Integer(
									split[i]) / 100);
							break;
						}
						case 14: {
							orderStatusModel.setMatchedAmount(new BigDecimal(
									split[i].replace(",", "")).setScale(2));
							break;
						}

						case 17: {
							orderStatusModel.setPrice(new BigDecimal(split[i])
									.setScale(3));
							break;
						}

						}
						n3 = n2;
						orderStatusModel2 = orderStatusModel;
						n4 = n;
						if (i - n2 == 1) {
							n4 = n + 1;
							n3 = n2 + msgLength;
							orderStatusModel.populateData();
							this.orderStatusModelList.add(orderStatusModel);
							orderStatusModel2 = new OrderStatus();
						}
					}
				} else {
					throw new BrokerInterfaceException(
							String.format(
									"Error retrieving order status result. Order summary result returned = %s",
									response));
				}
			} else {
				throw new BrokerInterfaceException(
						String.format(
								"Error retrieving order status result. Order summary result returned = %s",
								response));
			}
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			throw new BrokerInterfaceException(
					String.format(
							"Error processing order status result. Order summary result returned = %s. Error message = %s",
							response, e.getMessage()));
		} 
	}

	public static void main(String[] args) {
		String result = "DC,0*2*1,149484*22/8/2016*22/8/2016*10:32:36*10:32:32*10:32:41*S*0040*OPENSYS*405*32000*32000*10,880.00*100*0.350*0.340*m*All matched.*0*0*1,146001*22/8/2016*22/8/2016*09:22:05*09:21:55*10:30:34*S*0040*OPENSYS*405*0*0*0.00*100*0.350*0.345*c*Cancelled.*0*0*";
		OrderStatusParser parser = new OrderStatusParser(result);
		try {
			parser.parse();
		} catch (BrokerInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(parser.getTotal());
	}
}
