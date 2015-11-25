package algorithms;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.jfree.data.xy.XYDataset;

import gui.MessageBox;
import gui.UserGUI;
import gui.UserGui_V2;
import jxl.read.biff.BiffException;
import plotting.ScatterPlotEmbedded;
import plotting.ScatterPlotWindow;
import struct.Cluster;
import struct.DataModel;
import struct.DataModel.SplitMethod;
import struct.DataPoint;
import struct.DataSet;
import struct.Results;
import utilities.Dice;

public class Hierarchical implements I_Algorithm
{
	//private DataSet dataSet;
	private volatile boolean isAborted;
	private boolean isRunning;
	private int desiredClusterNumber;
	private UserGui_V2 userGUI;
	private ArrayList<Cluster> clusters;
	private DataModel model;
	private State algState;
	private Results result;
	
	public Hierarchical() 
	{
		this.isRunning = false;
		this.isAborted = false;
	}
	
	@Override
	public Algorithm GetType()
	{
		return Algorithm.Hierarchical;
	}

	@Override
	public DataModel GetDataModel()
	{
		return model;
	}
	
	@Override
	public State GetState()
	{
		return algState;
	}
	
	@Override
	public Results GetResults()
	{
		if(!isRunning)
			algState=State.Finished;
		return result;
	}
	
	@Override
	public void run() 
	{	
		this.isRunning = true;	
		
		try
		{
			this.model.GetDataFromExcel();
		}
		catch(Exception ex)
		{}
		
		if (this.model.GetTrainingSet().GetDataSetSize() > this.desiredClusterNumber)
		{
			algState = State.Initializing;
			this.clusters = new ArrayList<Cluster>(model.GetTrainingSet().GetDataSetSize());
			
			for (int i = 0; i < model.GetTrainingSet().GetDataSetSize(); i++)
			{
				Cluster temp = new Cluster(i);
				temp.AddDataPoint(model.GetTrainingSet().GetPoint(i));
				temp.RecalculateCentroid();
				this.clusters.add(temp);
			}
			
			algState = State.Running;
			while(isRunning && !isAborted)
			{				
				// Assign the points in the data set to a cluster.
				this.AssignPoints();
				this.MergeClusters();
				
				// Recalc the centroid for each cluster.
				for (int j = 0; j < this.clusters.size(); j++)
				{
					this.clusters.get(j).RecalculateCentroid();
				}
				
				// Check if algorithm reached the desired number of clusters.
				this.CheckStoppingCondition();
				model.GetTrainingSet().SetIsPlotting(true);
				userGUI.CurrentSolution(this.clusters);		
				
			}
			
			algState = State.Analyzing;
			System.out.println("Results...");
			for (int x = 0; x < clusters.size(); x++)
			{
				clusters.get(x).SetClusterType();
				System.out.println("Cluster " + clusters.get(x).GetClusterID());
				System.out.println(clusters.get(x).ClusterStats());
			}
			algState=State.Analyzing;
			GenerateResult();
		}
		else
		{
			MessageBox.show("Cannot have more clusters than there are datapoints!", "To many clusters.");
		}		
			
		//this.userGUI.SetAlgorithmFinished();
		this.isRunning = false;
	}
	
	@Override
	public void Stop()
	{
		this.isAborted = true;
	}

	@Override
	public void Set(DataModel set, int numClusters, UserGui_V2 gui) 
	{
		this.desiredClusterNumber = numClusters;
		this.userGUI = gui;
		model = set;
		algState = State.Idle;
	}

	@Override
	public boolean IsRunning() 
	{
		return this.isRunning;
	}

	@Override
	public ArrayList<Cluster> CurrentSolution() 
	{
		return this.clusters;
	}

	public int GetDesiredClusters()
	{
		return desiredClusterNumber;
	}
	
	@Override
	public void CheckStoppingCondition() 
	{
		this.clusters.trimToSize();
		
		if (this.clusters.size() <= this.desiredClusterNumber)
		{
			this.isRunning = false;
		}
	}
	
	/**
	 * Removes any clusters from the clusters array if the cluster
	 * has no data points.
	 */
	private void MergeClusters()
	{
		ArrayList<Cluster> temp = new ArrayList<Cluster>();
		
		for (int i = 0; i < clusters.size(); i++)
		{
			if (clusters.get(i).HasDataPoints())
			{
				temp.add(clusters.get(i));
			}
		}
		
		this.clusters = null;
		this.clusters = temp;
		temp = null;
		
		System.out.println("Number of clusters: " + clusters.size());
	}
	
	/**
	 * Combines any two clusters that are nearest to each other
	 * into one single cluster. 
	 */
	private void AssignPoints()
	{	
		// Make a distance map of clusters.
		boolean didCombineClusters = false;
		int size = clusters.size();
		int init = -1;
		double[][] distanceMap = new double[size][size];
		for (int row = 0; row < size; row++)
		{
			for (int col = 0; col < size; col++)
			{
				distanceMap[row][col] = clusters.get(row).GetDistance(clusters.get(col).GetCentroid());
				//System.out.print(distanceMap[row][col] + " ");
			}
			//System.out.println("\n");
		}
		
		for (int row = 0; row < size; row++)
		{
			double distance;			
			int closest = -1;
			// Get the first distance value from map
			if (row == 0)
				init = 1;
			else
				init = 0;
			
			distance = distanceMap[row][init];
			
			for (int col = 0; col < size; col++)
			{				
				if (row == col)
				{
					// distance will be zero in map. Nearest neighbor is not itself!
					continue;
				}
				else
				{
					double temp = distanceMap[row][col];	
					boolean isContinue = false;
					
					if (temp < distance)
					{
						// Found a smaller distance.
						for (int i = 0; i < size; i++)
						{
							// See if there is a smaller distance for another cluster.
							if (distanceMap[i][col] < temp && i != col)
							{
								// There is a different cluster that is closer.
								isContinue = false;
								break;
							}
							
							isContinue = true;
						}
						
						if (isContinue)
						{
							// Found a new valid smallest distance.
							distance = temp;
							closest = col;
						}
					}				
				}
			}
			
			// pre-check
			if (closest == -1)
			{
				// Means we didn't find a smaller value then initial value of distance.
				for (int i = 0; i < size; i++)
				{
					if (i == row)
						continue;
					
					double temp = distanceMap[row][i];
					
					if (temp < distance)
					{
						closest = -1;
						break;
					}
					else
					{
						closest = init;
					}
				}
			}
			
			// post-check.
			if (closest != -1 && clusters.get(closest).GetCentroid().isVisited() == false)
			{
				// Found the closest neighbor cluster that was no closer to any other cluster.
				// So combine their data points.
				didCombineClusters = true;
				Cluster selected = clusters.get(closest);
				
				clusters.get(row).AddDataPoints(selected.GetDataPoints());
				selected.ClearDataPoints();
				selected.GetCentroid().setVisited(true);
				
				selected = null;
			}			
		}
		
		// Check to make sure at least two cluster were merged. If none combined, then the clusters are an equal distance from
		// each other. Pick a random cluster and merge it with its nearest neighbor.
		if (!didCombineClusters)
		{
			int r = Dice.roll(this.clusters.size());
			int c = 0;
			Cluster selected = this.clusters.get(r);		
			Cluster mSelected = null;
			
			// init distance.
			double d = distanceMap[r][c]; //heh
			
			for (int i = 1; i < this.clusters.size(); i++)
			{
				double temp = distanceMap[r][i];
				
				if (temp < d)
				{
					d = temp;
					c = i;
				}
			}
			
			mSelected = this.clusters.get(c);			
			selected.AddDataPoints(mSelected.GetDataPoints());
			mSelected.ClearDataPoints();
			mSelected.GetCentroid().setVisited(true);
			
			mSelected = null;
			selected = null;
		}
	}
	
	public String toString()
	{
		return GetType().toString() + " " + model.GetExcelFileName();
	}
	
	private void GenerateResult()
	{
		result = new Results();
		result.alg = GetType();
		result.clusters=new Cluster[this.desiredClusterNumber];
		result.clusters=clusters.toArray(result.clusters);
		result.dataFileName=this.model.GetExcelFileName();
		int clusterNum=1;
		result.dataModel = model;
		result.stoppingDistance = 0.0;
		result.fuzzyFactor=0.0;
		result.output="";
		result.desiredClusters=this.desiredClusterNumber;
		for (Cluster c : clusters)
		{
			result.output+="Cluster " + clusterNum + "\n" + c.ClusterStats()+"\n";
			result.output+="Gini = " + c.CaclGiniIndex() + "\n";
			clusterNum++;
		}
		result.Serialize();
	}

	public static void main(String[] args) throws BiffException, IOException, InterruptedException 
	{
		String path = System.getProperty("user.dir")+ "\\data\\Iris Data Set.xls";
		int clusters = 3;
		
		DataModel winning = new DataModel(path);
		winning.GetDataFromExcel(SplitMethod.RandomPercent, 75);
		Cluster.SetAttributeNames(winning.GetAttributes());
		Hierarchical h = new Hierarchical();
		
		//h.Set(winning.GetTrainingSet(), clusters, new UserGUI());			

		Thread t = new Thread(h);
		t.start();
	}
	
}
