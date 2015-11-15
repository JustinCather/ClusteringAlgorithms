package struct;

import java.util.LinkedList;

public class DataSet 
{	
	private LinkedList<DataPoint> points;
	private boolean isPlotting;
	
	public DataSet()
	{
		points = new LinkedList<DataPoint>();
		isPlotting = false;
	}
	
	/**
	 * Resets the IsVisited boolean to false for all data points in the data set.
	 */
	public void ResetVisited()
	{
		for (int i = 0; i < points.size(); i++)
		{
			points.get(i).setVisited(false);
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
				if(points.get(i).getAttribute(attribute)<temp)
				{
					temp = points.get(i).getAttribute(attribute);
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
				if(points.get(i).getAttribute(attribute) >temp)
				{
					temp = points.get(i).getAttribute(attribute);
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
	public int GetDataSetSize(){
		return points.size();
	}
}
