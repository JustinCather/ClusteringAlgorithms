package algorithms;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import gui.UserGUI;
import struct.DataPoint;
import struct.DataSet;

public class K_Means implements I_Algorithm {
	
	private DataSet dataSet;
	private boolean isRunning;
	private int clusters;
	private UserGUI userGUI;
	private ArrayList<DataPoint> previousCentroids;
	private ArrayList<DataPoint> currentCentroids;
	
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
			
			int i = 0;
			
			while (i < 10)
			{
				AssignPoints();
				RecalcCentroids();
				if(i%10==0)
				{
					//dataSet.setIsPlotting(true);
					userGUI.currentSolution(dataSet);
				}
					
				while (dataSet.getIsPlotting()){Thread.sleep(500);}	
				i++;
				
			}
			
			for(int j =0;j<this.currentCentroids.size();j++)
			{
				System.out.println("Centriod " + (j+1)+":");//+this.currentCentroids.get(j));
				System.out.print(this.currentCentroids.get(j));
				int pointCntr=1;
				for(int k =0;k<dataSet.getDataSetSize();k++)
				{
					if(dataSet.getPoint(k).getClusterNumber()==j)
					{
						System.out.println("Point " + pointCntr+":");
						System.out.println(dataSet.getPoint(k));
						pointCntr++;
					}
				}
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
		this.clusters = numClusters;
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
		// TODO Auto-generated method stub

	}
	
	private void AssignPoints()
	{
		for(int i =0; i< dataSet.getDataSetSize();i++)
		{
			DataPoint p = dataSet.getPoint(i);
			int center = 0;
			Double distance = getDistance(this.currentCentroids.get(0), p);
			for(int j=1;j<this.currentCentroids.size();j++)
			{
				double dis = getDistance(this.currentCentroids.get(j),p);
				if(p == this.currentCentroids.get(j))
				{
					p.setClusterNumber(center);
					continue;
				}
				if(dis<distance)
				{
					distance = dis;
					center = j;
				}
			}
			
			p.setClusterNumber(center);
		}
	}
	
	private void RecalcCentroids()
	{
		this.previousCentroids = this.currentCentroids;
		this.currentCentroids = new ArrayList<DataPoint>();
		ArrayList<DataPoint>[] sorted = new ArrayList[this.clusters];
		for(int i=0;i<this.clusters;i++)
			sorted[i]=new ArrayList<DataPoint>();
		for(int i=0;i<dataSet.getDataSetSize();i++)
		{
			sorted[dataSet.getPoint(i).getClusterNumber()].add(dataSet.getPoint(i));
		}
		
		for(int i=0;i<this.clusters;i++)
		{
			DataPoint p = new DataPoint();
			HashMap<String,Double> sums = new HashMap<String,Double>();
			for(int j=0;j<sorted[i].size();j++)
			{
				for(int k =0; k< this.dataSet.getAttributes().size();k++)
				{
					if(!sums.containsKey(this.dataSet.getAttributes().get(k)))
						sums.put(this.dataSet.getAttributes().get(k), 0.0);
					sums.put(this.dataSet.getAttributes().get(k), sums.get(this.dataSet.getAttributes().get(k))+sorted[i].get(j).getAttribute(this.dataSet.getAttributes().get(k)));
							
				}
			}
			
			for(int j = 0; j<sums.size(); j++)
			{
				p.addAttribute(this.dataSet.getAttributes().get(j), sums.get(this.dataSet.getAttributes().get(j))/sorted[i].size());
			}
			this.currentCentroids.add(p);
			
		}
	}
	
	private double getDistance(DataPoint one, DataPoint two)
	{
		double summation =0;
		for(int i=0;i<dataSet.getAttributes().size();i++)
		{
			double pointOneVal = one.getAttribute(dataSet.getAttributes().get(i));
			double pointTwoVal = two.getAttribute(dataSet.getAttributes().get(i));
			summation += Math.pow((pointOneVal-pointTwoVal),2);
		}
		summation = Math.sqrt(summation);
		return summation;
	}
	private void pickInitCentroids()
	{
		currentCentroids = new ArrayList<DataPoint>();
		
		for (int i = 0; i < clusters; i++)
		{
			boolean validIndex = false;
			
			while(!validIndex)
			{
				double temp = Math.random() * dataSet.getDataSetSize() - 1;
				int index = (int)Math.floor(temp);
				DataPoint tempPoint;
				tempPoint =dataSet.getPoint(index);
				
				if(!currentCentroids.contains(tempPoint))
				{
					currentCentroids.add(tempPoint);
					validIndex = true;
					tempPoint.setCentroid(true);
				}
			}
		}
	}

}
