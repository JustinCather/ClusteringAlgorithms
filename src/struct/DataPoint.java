package struct;

import java.util.HashMap;

public class DataPoint {
	
	private HashMap<String, Double> point;
	private String type;
	private boolean isCentroid;
	private boolean isVisited;
	private boolean isReal;
	private int clusterNumber;
	
	public DataPoint()
	{
		point = new HashMap<String,Double>();
		type = "";
		isCentroid=false;
		isVisited=false;
		isReal=true;
		clusterNumber=-1;
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
	
	public String toString()
	{
		String s ="";
		for ( String key : this.point.keySet() ) {
		    System.out.println( "\n\t"+key+":"+this.point.get(key) );
		}
		return s;
	}
}
