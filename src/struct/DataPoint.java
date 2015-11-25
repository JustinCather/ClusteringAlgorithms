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
	
	public boolean isCentroid() {
		return isCentroid;
	}
	public void setCentroid(boolean isCentroid) {
		this.isCentroid = isCentroid;
	}
	public boolean isVisited() {
		return isVisited;
	}
	public void setVisited(boolean isVisited) {
		this.isVisited = isVisited;
	}
	public boolean isReal() {
		return isReal;
	}
	public void setReal(boolean isReal) {
		this.isReal = isReal;
	}
	public int getClusterNumber() {
		return clusterNumber;
	}
	public void setClusterNumber(int clusterNumber) {
		this.clusterNumber = clusterNumber;
	}

	public void addAttribute(String name, double attr)
	{
		point.put(name,attr);
	}
	
	public void changeAttributeValue(String name, double newVal)
	{
		this.point.put(name, newVal);
	}
	
	public void setType(String s)
	{
		type=s;
	}
	
	public String getType()
	{		
		return type;
	}
	
	public double getAttribute(String name)
	{
		return point.get(name);
	}
	
	public int GetNumberOfAttributes()
	{
		return this.point.size();
	}
	
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
		    System.out.println( "\n\t"+key+":"+this.point.get(key) );
		}
		return s;
	}
	public double GetDistance(DataPoint p)
	{
		double summation = 0;
		
		for(int i = 0; i < this.GetNumberOfAttributes(); i++)
		{
			double centroidVal = this.getAttribute(this.GetAttributeNames()[i]);
			double pointTwoVal = p.getAttribute(this.GetAttributeNames()[i]);
			summation += Math.pow((centroidVal-pointTwoVal),2);
		}
		
		summation = Math.sqrt(summation);
		return summation;
	}
}
