package struct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Represents a data set of data points.
 *
 */
public class DataSet implements Serializable
{	
	private ArrayList<String> attributes;
	private LinkedList<DataPoint> points;
	private boolean isPlotting;
	
	public DataSet()
	{
		points = new LinkedList<DataPoint>();
		attributes = new ArrayList<String>();
		isPlotting = false;
	}
	
	/** Sets the attributes that exist for this data set.
	 * @param a The list of attributes to use for this data set.
	 */
	public void SetAttributes(ArrayList<String> a)
	{
		this.attributes = a;
	}
	
	/** Gets the attributes that exist for this data set.
	 * @return The list of attribute that are used by this data set.
	 */
	public ArrayList<String> GetAttributes()
	{
		return this.attributes;
	}
	
	/**
	 * Resets the IsVisited boolean to false for all data points in the data set.
	 */
	public void ResetVisited()
	{
		for (int i = 0; i < points.size(); i++)
		{
			points.get(i).SetVisited(false);
		}
	}
	
	/** Gets the isPlotting value.
	 * @return True if plotting, false if not.
	 */
	public boolean GetIsPlotting()
	{
		return isPlotting;
	}
	
	/** Set the isPlotting value.
	 * @param value True if plotting, false if not.
	 */
	public void SetIsPlotting(boolean value)
	{
		isPlotting = value;
	}
	
	/** Add a new data point to this data set.
	 * @param p The point to add.
	 */
	public void AddPoint(DataPoint p)
	{
		points.add(p);
	}
	
	
	/** Removes the data point from the set.
	 * @param index The index of the point to remove.
	 */
	public void RemoveDataPoint(int index)
	{
		points.remove(index);
	}
	
	/** Gets a data point from the data set.
	 * @param Index The index of the desired data point.
	 * @return The datapoint if it exists, null if it does not.
	 */
	public DataPoint GetPoint(int index)
	{
		if (index > points.size() || index < 0)
			return null;
		else
			return points.get(index);
	}
	
	/** Finds the minimum value for a particular attribute in the data set.
	 * @param attribute The attribute to perform the search on.
	 * @return The minimum value for the input attribute, -1 if failed.
	 */
	public double FindMin(String attribute)
	{
		double temp = Double.MAX_VALUE;
		
		try 
		{
			for(int i = 0; i < points.size(); i++)
			{
				if(points.get(i).GetAttribute(attribute)<temp)
				{
					temp = points.get(i).GetAttribute(attribute);
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			temp = -1.0;
		}
		
		return temp;
	}
	
	/** Finds the maximum value for a particular attribute in the data set.
	 * @param attribute The attribute to perform the search on.
	 * @return The maximum value for the input attribute, -1 if failed.
	 */
	public double FindMax(String attribute)
	{
		double temp = Double.MIN_VALUE;
		
		try {
			for(int i = 0; i < points.size(); i++)
			{
				if(points.get(i).GetAttribute(attribute) >temp)
				{
					temp = points.get(i).GetAttribute(attribute);
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			temp = -1.0;
		}
		
		return temp;
	}
	
	/** Gets the number of data points in the data set.
	 * @return Integer equal to the number of points in the set.
	 */
	public int GetDataSetSize()
	{
		return points.size();
	}
	
	/** Gets the linked list of DataPoint objects.
	 * @return The linked list of datapoints.
	 */
	public LinkedList<DataPoint> GetDataPoints()
	{
		return this.points;
	}
	
	/** Gets the centroid of the data set.
	 * @param attributes The attributes have the data set.
	 * @return The data point that represents the centroid of the data set.
	 */
	public DataPoint GetCenter(String[] attributes)
	{
		DataPoint newCentroid = new DataPoint();
		
		// Need at least two points to find a new centroid.
		if (this.GetDataPoints().size() > 1)
		{
			double[] sums = new double[attributes.length];
			// Zero out the sum array.
			for (int i = 0; i < sums.length; i++)
			{
				sums[i] = 0;
			}
			
			// Go through all data points
			for (int i = 0; i < points.size(); i++)
			{
				// sum up each attribute of each data point.
				for (int j = 0; j < sums.length; j++)
				{
					points.get(i).assigned=false;
					sums[j] += points.get(i).GetAttribute(attributes[j]);
				}
			}
			
			// Calculate new centroid from sums of the data point attributes.
			for (int i = 0; i < sums.length; i++)
			{
				newCentroid.AddAttribute(attributes[i], sums[i] / points.size());
			}
			return newCentroid;
		}
		return null;

	}
}
