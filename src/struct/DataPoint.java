package struct;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

public class DataPoint implements Serializable{
	
	private HashMap<String, Double> point;
	private String type;
	private boolean isCentroid;
	private boolean isVisited;
	private boolean isReal;
	private int clusterNumber;
	public boolean assigned;
	public DataPoint()
	{
		point = new HashMap<String,Double>();
		type = "";
		isCentroid=false;
		isVisited=false;
		isReal=true;
		clusterNumber=-1;
		assigned = false;
	}
	
	/** Checks if this datapoint is a centroid.
	 * @return True is centroid, false if not.
	 */
	public boolean IsCentroid() 
	{
		return isCentroid;
	}
	
	/** Sets if this datapoint is a centroid.
	 * @param isCentroid True if this point is a centroid, false if not.
	 */
	public void SetCentroid(boolean isCentroid) 
	{
		this.isCentroid = isCentroid;
	}
	
	/** Gets if this point has been visited.
	 * @return True if visited, false if not.
	 */
	public boolean IsVisited() 
	{
		return isVisited;
	}
	
	/** Sets if this point has been visited.
	 * @param isVisited True if visited, false if not.
	 */
	public void SetVisited(boolean isVisited) 
	{
		this.isVisited = isVisited;
	}
	
	/** Gets if this point is real.
	 * @return True if point is real, false if not.
	 */
	public boolean IsReal() 
	{
		return isReal;
	}
	
	/** Sets if this point is real.
	 * @param isReal True if real, false if not.
	 */
	public void SetReal(boolean isReal) 
	{
		this.isReal = isReal;
	}
	
	/** Gets the cluster number that this point is in.
	 * @return The ID of the cluster that contains this point.
	 */
	public int GetClusterNumber() 
	{
		return clusterNumber;
	}
	
	/** Sets the cluster ID for this point.
	 * @param clusterNumber The ID of the cluster that contains this point.
	 */
	public void SetClusterNumber(int clusterNumber) 
	{
		this.clusterNumber = clusterNumber;
	}

	/** Add a data attribute to the point.
	 * @param name The name of the attribute.
	 * @param attr The value of the attribute.
	 */
	public void AddAttribute(String name, double attr)
	{
		point.put(name,attr);
	}
	
	/** Changes an existing attribute value.
	 * @param name The name of the attribute to change.
	 * @param newVal The new value for the attribute.
	 */
	public void ChangeAttributeValue(String name, double newVal)
	{
		this.point.put(name, newVal);
	}
	
	/** Sets the class of this point.
	 * @param s The class type of this point.
	 */
	public void SetType(String s)
	{
		type = s;
	}
	
	/** Gets the class of this point.
	 * @return The class type of this point.
	 */
	public String GetType()
	{		
		return type;
	}
	
	/** Gets the corresponding value for the attribute name.
	 * @param name The name of the attribute.
	 * @return The attributes value.
	 */
	public double GetAttribute(String name)
	{
		return point.get(name);
	}
	
	/** Gets the total number of attributes for this point.
	 * @return The number of attributes for this point.
	 */
	public int GetNumberOfAttributes()
	{
		return this.point.size();
	}
	
	/** Gets the names of the attributes for this point.
	 * @return An array of attribute names.
	 */
	public String[] GetAttributeNames()
	{
		String[] names =  new String[this.point.size()];
		int count = 0;
		
		for (Entry<String, Double> entry : this.point.entrySet())
		{
			names[count] = entry.getKey();
			count++;
		}
		
		return names;
	}
	
	public String toString()
	{
		String s ="";
		for ( String key : this.point.keySet() ) {
		     s+= "\n\t" + key + ":" + this.point.get(key); 
		}
		return s;
	}
	
	/** Gets the distance from this point to another point.
	 * @param p The point to measure the distance to.
	 * @return The distance between this point and point p.
	 */
	public double GetDistance(DataPoint p)
	{
		double summation = 0;
		
		for(int i = 0; i < this.GetNumberOfAttributes(); i++)
		{
			double centroidVal = this.GetAttribute(this.GetAttributeNames()[i]);
			double pointTwoVal = p.GetAttribute(this.GetAttributeNames()[i]);
			summation += Math.pow((centroidVal-pointTwoVal),2);
		}
		
		summation = Math.sqrt(summation);
		return summation;
	}
}
