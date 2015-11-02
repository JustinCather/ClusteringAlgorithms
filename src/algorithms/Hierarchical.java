package algorithms;

import java.io.IOException;
import java.util.ArrayList;

import gui.MessageBox;
import gui.UserGUI;
import jxl.read.biff.BiffException;
import struct.Cluster;
import struct.DataPoint;
import struct.DataSet;

public class Hierarchical implements I_Algorithm 
{
	private DataSet dataSet;
	private boolean isRunning;
	private int desiredClusterNumber;
	private UserGUI userGUI;
	private ArrayList<Cluster> clusters;
	
	public Hierarchical() 
	{
		this.isRunning = false;
	}
	
	@Override
	public void start() throws InterruptedException 
	{
		if (dataSet == null)
		{
			MessageBox.show("No data set.", "No dataset");
			return;
		}
		else
		{
			this.isRunning = true;

			int i = 0;			
			while(isRunning)
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
				
				// On every 10th iteration allow the gui to update.
				if (i % 10 == 0)
				{
					userGUI.currentSolution(this.dataSet);
				}
				while (this.dataSet.getIsPlotting()){Thread.sleep(500);}				
				i++;
			}
			
			System.out.println("Results...\r\n");
			for (int x = 0; x < clusters.size(); x++)
			{
				System.out.println("Cluster " + clusters.get(x).GetClusterID());
				System.out.println(clusters.get(x).ClusterStats());
			}
		}
	}

	@Override
	public void set(DataSet set, int numClusters, UserGUI gui) 
	{
		// Need a bigger data set than number of clusters.
		if(set.getDataSetSize() < numClusters)
		{
			MessageBox.show("You have entered more clusters than available points!", "ERROR");
			this.dataSet = null;
		}
		else
		{
			this.dataSet = set;
			this.desiredClusterNumber = numClusters;
			this.userGUI = gui;
			
			// Each data point is its own cluster initially.
			this.clusters = new ArrayList<Cluster>(set.getDataSetSize());
			
			for (int i = 0; i < set.getDataSetSize(); i++)
			{
				Cluster temp = new Cluster(i);
				temp.AddDataPoint(set.getPoint(i));
				temp.RecalculateCentroid();
				this.clusters.add(temp);
			}
		}
	}

	@Override
	public boolean isRunning() 
	{
		return this.isRunning;
	}

	@Override
	public DataSet currentSolution() 
	{
		// TODO Auto-generated method stub
		return null;
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
	
	private void AssignPoints()
	{	
		// Make a distance map of clusters.
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
				Cluster selected = clusters.get(closest);
				
				clusters.get(row).AddDataPoints(selected.GetDataPoints());
				selected.ClearDataPoints();
				selected.GetCentroid().setVisited(true);
				
				selected = null;
			}			
		} 
	}

	public static void main(String[] args) throws BiffException, IOException, InterruptedException 
	{
		DataSet winning = DataSet.CreateFromExcel("E:\\temp\\ClusteringAlgorithms\\ClusteringAlgorithms\\data\\Iris Data Set.xls");
		Cluster.SetAttributeNames(winning.getAttributes());
		Hierarchical h = new Hierarchical();
		h.set(winning, 3, new UserGUI());			
		h.start();		
	}

}