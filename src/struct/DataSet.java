package struct;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import algorithms.I_Algorithm;
import algorithms.K_Means;
import gui.UserGUI;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class DataSet 
{
	private LinkedList<DataPoint> points;
	private boolean isPlotting;
	private ArrayList<String> attributes;
	
	public DataSet()
	{
		attributes =  new ArrayList<String>();		
		points = new LinkedList<DataPoint>();
		isPlotting = false;
	}
	
	public boolean getIsPlotting()
	{
		return isPlotting;
	}
	
	public void setIsPlotting(boolean value)
	{
		isPlotting = value;
	}
	
	public void AddPoint(DataPoint p)
	{
		points.add(p);
	}
	
	public ArrayList<String> getAttributes()
	{
		return attributes;
	}
	
	public DataPoint getPoint(int index)
	{
		if (index > points.size() || index < 0)
			return null;
		else
			return points.get(index);
	}
	
	public double findMin(String attribute)
	{
		double temp = Double.MAX_VALUE;
		
		for(int i =0; i<points.size();i++)
		{
			if(points.get(i).getAttribute(attribute)<temp)
			{
				temp = points.get(i).getAttribute(attribute);
			}
		}
		return temp;
	}
	
	public double findMax(String attribute)
	{
		double temp = Double.MIN_VALUE;
		
		for(int i =0; i<points.size();i++)
		{
			if(points.get(i).getAttribute(attribute) >temp)
			{
				temp = points.get(i).getAttribute(attribute);
			}
		}
		return temp;
	}
	
	public int getDataSetSize(){
		return points.size();
	}
	
	private static DataSet CreateFromExcel(String path) throws BiffException, IOException
	{
		DataSet temp = new DataSet();
		Workbook workbook = Workbook.getWorkbook(new File(path));
		Sheet sheet = workbook.getSheet(0);
		int row = sheet.getRows();
		int col = sheet.getColumns();
		
		for (int i = 0; i < col - 1; i++)
		{
			temp.attributes.add(sheet.getCell(i, 0).getContents());
		}
		
		for(int r = 1; r < row;r++)
		{
			DataPoint p = new DataPoint();
			for(int c =0;c<col;c++)
			{
				if(c!=col-1)
					p.addAttribute(temp.attributes.get(c),Double.parseDouble(sheet.getCell(c,r).getContents()));
				else
					p.setType(sheet.getCell(c,r).getContents());
			}
			temp.AddPoint(p);
		}
		return temp;
	}
	
	public static void main(String[] args) throws BiffException, IOException
	{
		DataSet winning = CreateFromExcel("E:\\temp\\ClusteringAlgorithms\\ClusteringAlgorithms\\data\\Iris Data Set.xls");
		
		int centers = 3;
		
		I_Algorithm k = new K_Means();
		k.set(winning, 3,new UserGUI());
		try
		{
			k.start();
		}
		catch(Exception e)
		{
			
		}
	}

}
