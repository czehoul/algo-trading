package com.yee.trading.auto.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.broker.BrokerInterfaceException;
import com.yee.trading.auto.broker.BrokerInterfaceManager;
import com.yee.trading.auto.dao.EquityTrackingDao;
import com.yee.trading.auto.dao.OrderDao;
import com.yee.trading.auto.dao.PortfolioDao;
import com.yee.trading.auto.dao.StrategyDao;
import com.yee.trading.auto.event.EventProcessor;
import com.yee.trading.auto.order.Order;
import com.yee.trading.auto.order.OrderType;
import com.yee.trading.auto.report.EODReportGenerator;
import com.yee.trading.auto.report.EquityTracking;
import com.yee.trading.auto.stockinfo.OrderStatus;
import com.yee.trading.auto.stockinfo.StockQuote;
import com.yee.trading.auto.strategy.Strategy;
import com.yee.trading.auto.util.NotificationEventUtil;
import com.yee.trading.auto.util.OrderQueueContainer;

@Component("klsePortfolioManager")
public class KLSEPortfolioManager implements PortfolioManager {
	// Pass the following in as propeties / hashmap in constructor

	private BigDecimal brokerFeePercent;

	private BigDecimal minimunBrokerFee;

	private BigDecimal stampDutyPercent;

	private BigDecimal maximumStampDuty;

	private BigDecimal clearingFeePercent;

	private BigDecimal maximumclearingFee;
	@Autowired
	private PortfolioDao portfolioDao;
	@Autowired
	private StrategyDao strategyDao;
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private EquityTrackingDao equityTrackingDao;
	@Autowired
	private EventProcessor eventProcessor;
	@Autowired
	private EODReportGenerator eodReportGenerator;
	// do this in constructor for performance reason
	private BigDecimal totalCostPercent;// =
										// brokerFeePercent.add(stampDutyPercent).add(clearingFeePercent).add(new
										// BigDecimal(1));
	private BigDecimal minBrokerLimitExcldCost;// =
												// minimunBrokerFee.divide(brokerFeePercent);
	private BigDecimal minBrokerLimitIncCost;// =
												// minBrokerLimitExcldCost.multiply(totalCostPercent);
	private BigDecimal costPercentExclBroker;// =
												// totalCostPercent.subtract(brokerFeePercent);
	// private BigDecimal costPercentExclBrokerStmpDuty;// =
	// costPercentExclBroker.subtract(stampDutyPercent);
	// private BigDecimal minimunLimitExcldCost;

	@Autowired
	@Qualifier("simulationBrokerInterfaceManager") 
	private BrokerInterfaceManager simulationBrokerInterfaceManager;
	
	@Autowired
	@Qualifier("hLeBrokingInterfaceManager") 
	private BrokerInterfaceManager brokerInterfaceManager;
	
	private final Logger logger = LoggerFactory
			.getLogger(KLSEPortfolioManager.class);

	public KLSEPortfolioManager(
			@Value("${broker.fee.percent:0.00106}") BigDecimal brokerFeePercent,
			@Value("${broker.fee.minimum:8.48}") BigDecimal minimunBrokerFee,
			@Value("${stamp.duty.percent:0.001}") BigDecimal stampDutyPercent,
			@Value("${stamp.duty.maximum:200}") BigDecimal maximumStampDuty,
			@Value("${clearing.fee.percent:0.000318}") BigDecimal clearingFeePercent,
			@Value("${clearing.fee.maximum:200}") BigDecimal maximumclearingFee) {
		this.brokerFeePercent = brokerFeePercent;
		this.minimunBrokerFee = minimunBrokerFee;
		this.stampDutyPercent = stampDutyPercent;
		this.maximumStampDuty = maximumStampDuty;
		this.clearingFeePercent = clearingFeePercent;
		this.maximumclearingFee = maximumclearingFee;
		totalCostPercent = brokerFeePercent.add(stampDutyPercent)
				.add(clearingFeePercent).add(new BigDecimal(1));
		minBrokerLimitExcldCost = minimunBrokerFee.divide(brokerFeePercent);
		minBrokerLimitIncCost = minBrokerLimitExcldCost
				.multiply(totalCostPercent);
		costPercentExclBroker = totalCostPercent.subtract(brokerFeePercent);
		// costPercentExclBrokerStmpDuty =
		// costPercentExclBroker.subtract(stampDutyPercent);
		// minimunLimitExcldCost = minimunBrokerFee.divide(brokerFeePercent);
	}

	// public int getNumberOfLot(int strategyId, BigDecimal buyPrice) {
	// //before submitting order hv to insert to db as hold first
	// //only remove/update when end of the day to db after checking order
	// status
	// portfolioDao.getPortfoliosByStrategyId(strategyId, true);
	// //if already submitted
	// return 0;
	// }

	private int calculateShareQuantity(Order order, BigDecimal allocatedCash, int availablePosition) throws PortfolioManagerException {
		
		BigDecimal shareAmount = null;
		BigDecimal zero = new BigDecimal(0);
		if(allocatedCash.compareTo(zero) < 1 || availablePosition <= 0) {
			throw new PortfolioManagerException("Not enough available cash or position for this strategy to create portfolio.");
		}
		
		shareAmount = allocatedCash.divide(totalCostPercent, 3,
				RoundingMode.HALF_UP);
		//TDOD in future need to factor in max clearing fee and max stamp duty too
		// stamp duty - 0.1% round to nearest ringgit maximum 200 if(amount
		// > 200,000)
		// clearing fee - 0.03% max 200 if(amount > 666,666.67)
		// use similar way as below to calculate
		if (allocatedCash.compareTo(minBrokerLimitIncCost) == -1) {
			shareAmount = allocatedCash.divide(costPercentExclBroker, 3,
					RoundingMode.HALF_UP).subtract(minimunBrokerFee);
		}
		BigDecimal quantityInDecimal = shareAmount.divide(order.getPrice(), 3,
				RoundingMode.HALF_UP).divide(new BigDecimal(100), 3,
				RoundingMode.HALF_UP);
		quantityInDecimal = quantityInDecimal.setScale(0, RoundingMode.DOWN);
		return quantityInDecimal.intValue();
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Order createPortfolio(Order order)
			throws PortfolioManagerException {
		Strategy strategy = strategyDao.getStrategyById(order.getStrategy()
				.getId());
		if (strategy.getAvailablePosition() == 0) {
			throw new PortfolioManagerException("Not enough available position for this strategy to create portfolio.");
		}
		//min(available cash or total equity/total position) 
		BigDecimal allocatedCash = strategy.getAvailableCash().divide(
				new BigDecimal(strategy.getAvailablePosition()), 3,
				RoundingMode.HALF_DOWN);
		BigDecimal allocatedEquity = strategy.getTotalEquity().divide(
				new BigDecimal(strategy.getMaxPortfolioNumber()), 3,
				RoundingMode.HALF_DOWN);
		allocatedCash = allocatedCash.min(allocatedEquity);
		
		int quantity = 0;
		if(order.getQuantity() > 0){
			quantity = order.getQuantity();
		} else{
			quantity = calculateShareQuantity(order, allocatedCash, strategy.getAvailablePosition());
		}

		if (quantity == 0) {
			throw new PortfolioManagerException("Not enough available cash or position for this strategy to create portfolio.");
		}		
		
		BigDecimal shareAmount = order.getPrice().multiply(new BigDecimal(quantity*100));
		
		BigDecimal shareAmountWithCost = calcShareAmountWithCost(shareAmount);

		strategy.setAvailableCash(strategy.getAvailableCash().subtract(
				shareAmountWithCost));
		strategy.setAvailablePosition(strategy.getAvailablePosition() - 1);
		Portfolio portfolio = new Portfolio();
		portfolio.setBuyDate(order.getOrderDate());
		portfolio.setBuyPrice(order.getPrice());
		portfolio.setHold(true);
		portfolio.setQuantity(quantity);
		portfolio.setStockCode(order.getStockCode());
		portfolio.setStockName(order.getStockName());
		portfolio.setStrategy(strategy);
		portfolio.setTotalAmount(shareAmount);
		portfolio.setTotalAmountIncCost(shareAmountWithCost);
		portfolio.setAllocatedCash(allocatedCash);
		order.setQuantity(quantity);
		order.setPortfolio(portfolio);			
		portfolioDao.cretePortfolio(portfolio);
		strategyDao.updateStrategy(strategy);
		return createOrder(order);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Order recreateOrder(Order previousOrder, OrderStatus previousOrderStatus, Order newOrder) throws PortfolioManagerException{
		if(previousOrder.getOrderType() == OrderType.Buy) {
			Portfolio portfolio = portfolioDao.getPortfolioById(newOrder.getPortfolio().getId());
			BigDecimal preMatchedAmount = calculateTotalOrderAmount(portfolio.getOrders(), true).getAmount();
			BigDecimal matchAmount = previousOrderStatus.getPrice().multiply(new BigDecimal(previousOrderStatus.getMatchedQuantity()*100));
			BigDecimal totalMatchedAmount = preMatchedAmount.add(matchAmount);
			BigDecimal totalMatchAmountIncCost = calcShareAmountWithCost(totalMatchedAmount);
			BigDecimal allocatedCash = portfolio.getAllocatedCash().subtract(totalMatchAmountIncCost);
			newOrder.setQuantity(calculateShareQuantity(newOrder, allocatedCash, 1));
		}else{			
			newOrder.setQuantity(previousOrder.getQuantity() - previousOrderStatus.getMatchedQuantity());
		}
		//previousOrder.setOriginalQuantity(previousOrder.getQuantity());
		previousOrder.setQuantity(previousOrderStatus.getMatchedQuantity());
		previousOrder.setDone(true);
		orderDao.updateOrder(previousOrder);
		newOrder.setOriginalQuantity(newOrder.getQuantity());
		newOrder.setDone(false);
		newOrder.setSubmitCount(previousOrder.getSubmitCount() + 1);
		return orderDao.createOrder(newOrder);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void reduceOrder(Order order, int quantity){
		order.setQuantity(order.getQuantity() - quantity);
		order.setDone(true);
		orderDao.updateOrder(order);		
		Portfolio portfolio =  order.getPortfolio();
		updatePortfolioStrategy(portfolio);		
	}
	
	private Order calculateTotalOrderAmount(Set<Order> orders, boolean done){
		BigDecimal totalAmount = new BigDecimal(0);
		int totalQuantity = 0;
		Order consolidatedOrder = new Order();
		if(orders != null){
			for(Order order : orders){
				if(order.isDone() == done && 
						order.getOrderDate().after(getTodayStartTime()) && 
						order.getOrderDate().before(getTodayEndTime())){
					totalAmount = totalAmount.add(order.getAmount());
					totalQuantity = totalQuantity + order.getQuantity();
				}
			}
		}
		consolidatedOrder.setAmount(totalAmount);
		consolidatedOrder.setQuantity(totalQuantity);
		return consolidatedOrder;
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Order createOrder(Order newOrder) {		
		if(newOrder.getOrderType() == OrderType.Sell) {
			Portfolio portfolio = portfolioDao.getStrategyPortfolioByStockCode(
					newOrder.getStrategy().getId(), newOrder.getStockCode());
			portfolio.setSellDate(newOrder.getOrderDate());
			portfolio.setHold(false);
			portfolio.setSellPrice(newOrder.getPrice());
			newOrder.setQuantity(portfolio.getQuantity());
			portfolioDao.updatePortfolio(portfolio);
			newOrder.setPortfolio(portfolio);
		}		
		newOrder.setOriginalQuantity(newOrder.getQuantity());
		newOrder.setSubmitCount(1);
		return orderDao.createOrder(newOrder);
	}
	
	// @Override
	// @Transactional
//	private int getShareQuantity(Strategy strategy, Order order) {
//
//		// if (order.getOrderType() == OrderType.Buy) {
//		// Strategy strategy =
//		// strategyDao.getStrategyById(order.getStrategy().getId());
//		// calculate lot/share based on above trading cost
//		// 0.106% min RM 8.48
//		// stamp duty - 0.1% round to nearest ringgit maximum 200 if(amount
//		// > 200,000)
//		// clearing fee - 0.03% max 200 if(amount > 666,666.67)
//		// allocated cash for this position
//
//		BigDecimal allocatedCash = strategy.getAvailableCash().divide(
//				new BigDecimal(strategy.getAvailablePosition()), 3,
//				RoundingMode.HALF_UP);
//		BigDecimal shareAmount = allocatedCash.divide(totalCostPercent, 3,
//				RoundingMode.HALF_UP);
//		if (allocatedCash.compareTo(minBrokerLimitIncCost) == -1) {
//			shareAmount = allocatedCash.divide(costPercentExclBroker, 3,
//					RoundingMode.HALF_UP).subtract(minimunBrokerFee);
//		}
//		BigDecimal quantity = shareAmount.divide(order.getPrice(), 3,
//				RoundingMode.HALF_UP).divide(new BigDecimal(100), 3,
//				RoundingMode.HALF_UP);
//		quantity = quantity.setScale(0, RoundingMode.DOWN);
//		return quantity.intValue();
//		// }
//		// else {
//		// return
//		// portfolioDao.getStrategyPortfolioByStockCode(order.getStrategyId(),
//		// order.getStockCode())
//		// .getQuantity();
//		// }
//
//		// BigDecimal estTotalCostPercent =
//		// brokerFeePercent.add(brokerFeePercent).add(stampDutyPercent).add(clearingFeePercent).add(new
//		// BigDecimal(1));
//
//		// BigDecimal shareAmount = allocatedCash.divide(estTotalCostPercent);
//
//		// BigDecimal brokerFee =
//		// shareAmount.multiply(brokerFeePercent).max(minimunBrokerFee);
//		//
//		// if(brokerFee.compareTo(minimunBrokerFee))
//		//
//		// BigDecimal stampDuty = shareAmount.multiply(new
//		// BigDecimal(0.001)).min(new BigDecimal(200));
//		// if(stampDuty.compareTo(new BigDecimal(200)) == 0){
//		// shareAmount = allocatedCash.divide(new
//		// BigDecimal(1.00136)).subtract(new BigDecimal(200));
//		// }
//		// BigDecimal clearingFee = shareAmount.multiply(new
//		// BigDecimal(0.0003)).min(new BigDecimal(200));
//		// shareAmount =
//		// allocatedCash.subtract(brokerFee).subtract(stampDuty).subtract(clearingFee);
//		// BigDecimal quantity = shareAmount.divide(buyPrice, 3,
//		// RoundingMode.HALF_UP).divide(new BigDecimal(100), 3,
//		// RoundingMode.HALF_UP);
//		// quantity = quantity.setScale(0, RoundingMode.DOWN);
//		// return quantity.intValue();
//		// = Y + CF + SD + BF
//		// = Y + (0.0003 * Y) + (0.001 * Y) + (0.00106 * Y)
//		// = 1.00236*Y
//		// = 1.00236*P*X
//		// X = allocatedCash / (1.00236*P)
//		// CF = min(0.0003 * Y, 200)
//		// SD = min(round(0.001 * Y, 200))
//		// BF = max(0.00106 * Y, 8.48)
//		// y = total amount = P * X
//		// return 0;
//		//
//		// condition
//		// actualShareAmount = allocatedCash/1.00236;
//		// BF = max(actualShareAmount * 0.00106, 8.48);
//		// SD=min(actualShareAmount * 0.001, 200000);
//		// if SD == 200000
//		// actualShareAmount = allocatedCash/1.00136 - 200;
//		// CF= min(actualShareAmount * 0.0003, 200);
//		//
//
//	}

	private BigDecimal calcShareAmountWithCost(BigDecimal shareAmount) {
		BigDecimal shareAmountWithCost = shareAmount;// shareAmount.multiply(totalCostPercent);
		BigDecimal zero = new BigDecimal(0);
		if (shareAmount.compareTo(zero) == 0)
			return zero;
		// add broker fee
		if (shareAmount.compareTo(minBrokerLimitExcldCost) == -1) {
			shareAmountWithCost = shareAmountWithCost.add(minimunBrokerFee);
		} else {
			shareAmountWithCost = shareAmountWithCost.add(shareAmount
					.multiply(brokerFeePercent));
		}
		// add stamp duty
		BigDecimal stampDuty = shareAmount.multiply(stampDutyPercent)
				.setScale(0, RoundingMode.UP).min(maximumStampDuty);
		shareAmountWithCost = shareAmountWithCost.add(stampDuty);
		// add clearing fee
		BigDecimal clearingFee = shareAmount.multiply(clearingFeePercent).min(
				maximumclearingFee);
		shareAmountWithCost = shareAmountWithCost.add(clearingFee);
		return shareAmountWithCost.setScale(2, RoundingMode.UP);
	}

//	@Override
//	public int removePortfolio(Order order) {
//		Portfolio portfolio = portfolioDao.getStrategyPortfolioByStockCode(
//				order.getStrategy().getId(), order.getStockCode());
//		portfolio.setHold(false);
//		portfolio.setSellPrice(order.getPrice());
//		portfolioDao.updatePortfolio(portfolio);
//		Strategy strategy = portfolio.getStrategy();
//		strategy.setAvailablePosition(strategy.getAvailablePosition() + 1);
//		BigDecimal availableCash = strategy.getAvailableCash();
//		BigDecimal sellingCost = calcShareAmountWithCost(
//				portfolio.getTotalAmount())
//				.subtract(portfolio.getTotalAmount());
//		availableCash = availableCash.add(portfolio.getTotalAmount().subtract(
//				sellingCost));
//		strategy.setAvailableCash(availableCash);
//		strategyDao.updateStrategy(strategy);
//		return portfolio.getQuantity();
//	}

//	private void removePortfolio(OrderStatus orderStatus, Portfolio portfolio) {
//		// Portfolio portfolio =
//		// portfolioDao.getStrategyPortfolioByStockCode(order.getStrategyId(),
//		// order.getStockCode());
//		Strategy strategy = portfolio.getStrategy();
//		if (orderStatus.getOrderStatusType() == OrderStatusType.PARTIALLY_MATCHED) {
//			portfolio.setHold(true);
//			portfolio.setQuantity(orderStatus.getQuantity()
//					- orderStatus.getMatchedQuantity());
//			portfolio.setTotalAmount(portfolio.getTotalAmount().subtract(
//					orderStatus.getMatchedAmount()));
//			portfolio.setTotalAmountIncCost(calcShareAmountWithCost(portfolio
//					.getTotalAmount()));
//		} else if (orderStatus.getOrderStatusType() == OrderStatusType.ALL_MATCHED) {
//			portfolio.setHold(false);
//			strategy.setAvailablePosition(strategy.getAvailablePosition() + 1);
//		} else {
//			portfolio.setSellDate(null);
//		}
//		portfolio.setSellPrice(orderStatus.getPrice());
//		portfolioDao.updatePortfolio(portfolio);
//
//		if (orderStatus.getOrderStatusType() == OrderStatusType.PARTIALLY_MATCHED
//				|| orderStatus.getOrderStatusType() == OrderStatusType.ALL_MATCHED) {
//			BigDecimal availableCash = strategy.getAvailableCash();
//			BigDecimal sellingCost = calcShareAmountWithCost(
//					orderStatus.getMatchedAmount()).subtract(
//					orderStatus.getMatchedAmount());
//			availableCash = availableCash.add(orderStatus.getMatchedAmount()
//					.subtract(sellingCost));
//			strategy.setAvailableCash(availableCash);
//			strategyDao.updateStrategy(strategy);
//		}
//	}
	
	private Date getTodayStartTime(){
		Calendar startCalc = Calendar.getInstance();
		startCalc.set(Calendar.HOUR_OF_DAY, 0);
		startCalc.set(Calendar.MINUTE, 0);
		startCalc.set(Calendar.SECOND, 0);
		startCalc.set(Calendar.MILLISECOND, 0);
		return startCalc.getTime();
	}
	
	private Date getTodayEndTime(){
		Calendar stopCalc = Calendar.getInstance();
		stopCalc.set(Calendar.HOUR_OF_DAY, 23);
		stopCalc.set(Calendar.MINUTE, 59);
		stopCalc.set(Calendar.SECOND, 59);
		stopCalc.set(Calendar.MILLISECOND, 999);
		return stopCalc.getTime();
	}
	
	private void updateActiveOrder() throws BrokerInterfaceException, PortfolioManagerException {
		
		List<OrderStatus> orderStatusList = brokerInterfaceManager.querySummaryOrdersStatus();
		//TODO if simulation mode, get this from DB
		List<OrderStatus> simulationOrderStatusList = simulationBrokerInterfaceManager.querySummaryOrdersStatus();
		orderStatusList.addAll(simulationOrderStatusList);
		
		List<Order> orderList = orderDao.getActiveOrders(getTodayStartTime(), getTodayEndTime());
		for(Order order : orderList){
			updateOrder(order, orderStatusList);
		}
		
	}
	
	private void updateOrder(Order order, List<OrderStatus> orderStatusList) throws PortfolioManagerException {
		OrderStatus foundOrderStatus = null;
		Calendar startTime = Calendar.getInstance();
		startTime.setTime(order.getOrderDate());
		startTime.add(Calendar.MINUTE, -1);
		Calendar endTime = Calendar.getInstance();
		endTime.setTime(order.getOrderDate());
		endTime.add(Calendar.MINUTE, 1);
		for(OrderStatus orderStatus : orderStatusList){			
			if(order.getStockName().equals(orderStatus.getStockName()) &&
					order.getQuantity() == orderStatus.getQuantity() &&
					order.getOrderType() == orderStatus.getOrderType() &&
					order.getPrice().compareTo(orderStatus.getPrice()) == 0 &&
					orderStatus.getOrderDate().after(startTime.getTime()) && 
					orderStatus.getOrderDate().before(endTime.getTime())){
				foundOrderStatus = orderStatus;
				break;
			}
		}
		if(foundOrderStatus != null){
			//order.setOriginalQuantity(order.getQuantity());
			order.setQuantity(foundOrderStatus.getMatchedQuantity());
			order.setDone(true);
			orderDao.updateOrder(order);
		} else{
			throw new PortfolioManagerException("Order status nout found for order " + order.toString());
		}
	}

//	private Portfolio findPortfolio(OrderStatus orderStatus, OrderType orderType) {
//		Calendar startCalc = Calendar.getInstance();
//		startCalc.set(Calendar.HOUR_OF_DAY, 0);
//		startCalc.set(Calendar.MINUTE, 0);
//		startCalc.set(Calendar.SECOND, 0);
//		startCalc.set(Calendar.MILLISECOND, 0);
//		Calendar stopCalc = Calendar.getInstance();
//		stopCalc.set(Calendar.HOUR_OF_DAY, 23);
//		stopCalc.set(Calendar.MINUTE, 59);
//		stopCalc.set(Calendar.SECOND, 59);
//		stopCalc.set(Calendar.MILLISECOND, 999);
//		List<Portfolio> portfolios = portfolioDao.getPortfolioByStockCode(
//				orderStatus.getStockCode(), orderType, startCalc.getTime(),
//				stopCalc.getTime());
//		Portfolio foundPortfolio = null;
//		for (Portfolio portfolio : portfolios) {
//			if (portfolio.getQuantity() == orderStatus.getQuantity()
//					&& portfolio.isHold()) {
//				foundPortfolio = portfolio;
//			}
//		}
//		return foundPortfolio;
//	}

	/**
	 * EOD daily status update - orders, portfolio and strategy are updated for
	 * unmatched and partially matched order 
	 * All matched order is updated by listener
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updatePortfolioByOrderStatus() {
		try {
//			List<OrderStatus> orderStatusList = null;
//			if (1 == 2)
//				orderStatusList = brokerInterfaceManager.queryOrdersStatus();
//			else
//				orderStatusList = new ArrayList<OrderStatus>();
//			OrderStatus mockOrderStatus = new OrderStatus();
//			mockOrderStatus.setStockCode("9326");
//			mockOrderStatus.setStockName("LBALUM");
//			mockOrderStatus.setMatchedAmount(new BigDecimal(6930));
//			mockOrderStatus.setMatchedQuantity(110);
//			mockOrderStatus.setPrice(new BigDecimal(0.63).setScale(3,
//					RoundingMode.HALF_UP));
//			mockOrderStatus.setQuantity(127);
//			mockOrderStatus.setOrderType(OrderType.Sell);
//			mockOrderStatus
//					.setOrderStatusType(OrderStatusType.PARTIALLY_MATCHED);
//			orderStatusList.add(mockOrderStatus);
			
			//Clear order queue
			OrderQueueContainer.getInstance().clearQueue();
			
			List<Portfolio> portFolios = portfolioDao
					.getPortfolioWithActiveOrder(getTodayStartTime(),
							getTodayEndTime());
			List<Portfolio> nonCompletedPortFolios = new ArrayList<Portfolio>();
			
			for(Portfolio portfolio : portFolios ){
				Set<Order> orderList = portfolio.getOrders();
				//List<Order> orderList = orderDao.getActiveOrders(getTodayStartTime(), getTodayEndTime());
				for(Order order : orderList){
					if(!order.isDone()){
						nonCompletedPortFolios.add(portfolio);
						break;
					}
				}				
			}

			updateActiveOrder();
			
			//get today buy/sell portfolio
			//for each portfolio get all its orders
			//add all quantity * price = amount / amount inc cost for the order and update the portfolio
			//hv to add all quantity too and update so that when sell we know how much to sell
			//finally call update equity
			
			for (Portfolio portfolio : nonCompletedPortFolios) {
				updatePortfolioStrategy(portfolio);

			}
			updateStrategiesEquity();
			eodReportGenerator.generateReport();
		} catch (BrokerInterfaceException e) {
			logger.error("Error update EOD portfolio. Error getting order status - "
					+ e.getMessage());
			eventProcessor
					.onEvent(NotificationEventUtil
							.createNotificationEvent("Error update EOD portfolio. Error getting order status - "
									+ e.getMessage()));
		} catch (PortfolioManagerException e) {
			logger.error("Error update EOD portfolio. " + e.getMessage());
			eventProcessor.onEvent(NotificationEventUtil
					.createNotificationEvent("Error update EOD portfolio. "
							+ e.getMessage()));
		}

	}

	private void updatePortfolioStrategy(Portfolio portfolio) {
		Set<Order> orders = portfolio.getOrders();
		BigDecimal zero = new BigDecimal(0);
		Order consolidatedOrder = calculateTotalOrderAmount(orders,
				true);
		Strategy portfolioStrategy = portfolio.getStrategy();
		if (consolidatedOrder.getAmount().compareTo(zero) == 1
				&& consolidatedOrder.getQuantity() > 0) { // partial /
															// all match
			BigDecimal actShareAmountWithCost = calcShareAmountWithCost(consolidatedOrder
					.getAmount());
			BigDecimal totalAmountIncCostDiff = portfolio
					.getTotalAmountIncCost().subtract(
							actShareAmountWithCost);

			// for sell if not completely match have to handle too
			if (portfolio.isHold()) { // buy
//				if (portfolio.getQuantity() > consolidatedOrder
//						.getQuantity()) { // partially match/all match (no different)
					portfolio.setQuantity(consolidatedOrder
							.getQuantity());
					portfolio.setTotalAmount(consolidatedOrder
							.getAmount());
					portfolio
							.setTotalAmountIncCost(actShareAmountWithCost);
					portfolio.setBuyPrice(portfolio.getTotalAmount()
							.divide(new BigDecimal(portfolio
									.getQuantity() * 100), 3,
									RoundingMode.HALF_UP));
					// calculate the different
					portfolioStrategy
							.setAvailableCash(portfolioStrategy
									.getAvailableCash().add(
											totalAmountIncCostDiff));
					portfolioDao.updatePortfolio(portfolio);
					strategyDao.updateStrategy(portfolioStrategy);
			//	} 
			} else { // sell
				BigDecimal transactionCost = actShareAmountWithCost
						.subtract(consolidatedOrder.getAmount());
				if (portfolio.getQuantity() > consolidatedOrder
						.getQuantity()) { // partially match

					portfolio.setHold(true);
					portfolio.setQuantity(portfolio.getQuantity()
							- consolidatedOrder.getQuantity());
					portfolio.setTotalAmount(portfolio.getTotalAmount()
							.subtract(consolidatedOrder.getAmount()));
					portfolio
							.setTotalAmountIncCost(calcShareAmountWithCost(portfolio
									.getTotalAmount()));
					portfolio.setSellPrice(portfolio.getTotalAmount()
							.divide(new BigDecimal(portfolio
									.getQuantity() * 100), 3,
									RoundingMode.HALF_UP));
					// TODO send notification

				} else { // all match
					portfolio.setHold(false);
					portfolioStrategy
							.setAvailablePosition(portfolioStrategy
									.getAvailablePosition() + 1);
				}

				portfolioStrategy.setAvailableCash(portfolioStrategy
						.getAvailableCash()
						.add(consolidatedOrder.getAmount())
						.subtract(transactionCost));
				strategyDao.updateStrategy(portfolioStrategy);
				portfolioDao.updatePortfolio(portfolio);
			}

		} else { // no match at all
			if (portfolio.isHold()) { // buy
				portfolioStrategy.setAvailableCash(portfolioStrategy
						.getAvailableCash().add(
								portfolio.getTotalAmountIncCost()));
				portfolioStrategy
						.setAvailablePosition(portfolioStrategy
								.getAvailablePosition() + 1);
				strategyDao.updateStrategy(portfolioStrategy);
				portfolioDao.deletePortfolio(portfolio);
			} else { // sell
				// TODO if sell have to handle it with notification
				portfolio.setSellDate(null);
				portfolio.setHold(true);
				portfolio.setSellPrice(null);
				portfolioDao.updatePortfolio(portfolio);
			}
		}
	}
	
	private void updateStrategiesEquity() throws BrokerInterfaceException {
		List<Strategy> strategies = strategyDao.getAllStrategies();
		Date today = new Date();
		for (Strategy strategy : strategies) {
			List<Portfolio> portfolios = portfolioDao
					.getPortfoliosByStrategyId(strategy.getId(), true);
			BigDecimal totalShareValue = new BigDecimal(0);
			for (Portfolio portfolio : portfolios) {
				StockQuote stockQuote = brokerInterfaceManager
						.queryStockQuote(portfolio.getStockCode());
				
				totalShareValue = totalShareValue.add(stockQuote.getCurrentPrice().multiply(
						new BigDecimal(portfolio.getQuantity() * 100)));
				portfolio.setLastClosePrice(stockQuote.getCurrentPrice());
				portfolioDao.updatePortfolio(portfolio);
			}
			strategy.setTotalEquity(strategy.getAvailableCash().add(
					totalShareValue));
			EquityTracking equityTracking = new EquityTracking();
			equityTracking.setCloseDate(today);
			equityTracking.setStrategy(strategy);
			equityTracking.setEquity(strategy.getTotalEquity());
			equityTrackingDao.createEquityTracking(equityTracking);
			strategyDao.updateStrategy(strategy);
		}
	}

	/**
	 * This method will be called by listener, for only completed/all matched order 
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updatePortfolioByOrderStatus(OrderStatus orderStatus) {
		// update order based on order status
		Order order = orderDao.getOrderByOrderStatus(orderStatus);
		order.setOriginalQuantity(order.getQuantity());
		order.setQuantity(orderStatus.getMatchedQuantity());
		order.setDone(true);
		orderDao.updateOrder(order);
		
		// update portfolio and strategy
		Portfolio portfolio =  order.getPortfolio();
		updatePortfolioStrategy(portfolio);
	}

	// @Override
	// @Transactional
	// public Portfolio markPortfolioAsSell(Order order) {
	// Portfolio portfolio = portfolioDao.getStrategyPortfolioByStockCode(
	// order.getStrategy().getId(), order.getStockCode());
	// portfolio.setSellDate(order.getOrderDate());
	// portfolio.setHold(false);
	// portfolioDao.updatePortfolio(portfolio);
	// return portfolio;
	// }

}
