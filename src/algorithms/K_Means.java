package algorithms;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import gui.UserGUI;
import jxl.read.biff.BiffException;
import struct.Cluster;
import struct.DataPoint;
import struct.DataSet;

public class K_Means implements I_Algorithm {
	
	private DataSet dataSet;
	private boolean isRunning;
	private int numClusters;
	private UserGUI userGUI;
	private ArrayList<DataPoint> prvCentroids;
	private ArrayList<DataPoint> crtCentroids;
	private ArrayList<Cluster> clusters;
	
	public K_Means()
	{
		isRunning = false;
	}

	@Override
	public void start() throws InterruptedException {
		if (dataSet == null)
		{
			gui.MessageBox.show("No data set.", "No dataset");
			return;
		}
		else
		{
			isRunning = true;
			pickInitCentroids();
			
			int i = 0;		
			while (isRunning)
			{
				AssignPoints();
				RecalcCentroids();
				CheckStoppingCondition();			
				
				if(i % 10 == 0)
				{
					//dataSet.setIsPlotting(true);
					userGUI.currentSolution(dataSet);
					
					System.out.println(i + "th iteration...");
					for (int x = 0; x < clusters.size(); x++)
					{
						System.out.println("Cluster " + clusters.get(x).GetClusterID());
						System.out.println(clusters.get(x).ClusterStats());
					}
				}
				
				this.ResetPoints();
					
				while (dataSet.getIsPlotting()){Thread.sleep(500);}	
				i++;			
			}
			
			System.out.println("Results...");
			for (int x = 0; x < clusters.size(); x++)
			{
				System.out.println("Cluster " + clusters.get(x).GetClusterID());
				System.out.println(clusters.get(x).ClusterStats());
			}
		}
	}

	@Override
	public void set(DataSet set, int numClusters, UserGUI ugui) {
		
		if(set.getDataSetSize() < numClusters)
		{
			gui.MessageBox.show("You have entered more clusters than available points.!!!!!","ERROR");
			dataSet = null;
			return;
		}
		dataSet = set;
		this.numClusters = numClusters;
		userGUI = ugui;
		pickInitCentroids();
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public DataSet currentSolution() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void CheckStoppingCondition() {
		boolean isSame = true;
		String[] attr = clusters.get(0).GetCentroid().GetAttributeNames();
		
		if (crtCentroids.size() == prvCentroids.size()) 
		{
			for (int i = 0; i < crtCentroids.size(); i++) 
			{
				for (int j = 0; j < attr.length; j++) 
				{
					if (Double.compare(crtCentroids.get(i).getAttribute(attr[j]), prvCentroids.get(i).getAttribute(attr[j])) != 0) 
					{
						isSame = false;
					}
				}
			} 
		}
		else
		{
			isSame = false;
		}
		
		// if not the same keep running.
		isRunning = !isSame;
	}
	
	private void AssignPoints()
	{
		for(int i = 0; i < dataSet.getDataSetSize(); i++)
		{
			DataPoint p = dataSet.getPoint(i);
			int cluster = 0;
			double distance = this.clusters.get(0).GetDistance(p);
			for(int j = 1; j < this.clusters.size(); j++)
			{
				double dis =  this.clusters.get(j).GetDistance(p);
				
				if (dis < distance)
				{
					distance = dis;
					cluster = j;
				}
			}
			
			p.setClusterNumber(this.clusters.get(cluster).GetClusterID());
			this.clusters.get(cluster).AddDataPoint(p);
		}
	}
	
	private void RecalcCentroids()
	{
		this.prvCentroids = this.crtCentroids;
		this.crtCentroids = new ArrayList<DataPoint>();
		
		for (int i = 0; i < this.clusters.size(); i++)
		{
			clusters.get(i).RecalculateCentroid();
			this.crtCentroids.add(clusters.get(i).GetCentroid());
		}
	}
	
	private void pickInitCentroids()
	{
		clusters = new ArrayList<Cluster>(this.numClusters);
		crtCentroids = new ArrayList<DataPoint>();
		
		for (int i = 0; i < numClusters; i++)
		{
			boolean validIndex = false;
			clusters.add(new Cluster(i));
			
			while(!validIndex)
			{
				double temp = Math.random() * dataSet.getDataSetSize() - 1;
				int index = (int)Math.floor(temp);
				DataPoint tempPoint;
				tempPoint = dataSet.getPoint(index);
				
				if(!clusters.contains(tempPoint))
				{
					validIndex = true;
					tempPoint.setCentroid(true);
					crtCentroids.add(tempPoint);
					clusters.get(i).SetCentroid(tempPoint);
				}
			}
		}
	}
	
	private void ResetPoints()
	{
		// only want to clear if still running. Otherwise it would clear the final result.
		if (isRunning) 
		{
			for (int i = 0; i < this.clusters.size(); i++) {
				this.clusters.get(i).ClearDataPoints();
			} 
		}
	}
	
	public static void main(String[] args) throws BiffException, IOException
	{
		DataSet winning = DataSet.CreateFromExcel(System.getProperty("user.dir")+ "\\data\\Iris Data Set.xls");
		Cluster.SetAttributeNames(winning.getAttributes());
		int centers = 3;	
		I_Algorithm k = new K_Means();
		
		k.set(winning, centers, new UserGUI());
		try
		{
			k.start();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
