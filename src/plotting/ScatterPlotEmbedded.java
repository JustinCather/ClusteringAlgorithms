package plotting;

import java.util.ArrayList;
import java.util.Iterator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import struct.Cluster;

public class ScatterPlotEmbedded
{
	private String x;
	private String y;
	
	public ScatterPlotEmbedded()
	{
	}
	
	public ScatterPlotEmbedded(String x, String y)
	{
		this.SetXY(x, y);
	}
	
	public void SetY(String y)
	{
		this.y = y;
	}
	
	public void SetX(String x)
	{
		this.x = x;
	}
	
	public void SetXY(String x, String y)
	{
		this.x = x;
		this.y = y;
	}
	
	public ChartPanel DrawChart(ArrayList<Cluster> clusters)
	{
		XYSeriesCollection temp = new XYSeriesCollection();
		int count = 1;
		XYSeries centers = new XYSeries("Centroids");
		temp.addSeries(centers);
		for (Cluster c : clusters)
		{
			if(c==null)continue;
			centers.add(c.GetCentroid().GetAttribute(this.x),c.GetCentroid().GetAttribute(this.y));
			XYSeries series = new XYSeries("Cluster" + count);
			count++;
			
			for (int i = 0; i < c.GetDataPoints().size(); i++)
			{
				
				series.add(c.GetDataPoints().get(i).GetAttribute(this.x), c.GetDataPoints().get(i).GetAttribute(this.y));
			}
			
			temp.addSeries(series);
		}
		
		
	
		JFreeChart chart = ChartFactory.createScatterPlot(
	            "Visual Literacy",
	            this.x, this.y, 
	            temp, 
	            PlotOrientation.VERTICAL,
	            true, 
	            true, 
	            false
	        );
		
        NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setVerticalAxisTrace(true);
        chartPanel.setHorizontalAxisTrace(true);

        return chartPanel;
	}
	public ChartPanel DrawChart()
	{
		XYSeriesCollection temp = new XYSeriesCollection();
		JFreeChart chart = ChartFactory.createScatterPlot(
	            "Visual Literacy",
	            "", "", 
	            temp, 
	            PlotOrientation.VERTICAL,
	            true, 
	            true, 
	            false
	        );
		
        NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setVerticalAxisTrace(true);
        chartPanel.setHorizontalAxisTrace(true);

        return chartPanel;
	}
}
