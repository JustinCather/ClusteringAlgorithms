package utilities;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import jxl.read.biff.BiffException;
import struct.Cluster;
import struct.DataModel;
import struct.DataPointComparator;
import struct.DataModel.SplitMethod;
import struct.DataPoint;
import struct.DataSet;

/** Static class for performing preprocessing tasks on a data set.
 * @author Justin
 *
 */
public class Preprocessing 
{
	private Preprocessing(){}
	
	public enum SmoothMethod{Means, Median, Boundaries}
	
	/**
	 * Normalizes all attributes in the training set.
	 */
	public static void NormalizeDataSet(DataSet ds)
	{
		for (String a : ds.GetAttributes())
		{
			double min = ds.FindMin(a);
			double max = ds.FindMax(a);
			
			for (int i = 0; i < ds.GetDataSetSize(); i++)
			{
				double val = ds.GetPoint(i).GetAttribute(a);
				val = Preprocessing.NormailizeDataPoint(val, min, max);
				ds.GetPoint(i).ChangeAttributeValue(a, val);
			}
		}
	}
	
	/** Smoothes the values of the dataset. Helps reduce noise.
	 * @param ds The dataset to smooth.
	 * @param bucketSize The number of values to smooth at once.
	 * @param method The method used for smoothing.
	 * @Means All values in bucket set to the average of the bucket.
	 * @Median All values in the bucket set to the median value of the bucket.
	 * @Boundaries All values up to the mid-point of the bucket set to the first value of the bucket.
	 * All values from the mid-point to the end of the bucket set to the end value of the bucket.
	 */
	public static void SmoothDataSet(DataSet ds, int bucketSize, SmoothMethod method)
	{
		int size = ds.GetDataSetSize();
		DataPointComparator comparator = new DataPointComparator(null);
		
		for (String a : ds.GetAttributes())
		{				
			// sort linked list based on current attribute.
			comparator.setAttr(a);
			ds.GetDataPoints().sort(comparator);

			// put in buckets and smooth.
			for (int i = 0; i < size; i += bucketSize)
			{
				double[] bucket = new double[bucketSize];
				
				// Fill up the bucket.
				for (int j = 0; j < bucketSize; j++)
				{
					if (i + j < size)
						bucket[j] = ds.GetPoint(i + j).GetAttribute(a);
				}
				
				// Perform smoothing operation.
				switch(method)
				{
					case Boundaries:
						Preprocessing.SmoothBoundaries(bucket);
						break;
						
					case Means:
						Preprocessing.SmoothMeans(bucket);
						break;
						
					case Median:
						Preprocessing.SmoothMedian(bucket);
						break;
				}
				
				// Put the new values from bucket back into data set.
				for (int j = 0; j < bucketSize; j++)
				{
					if (i + j < size)
						ds.GetPoint(i + j).ChangeAttributeValue(a, bucket[j]);
				}
			}		
		}			
	}
	
	private static void SmoothBoundaries(double[] arr)
	{
		int mid = arr.length / 2;
		
		for (int i = 0; i < mid; i++)
		{
			arr[i] = arr[0];
		}
		
		for (int i = mid; i < arr.length; i++)
		{
			arr[i] = arr[arr.length - 1];
		}
	}
	
	private static void SmoothMeans(double[] arr)
	{
		double sum = 0.0;
		double avg = 0.0;
		
		for (int i = 0; i < arr.length; i++)
		{
			sum += arr[i];
		}
		
		avg = sum / arr.length;
		
		for (int i = 0; i < arr.length; i++)
		{
			arr[i] = avg;
		}
	}
	
	private static void SmoothMedian(double[] arr)
	{
		int mid = arr.length / 2;
		double median = arr[mid];
		
		for (int i = 0; i < arr.length; i++)
		{
			arr[i] = median;
		}
	}
	
	private static double NormailizeDataPoint(double value, double min, double max)
	{
		return (value - min) / (max - min);
	}
	
	public static void main(String[] args) throws BiffException, IOException{
		String path = System.getProperty("user.dir")+ "\\data\\3_Iris Data Set.xls";
		DataModel test = new DataModel(path);
		test.GetAttributesFromExcel();
		test.GetDataFromExcel(SplitMethod.ClassPercent, 75);
			
		System.out.println("Before");	
		for (int i = 0; i < test.GetTrainingSet().GetDataSetSize(); i++)
		{
			System.out.println("Point " + i);
			for (String s : test.GetAllAttributes())
			{
				System.out.println(test.GetTrainingSet().GetPoint(i).GetAttribute(s));
			}
			System.out.println();
			System.out.println();
		}
			
		Preprocessing.NormalizeDataSet(test.GetTrainingSet());
		//Preprocessing.SmoothDataSet(test.GetTrainingSet(), 15, SmoothMethod.Median);
		Preprocessing.SmoothDataSet(test.GetTrainingSet(), 5, SmoothMethod.Median);
		
		System.out.println("After");
		for (int i = 0; i < test.GetTrainingSet().GetDataSetSize(); i++)
		{
			System.out.println("Point " + i);
			for (String s : test.GetAllAttributes())
			{		
				System.out.println(test.GetTrainingSet().GetPoint(i).GetAttribute(s));
			}
			System.out.println();
			System.out.println();
		}
	}
	
}
