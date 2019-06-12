package com.yee.trading.auto.report;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.yee.trading.auto.dao.StrategyDao;
import com.yee.trading.auto.strategy.Strategy;

@Component
public class JFreeEquityChartGenerator implements ChartGenerator {

	@Autowired
	private StrategyDao strategyDao;

	@Value("${chart.path}")
	private String chartPath;
	@Value("${holiday.list}")
	private String holidayList;
	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void generateEquityChart() throws ChartGeneratorException {
		List<Strategy> strategyList = strategyDao.getAllStrategies();
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
		
		Map<Day, BigDecimal> totalSeriesMap = new LinkedHashMap<Day, BigDecimal>();
		for (Strategy strategy : strategyList) {
			TimeSeries series = new TimeSeries(strategy.getName());
			for (EquityTracking equityTracking : strategy.getEquityTrackings()) {
				Day day = new Day(equityTracking.getCloseDate());
				series.add(day, equityTracking.getEquity());
				if (totalSeriesMap.containsKey(day)) {
					// add
					totalSeriesMap.put(
							day,
							equityTracking.getEquity().add(
									totalSeriesMap.get(day)));
				} else {
					// put
					totalSeriesMap.put(day, equityTracking.getEquity());
				}
			}
			timeSeriesCollection.addSeries(series);
		}
		if(strategyList.size() > 1){
			TimeSeries totalSeries = new TimeSeries("Total Equity");
			timeSeriesCollection.addSeries(totalSeries);
		}

		try {
			JFreeChart lineChartObject = ChartFactory.createTimeSeriesChart(
					"Equity Curve", "Day", "Equity(RM)", timeSeriesCollection);
			XYPlot plot = lineChartObject.getXYPlot();

			DateAxis axis = (DateAxis) plot.getDomainAxis();
			axis.setDateFormatOverride(new SimpleDateFormat("d/M/yy"));

			SegmentedTimeline customTimeLine = SegmentedTimeline
					.newMondayThroughFridayTimeline();
			customTimeLine
					.addExceptions(convertDateList(holidayList.split(",")));
			axis.setTimeline(customTimeLine);

			int width = 740; /* Width of the image */
			int height = 480; /* Height of the image */
			File lineChart = new File(chartPath+"EquityCurveChart.jpeg");

			ChartUtilities.saveChartAsJPEG(lineChart, lineChartObject, width,
					height);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ChartGeneratorException(
					"IO error when try to save chart - " + e.getMessage());

		} catch (ParseException e) {
			e.printStackTrace();
			throw new ChartGeneratorException("Error parsing holidays date - "
					+ e.getMessage());
		}
		
	}
	
	private TimeSeries calculateAverage(Map<Day, BigDecimal> seriesMap, int numberOfStrategy){
		BigDecimal divisor = new BigDecimal(numberOfStrategy).setScale(0);
		TimeSeries averageSeries = new TimeSeries("Strategy Average");
		for (Entry<Day, BigDecimal> item : seriesMap.entrySet()) {
			averageSeries.add(item.getKey(), item.getValue().divide(divisor, 3, RoundingMode.HALF_UP));						
		}
		return averageSeries;
	}

	private List<Date> convertDateList(String[] holidayList)
			throws ParseException {
		List<Date> dateList = new ArrayList<Date>(holidayList.length);
		for (String dateStr : holidayList) {
			dateList.add(dateFormat.parse(dateStr));
		}
		return dateList;
	}

}
