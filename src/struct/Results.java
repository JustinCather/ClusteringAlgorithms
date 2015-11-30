package struct;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import algorithms.Algorithm;
import jxl.write.DateTime;

public class Results implements Serializable {
	public Cluster[] clusters;
	public String output;
	public Algorithm alg;
	public String dataFileName;
	public DataModel dataModel;
	public Double stoppingDistance;
	public Double fuzzyFactor;
	public int desiredClusters;
	public transient String path;
	public Results()
	{
		path="";
	}
	
	public void Serialize()
	{
		try
		{
			dataFileName=dataFileName.replace('\\','_');
			String fileName = alg.toString()+dataFileName+"_"+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())+".ser";
			File f = new File(fileName);
			path=f.getAbsolutePath();
			FileOutputStream fileOut =
	        new FileOutputStream(f);
	        ObjectOutputStream out = new ObjectOutputStream(fileOut);
	        out.writeObject(this);
	        out.close();
	        
	        fileOut.close();
	        
		}
		catch(Exception ex)
		{
			
		}
	}
	
	public static Results Deserialize(String fileName)
	{
		Results e = null;
	      try
	      {
	         FileInputStream fileIn = new FileInputStream(fileName);
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         e = (Results) in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException i)
	      {
	         i.printStackTrace();
	         return null;
	      }catch(ClassNotFoundException c)
	      {
	         System.out.println("Employee class not found");
	         c.printStackTrace();
	         return null;
	      }
	      return e;
	}

}
