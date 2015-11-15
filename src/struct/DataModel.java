package struct;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * @author Justin
 *
 */
public class DataModel 
{
	class IntPair
	{
		// Use IntPair in the DistributeDataPoints() method.
		// First acts as a counter, and second acts as max value.
		public int first;
		public int second;	
	}
	
	public enum SplitMethod{ClassPercent, DataPercent, RandomPercent, EveryOther };
	
	private DataSet trainingSet;
	private DataSet testingSet;
	private ArrayList<String> attributes;
	private SplitMethod splitMethod;
	private double percent;
	private String excelPath;
	
	public DataModel(String excelPath)
	{
		this.excelPath = excelPath;
		this.trainingSet = null;
		this.testingSet = null;
		this.attributes = new ArrayList<String>();
		this.splitMethod = SplitMethod.DataPercent;
		this.percent = .5;
	}
	
	/** Gets the training dataset
	 * @return The training dataset.
	 */
	public DataSet GetTrainingSet()
	{
		return this.trainingSet;
	}
	
	/** Gets the test dataset.
	 * @return The test dataset.
	 */
	public DataSet GetTestingSet()
	{
		return this.testingSet;
	}
	
	/** Gets the attributes associated with the data points.
	 * @return An ArrayList of attribute names.
	 */
	public ArrayList<String> GetAttributes()
	{
		return attributes;
	}
	
	/**
	 * Normalizes all attributes in the training set.
	 */
	public void NormalizeTrainingSet()
	{
		for (String a : this.GetAttributes())
		{
			double min = this.trainingSet.FindMin(a);
			double max = this.trainingSet.FindMax(a);
			
			for (int i = 0; i < this.trainingSet.GetDataSetSize(); i++)
			{
				double val = this.trainingSet.GetPoint(i).getAttribute(a);
				val = this.NormailizeDataPoint(val, min, max);
				this.trainingSet.GetPoint(i).changeAttributeValue(a, val);
			}
		}
	}
	
	/**
	 * Normalizes all attributes in the testing set.
	 */
	public void NormailzeTestingSet()
	{
		for (String a : this.GetAttributes())
		{
			double min = this.testingSet.FindMin(a);
			double max = this.testingSet.FindMax(a);
			
			for (int i = 0; i < this.testingSet.GetDataSetSize(); i++)
			{
				double val = this.testingSet.GetPoint(i).getAttribute(a);
				val = this.NormailizeDataPoint(val, min, max);
				this.testingSet.GetPoint(i).changeAttributeValue(a, val);
			}
		}
	}
	
	/** Gets the actual data from the excel document.
	 * @param useAttributes
	 * @param sm The SplitMethod to use when dividing the data between the testing and training sets.
	 * @param percent The percent of the data that the training set should receive.
	 * @throws BiffException
	 * @throws IOException
	 */
	public void GetDataFromExcel(ArrayList<String> useAttributes, SplitMethod sm, int percent) throws BiffException, IOException
	{
		if (useAttributes != null)
		{
			LinkedList<DataPoint> tempPoints = new LinkedList<DataPoint>();
			this.splitMethod = sm;
			this.SetPercent(percent);
			this.trainingSet = new DataSet();
			this.testingSet = new DataSet();
			
			// Hopefully user never got the attributes from the excel file.
			if (this.GetAttributes().size() < 1)
				this.GetAttributesFromExcel();
			
			Workbook workbook = Workbook.getWorkbook(new File(this.excelPath));
			Sheet sheet = workbook.getSheet(0);
			int row = sheet.getRows();
			int col = sheet.getColumns();
			
			// Getting the data from the file.
			for(int r = 1; r < row;r++)
			{
				DataPoint p = new DataPoint();
				for(int c = 0; c < col; c++)
				{
					// only add the attribute if it is one of the requested attributes.
					if (useAttributes.contains(this.attributes.get(c))) 
					{
						if (c != col - 1)
							p.addAttribute(this.attributes.get(c), Double.parseDouble(sheet.getCell(c, r).getContents()));
						else
							p.setType(sheet.getCell(c, r).getContents());
					}
				}
				
				// Add the point to temporary linked list.		
				tempPoints.add(p);
			}
			
			this.DistributeDataPoint(tempPoints);
			tempPoints = null;	
		}
	}
	
	/** Gets the actual data from the excel document.
	 * @param sm The SplitMethod to use when dividing the data between the testing and training sets.
	 * @param percent The percent of the data that the training set should receive.
	 * @throws BiffException
	 * @throws IOException
	 */
	public void GetDataFromExcel(SplitMethod sm, int percent) throws BiffException, IOException
	{
		LinkedList<DataPoint> tempPoints = new LinkedList<DataPoint>();
		this.splitMethod = sm;
		this.SetPercent(percent);
		this.trainingSet = new DataSet();
		this.testingSet = new DataSet();
		
		// Hopefully user never got the attributes from the excel file.
		if (this.GetAttributes().size() < 1)
			this.GetAttributesFromExcel();
		
		Workbook workbook = Workbook.getWorkbook(new File(this.excelPath));
		Sheet sheet = workbook.getSheet(0);
		int row = sheet.getRows();
		int col = sheet.getColumns();
		
		// Getting the data from the file.
		for(int r = 1; r < row;r++)
		{
			DataPoint p = new DataPoint();
			for(int c = 0; c < col; c++)
			{
				if(c != col - 1)
					p.addAttribute(this.attributes.get(c), Double.parseDouble(sheet.getCell(c, r).getContents()));
				else
					p.setType(sheet.getCell(c,r).getContents());
			}
			
			// Add the point to temporary linked list.		
			tempPoints.add(p);
		}
		
		this.DistributeDataPoint(tempPoints);
		tempPoints = null;
	}
	
	/** Gets the attributes for the classes from the defined excel file.
	 * @throws BiffException
	 * @throws IOException
	 */
	public void GetAttributesFromExcel() throws BiffException, IOException
	{
		Workbook workbook = Workbook.getWorkbook(new File(this.excelPath));
		Sheet sheet = workbook.getSheet(0);
		int col = sheet.getColumns();
		
		// Getting attributes from file.
		for (int i = 0; i < col - 1; i++)
		{
			this.attributes.add(sheet.getCell(i, 0).getContents());
		}
	}
	
	
	/** Sets the split percent.
	 * @param percent Integer, 1 - 99.
	 */
	private void SetPercent(int percent)
	{
		if (0 < percent && percent < 100)
		{
			this.percent = ((double)percent) / 100;
		}
		else
		{
			System.out.println("Percent can be 1 - 99 and was given a value of " + percent + ". Using default value of 50%.");
			this.percent = .5;
		}
	}
	
	/** Distributes a linked list of datapoints into the training set and test set
	 * depending on the values that splitMethod and percent are set to.
	 * @param points The datapoints to distribute.
	 */
	private void DistributeDataPoint(LinkedList<DataPoint> points)
	{		
		switch (this.splitMethod)
		{
			// Counts how many of each class there is and then calculates how many to add to
			// the training set based on how many there is and what percent we want for the training set.
			case ClassPercent:
				HashMap<String, IntPair> counts = new HashMap<String, IntPair>();
				
				// Count how many of each class there is.
				for (int i = 0; i < points.size(); i++)
				{
					try {
						String type = points.get(i).getType();
						counts.get(type).second++;
					} catch (Exception e) {
						// New class.
						String type = points.get(i).getType();
	
						counts.put(type, new IntPair());
						// set the counter part of IntPair.
						counts.get(type).first = 0;
						// set the count part of IntPair
						counts.get(type).second = 1;
					}
				}
				
				// Using the count of the IntPair.second set it to a the percent
				// of that class we want.
				for (Entry<String, IntPair> entry : counts.entrySet())
				{
					int value = entry.getValue().second;
					entry.getValue().second = (int)Math.floor(value * this.percent);
				}
				
				// Distribute the datapoints into the test and training sets.
				while (points.size() > 0)
				{
					String type = points.getFirst().getType();
					
					if (counts.get(type).first < counts.get(type).second)
					{
						this.trainingSet.AddPoint(points.removeFirst());
						counts.get(type).first++;
					}
					else
					{
						this.testingSet.AddPoint(points.removeFirst());
					}
				}
			
				break;
				
			// Adds x points to the training set and y points to the testing set.
			// Repeats alternating between x and y until points is empty.
			// Makes the distribution even across the data set.
			case DataPercent:
				int testingCount = (int)Math.floor(this.percent * 100);
				int trainingCount = points.size() / (100 - testingCount);
				testingCount = points.size() / testingCount;
				
				while (points.size() > 0)
				{
					// Add x data points to the training set.
					for (int x = 0; x < trainingCount; x++)
					{
						if (points.isEmpty())
							break;
						else
							this.trainingSet.AddPoint(points.removeFirst());
					}
					
					// Add y data points to the testing set.
					for (int y = 0; y < testingCount; y++)
					{
						if (points.isEmpty())
							break;
						else
							this.testingSet.AddPoint(points.removeFirst());
					}
				}
				break;
				
			// Add a random point to the training set until it is at the specified percent.
			case RandomPercent:
				java.util.Random rand = new Random();
				int trainingSize = (int)Math.floor(points.size() * this.percent);
				
				for (int i = 0; i < trainingSize; i++)
				{
					int index = rand.nextInt(points.size());
					this.trainingSet.AddPoint(points.remove(index));
				}
				break;
			
			// Alternate between adding points to training set and test set.
			case EveryOther:
				boolean isTrainingSet = false;
				
				while(points.size() > 0)
				{
					if (isTrainingSet)
					{
						this.trainingSet.AddPoint(points.removeFirst());
						isTrainingSet = false; // testing sets turn.
					}
					else
					{
						this.testingSet.AddPoint(points.removeFirst());
						isTrainingSet = true; // training sets turn.
					}
				}
				break;
				
			default:
				break;
		}
		
		if (!points.isEmpty()) 
		{
			// add any remaining elements in points to testing set.
			for (DataPoint p : points) 
			{
				this.testingSet.AddPoint(p);
			} 
		}
	}
	
	private double NormailizeDataPoint(double value, double min, double max)
	{
		return (value - min) / (max - min);
	}
	
	public static void main(String[] args) throws BiffException, IOException
	{
		//String path = System.getProperty("user.dir")+ "\\data\\Iris Data Set.xls";
		String path = System.getProperty("user.dir")+ "\\data\\LettersDataSet_LessIsMore.xls";
				
		//DataModel test = DataModel.CreateFromExcel(path, SplitMethod.RandomPercent, 99);
		DataModel test = new DataModel(path);
		test.GetAttributesFromExcel();
		test.GetDataFromExcel(SplitMethod.ClassPercent, 75);
		System.out.println("Training size: " + test.GetTrainingSet().GetDataSetSize());
		System.out.println("Test size: " + test.GetTestingSet().GetDataSetSize());
	}
}
