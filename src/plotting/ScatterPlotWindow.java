package plotting;

import java.util.ArrayList;

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

public class ScatterPlotWindow extends ApplicationFrame
{
	private String x;
	private String y;
	
	public ScatterPlotWindow(String title)
	{
		super(title);	
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
	
	public void DrawChart(ArrayList<Cluster> clusters)
	{
		XYSeriesCollection temp = new XYSeriesCollection();
		
		for (Cluster c : clusters)
		{
			XYSeries series = new XYSeries(c.GetClusterType());
			
			for (int i = 0; i < c.GetDataPoints().size(); i++)
			{
				series.add(c.GetDataPoints().get(i).getAttribute(x), c.GetDataPoints().get(i).getAttribute(y));
			}
			
			temp.addSeries(series);
		}
		

		JFreeChart chart = ChartFactory.createScatterPlot(
	            "Clusters",
	            x, y, 
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
        setContentPane(chartPanel);	
	}
}
