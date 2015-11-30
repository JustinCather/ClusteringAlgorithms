package struct;

import java.util.Comparator;

/** Compares two data points based on attr value.
 * @author Justin
 *
 */
public class DataPointComparator implements Comparator<DataPoint> {

	private String attr;
	
	public DataPointComparator(String attr) 
	{
		this.attr = attr;
	}
	
	/** Gets the attribute to compare with.
	 * @return The current attribute to compare with.
	 */
	public String getAttr() 
	{
		return attr;
	}

	/** Sets the attribute to compare with.
	 * @param attr The attribute to compare with.
	 */
	public void setAttr(String attr) 
	{
		this.attr = attr;
	}
	
	@Override
	public int compare(DataPoint arg0, DataPoint arg1) 
	{
		return Double.compare(arg0.GetAttribute(this.attr), arg1.GetAttribute(this.attr));
	}

}
