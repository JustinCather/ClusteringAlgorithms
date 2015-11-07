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
	
	public ChartPanel DrawChart(Iterator<Cluster> clusters)
	{
		XYSeriesCollection temp = new XYSeriesCollection();
		int count = 1;
		
		while (clusters.hasNext())
		{
			Cluster c = clusters.next();
			XYSeries series = new XYSeries("Cluster" + count);
			count++;
			
			for (int i = 0; i < c.GetDataPoints().size(); i++)
			{
				series.add(c.GetDataPoints().get(i).getAttribute(this.x), c.GetDataPoints().get(i).getAttribute(this.y));
			}
			
			temp.addSeries(series);
		}
	
		JFreeChart chart = ChartFactory.createScatterPlot(
	            "Plot",
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
}
