package struct;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import jxl.read.biff.BiffException;
import struct.DataModel.SplitMethod;

public class Cluster 
{
	private static ArrayList<String> ATTRIBUTE_NAMES;
	private LinkedList<DataPoint> dataPoints;
	private DataPoint centroid;
	private int clusterID;
	private String clusterType;
	
	public Cluster(int clusterID)
	{
		this.clusterID = clusterID;	
		this.dataPoints = new LinkedList<DataPoint>();
		this.centroid = null;
	}
	
	/** Gets the class type of the class with the most instances in this cluster.
	 * @return The cluster type.
	 */
	public String GetClusterType() 
	{
		return clusterType;
	}

	/**
	 * Counts the number of instances of each class in this cluster
	 * and sets the cluster type to the class that has the most instances.
	 */
	public void SetClusterType() 
	{
		int highNumber;
		String tempType;
		HashMap<String, Integer> counts = new HashMap<String, Integer>();	
		
		for (int i = 0; i < dataPoints.size(); i++)
		{
			try 
			{
				String type = dataPoints.get(i).getType();
				counts.put(type, counts.get(type) + 1);
			} 
			catch (Exception e) {
				String type = dataPoints.get(i).getType();
				counts.put(type, 1);
			}
		}
		
		tempType = dataPoints.get(0).getType();
		highNumber = counts.get(tempType);
		
		for (Entry<String, Integer> entry : counts.entrySet())
		{
			if (entry.getValue() > highNumber)
			{
				highNumber = entry.getValue();
				tempType = entry.getKey();
			}
		}
		
		this.clusterType = tempType;
	}

	/** This needs to be set to same values from the DataSet
	 * @param names The attribute names to use. 
	 */
	public static void SetAttributeNames(ArrayList<String> names)
	{
		Cluster.ATTRIBUTE_NAMES = names;
	}
	
	public static ArrayList<String> GetAtributeNames()
	{
		return Cluster.ATTRIBUTE_NAMES;
	}
	
	/** Gets the data points currently associated with this cluster.
	 * @return A linked list of data points within the cluster.
	 */
	public LinkedList<DataPoint> GetDataPoints()
	{
		return this.dataPoints;
	}
	
	public DataPoint GetDataPoint(int index)
	{
		if(dataPoints==null)
			return null;
		if(index>this.dataPoints.size())
			return null;
		return dataPoints.get(index);
	}
	/** Sets the data points associated with this cluster.
	 * @param points A linked list of data points within this cluster.
	 */
	public void SetDataPoints(LinkedList<DataPoint> points)
	{
		this.dataPoints = points;
	}
	
	/** Add a data point to this cluster.
	 * @param p The data point to add.
	 */
	public void AddDataPoint(DataPoint p)
	{
		p.assigned=true;
		p.setClusterNumber(this.GetClusterID());
		this.dataPoints.add(p);
	}
	
	public void AddDataPoints(LinkedList<DataPoint> points)
	{
		this.dataPoints.addAll(points);
	}
	
	/** Check to see if this cluster has any data points in it.
	 * @return True if there is at least one data point.
	 */
	public boolean HasDataPoints()
	{
		return this.dataPoints.size() > 0;
	}
	
	/**
	 * Removes all data points within this cluster.
	 */
	public void ClearDataPoints()
	{
		this.dataPoints.clear();
	}
	
	/** Gets the current centroid for this cluster.
	 * @return The current centroid.
	 */
	public DataPoint GetCentroid()
	{
		return this.centroid;
	}
	
	/** Set the centroid for this cluster.
	 * @param c The centroid for this cluster to use.
	 */
	public void SetCentroid(DataPoint c)
	{
		this.centroid = c;
		if(this.dataPoints.size()==0)
			this.dataPoints.add(c);
	}
	
	/**
	 * Given the data points within this cluster, a new centroid
	 * will be calculated for the cluster.
	 */
	public void RecalculateCentroid()
	{
		DataPoint newCentroid = new DataPoint();
		
		// Need at least two points to find a new centroid.
		if (this.GetDataPoints().size() > 1)
		{
			double[] sums = new double[Cluster.ATTRIBUTE_NAMES.size()];
			// Zero out the sum array.
			for (int i = 0; i < sums.length; i++)
			{
				sums[i] = 0;
			}
			
			// Go through all data points
			for (int i = 0; i < dataPoints.size(); i++)
			{
				// sum up each attribute of each data point.
				for (int j = 0; j < sums.length; j++)
				{
					dataPoints.get(i).assigned=false;
					sums[j] += dataPoints.get(i).getAttribute(Cluster.ATTRIBUTE_NAMES.get(j));
				}
			}
			
			// Calculate new centroid from sums of the data point attributes.
			for (int i = 0; i < sums.length; i++)
			{
				newCentroid.addAttribute(Cluster.ATTRIBUTE_NAMES.get(i), sums[i] / dataPoints.size());
			}
			
			// Set the newly calculated centroid.
			this.SetCentroid(newCentroid);
		}
		// If there is only one data point...well that is the center than.
		else if (this.GetDataPoints().size() == 1)
		{
			this.SetCentroid(this.GetDataPoints().getFirst());
		}	
		// No data points, no centroid.
		else
		{
			this.SetCentroid(null);
		}
	}
	
	/** Get the distance a data point is from the centroid of this cluster.
	 * @param p The data point to measure the distance from.
	 * @return The distance between data point p and the centroid of this cluster.
	 */
	public double GetDistance(DataPoint p)
	{
		double summation = 0;
		
		for(int i = 0; i < Cluster.ATTRIBUTE_NAMES.size(); i++)
		{
			double centroidVal = this.centroid.getAttribute(Cluster.ATTRIBUTE_NAMES.get(i));
			double pointTwoVal = p.getAttribute(Cluster.ATTRIBUTE_NAMES.get(i));
			summation += Math.pow((centroidVal-pointTwoVal),2);
		}
		
		summation = Math.sqrt(summation);
		return summation;
	}
	
	/** Gets the ID for this cluster.
	 * @return An integer value that is the ID for this cluster.
	 */
	public int GetClusterID()
	{
		return this.clusterID;
	}
	
	public LinkedList<DataPoint> RemoveUnAssigned()
	{
		LinkedList<DataPoint> removed = new LinkedList<DataPoint>();
		
		for(int i = 0; i< dataPoints.size();i++)
		{
			if(!dataPoints.get(i).assigned)
				removed.add(dataPoints.get(i));
		}
		
		dataPoints.removeAll(removed);
		
		return removed;
	}
	/** Gets the current statistics for this cluster.
	 * @return A formatted string consisting of total data points,
	 * and a count for each class present in the cluster and its percent.
	 */
	public String ClusterStats()
	{
		int ptsNum = dataPoints.size();
		String results = "Total Points = " + ptsNum + "\r\n";
		HashMap<String, Integer> counts = new HashMap<String, Integer>();	
		
		for (int i = 0; i < dataPoints.size(); i++)
		{
			try {
				String type = dataPoints.get(i).getType();
				counts.put(type, counts.get(type) + 1);
			} catch (Exception e) {
				String type = dataPoints.get(i).getType();
				counts.put(type, 1);
			}
		}
		
		for (Entry<String, Integer> entry : counts.entrySet())
		{
			double percent = ((double) entry.getValue()) / ptsNum * 100;
			results += "\t" + entry.getValue() + " " + entry.getKey() + " " + percent + "% \r\n";
		}
		
		return results;
	}
	
	public static void main(String[] args) throws BiffException, IOException
	{
		String path = System.getProperty("user.dir")+ "\\data\\Iris Data Set.xls";
		DataModel winning = new DataModel(path);
		winning.GetAttributesFromExcel();
		winning.GetDataFromExcel(SplitMethod.ClassPercent, 75);
		Cluster.SetAttributeNames(winning.GetAttributes());
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		int clusterCount = 0;
		Cluster temp = new Cluster(clusterCount);
		
		for (int i = 0; i < winning.GetTrainingSet().GetDataSetSize(); i++)
		{
			if (i != 0 && i % 50 == 0)
			{
				clusterCount++;
				clusters.add(temp);
				temp = new Cluster(clusterCount);
			}
			
			temp.AddDataPoint(winning.GetTrainingSet().GetPoint(i));
		}
		
		if (!clusters.contains(temp))
			clusters.add(temp);
		
		for (int i = 0; i < clusters.size(); i++)
		{
			System.out.println("Cluster " + clusters.get(i).clusterID + ":");
			clusters.get(i).RecalculateCentroid();
			clusters.get(i).GetCentroid().toString();
		}
	}
}
