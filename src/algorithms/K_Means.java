package algorithms;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import gui.UserGui;
import struct.Cluster;
import struct.DataModel;
import struct.DataPoint;
import struct.DataSet;
import struct.Results;
import utilities.Dice;


/**
 * Implementation of the K Means algorithm.
 *
 */
public class K_Means implements I_Algorithm{
	
	private volatile boolean isAborted;
	private boolean isRunning;
	private int numClusters;
	private UserGui userGUI;
	private ArrayList<DataPoint> prvCentroids;
	private ArrayList<DataPoint> crtCentroids;
	private ArrayList<Cluster> clusters;
	private DataSet set;
	public DataModel model;
	public double stoppingDistance;
	private State algState;
	private Results result;
	private transient double validity,sse;
	public K_Means()
	{
		isRunning = false;
		this.isAborted = false;
	}
	
	/** Sets the minimum distance that each iteration's centroids must be to stop
	 * the algorithm.
	 * @param dis The stopping distance.
	 */
	public void SetStoppingDistance(double dis)
	{
		this.stoppingDistance = dis;
	}
	
	/** Gets the minimum stopping distance.
	 * @return The minimum stopping distance.
	 */
	public double GetStoppingDistance()
	{
		return stoppingDistance;
	}
	
	@Override
	public Algorithm GetType()
	{
		return Algorithm.K_Means;
	}
	
	@Override
	public String toString()
	{
		return GetType().toString() + " " + model.GetExcelFileName();
	}
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
	public void run()  {
		algState = State.Initializing;
		try
		{
			this.model.GetDataFromExcel();
		}
		catch(Exception ex)
		{}
		
		this.set = model.GetTrainingSet();
		String[] a = new String[model.GetUsedAttributes().size()];
		DataPoint centriod = set.GetCenter(model.GetUsedAttributes().toArray(a));
		if(set.GetDataSetSize() < numClusters)
		{
			gui.MessageBox.show("You have entered more clusters than available points.!!!!!","ERROR");
			return;
		}
			
		pickInitCentroids(set);
		if (clusters == null)
		{
			gui.MessageBox.show("No data set.", "No dataset");
			return;
		}
		else if(clusters.size()==0)
		{
			gui.MessageBox.show("No data set.", "No dataset");
			return;
		}
		else
		{
			isRunning = true;
			
			int i = 0;		
			while (isRunning && !isAborted)
			{
				AssignPoints();
				RecalcCentroids();
				boolean done = CheckStoppingCondition();			
				if(done)break;
				model.GetTrainingSet().SetIsPlotting(true);
					
				System.out.println(i + "th iteration...");
				for (int x = 0; x < clusters.size(); x++)
				{
					System.out.println("Cluster " + clusters.get(x).GetClusterID());
					System.out.println(clusters.get(x).ClusterStats());
				}
							
				i++;			
			}
			algState = State.Analyzing;
			model.GetTrainingSet().SetIsPlotting(false);
			
			System.out.println("Results...");
			validity=0;
			sse=0.0;
			
			for (int x = 0; x < clusters.size(); x++)
			{
				validity+= GetValidity(clusters.get(x), centriod);
				sse+=clusters.get(x).CalcSquaredError();
				clusters.get(x).SetClusterType();
				System.out.println("Cluster " + clusters.get(x).GetClusterID());
				System.out.println(clusters.get(x).ClusterStats());
			}
		}
		
		this.GenerateResult();
		//this.userGUI.SetAlgorithmFinished();
		this.isRunning = false;
	}
	
	@Override
	public void Stop()
	{
		this.isAborted = true;
	}

	@Override
	public void Set(DataModel set, int numClusters, UserGui ugui) {
		
		this.numClusters = numClusters;
		userGUI = ugui;
		this.model = set;
		clusters = new ArrayList<Cluster>(this.numClusters);
		algState = State.Idle;
		result = null;
	}

	@Override
	public boolean IsRunning() {
		return isRunning;
	}

	@Override
	public ArrayList<Cluster> CurrentSolution() {
		return this.clusters;
	}

	public int GetDesiredClusters()
	{
		return numClusters;
	}
	@Override
	public boolean CheckStoppingCondition() {
		boolean isSame = true;
		String[] attr = clusters.get(0).GetCentroid().GetAttributeNames();
		
		if (crtCentroids.size() == prvCentroids.size()) 
		{
			for (int i = 0; i < crtCentroids.size(); i++) 
			{
				for (int j = 0; j < attr.length; j++) 
				{
					if ((crtCentroids.get(i).GetAttribute(attr[j])- prvCentroids.get(i).GetAttribute(attr[j]))>stoppingDistance) 
					{
						isSame = false;
						break;
					}
				}
				if(!isSame)
					break;
			} 
		}
		else
		{
			isSame = false;
		}
		return isSame;
		// if not the same keep running.
		//isRunning = !isSame;
	}
	
	/**
	 *  Distributes the data points in the set to the
	 *  cluster that the data point is closest to.
	 */
	private void AssignPoints()
	{
		//foreach cluster
		for(int c =0;c<clusters.size();c++)
		{
			Cluster temp = clusters.get(c);
			LinkedList<DataPoint> data = temp.RemoveUnAssigned();
			
			//foreach datapoint in cluster
			int stop = data.size();
			if(set!=null)
				stop = set.GetDataSetSize();
			for(int d =stop-1; d>=0;d--)
			{
				DataPoint p;
				if(set!=null)
					p= set.GetPoint(d);
				else
					p = data.get(d);
				int cluster = 0;
				double distance = Double.MAX_VALUE;
				for(int newC=0;newC<clusters.size();newC++)
				{
					double dis = clusters.get(newC).GetDistance(p);
					if(dis<distance)
					{
						distance = dis;
						cluster = newC;
					}
				}
				//p.assigned=true;
				p.SetClusterNumber(this.clusters.get(cluster).GetClusterID());
				this.clusters.get(cluster).AddDataPoint(p);
				if(set!=null)
					set.RemoveDataPoint(d);
			}
			if(set!=null)
			{
				set = null;
				break;
			}
		}

		//compare distance to all other clusters
		/*for(int i = 0; i < dataSet.GetDataSetSize(); i++)
		{
			DataPoint p = dataSet.GetPoint(i);
			int cluster = 0;
			double distance = this.clusters.get(0).GetDistance(p);
			for(int j = 1; j < this.clusters.size(); j++)
			{
				double dis =  this.clusters.get(j).GetDistance(p);
				
				if (dis < distance)
				{
					distance = dis;
					cluster = j;
				}
			}
			
			p.setClusterNumber(this.clusters.get(cluster).GetClusterID());
			this.clusters.get(cluster).AddDataPoint(p);
		}*/
	}
	
	/**
	 * Saves the previous iterations centroids and calculates the
	 * newest iterations centroids.
	 */
	private void RecalcCentroids()
	{
		this.prvCentroids = this.crtCentroids;
		this.crtCentroids = new ArrayList<DataPoint>();
		
		for (int i = 0; i < this.clusters.size(); i++)
		{
			clusters.get(i).RecalculateCentroid();
			this.crtCentroids.add(clusters.get(i).GetCentroid());
		}
	}
	
	/** Should only be called once. Picks the initial centroids to 
	 * build the clustering model from.
	 * @param s
	 */
	private void pickInitCentroids(DataSet s)
	{
		clusters = new ArrayList<Cluster>(this.numClusters);
		crtCentroids = new ArrayList<DataPoint>();
		
		for (int i = 0; i < numClusters; i++)
		{
			boolean validIndex = false;
			clusters.add(new Cluster(i));
			
			while(!validIndex)
			{
				//double temp = Math.random() * s.GetDataSetSize() - 1;
				int index = Dice.roll(s.GetDataSetSize()); //(int)Math.floor(temp);
				DataPoint tempPoint;
				tempPoint = s.GetPoint(index);
				
				if(!crtCentroids.contains(tempPoint))
				{
					validIndex = true;
					tempPoint.SetCentroid(true);
					crtCentroids.add(tempPoint);
					clusters.get(i).SetCentroid(tempPoint);
					
					clusters.get(i).SetAttributeNames(new ArrayList<String>(Arrays.asList(tempPoint.GetAttributeNames())));
				}
			}
		}
	}
	
	@Override
	public void GenerateResult()
	{
		result = new Results();
		result.alg = GetType();
		result.clusters=new Cluster[this.numClusters];
		result.clusters=clusters.toArray(result.clusters);
		result.dataFileName=this.model.GetExcelFileName();
		int clusterNum=1;
		result.dataModel = model;
		result.stoppingDistance = this.stoppingDistance;
		result.fuzzyFactor=0.0;
		result.output="Overall Validity = " + validity+"\nSum of Squared Error = " +sse+"\n";
		result.desiredClusters=this.numClusters;
		for (Cluster c : clusters)
		{
			//result.output+="Cluster " + clusterNum + "\n" + c.ClusterStats()+"\n";
			//result.output+="Gini = " + c.CaclGiniIndex() + "\n";
			result.output += "Cluster " + clusterNum + " Gini = " + c.CaclGiniIndex() +"\n"+c.ClusterStats() + "\n\n";
			clusterNum++;
		}
		result.Serialize();
	}
	
	/** Calculate the cohesion between points in a given cluster
	 * @param Cluster c
	 * @return double Cohesion betwen points in a cluster
	 */
	private double  PointBasedCohesion(Cluster c)
	{
		double proximity = 0.0;
		for(int i =0; i<c.GetDataPoints().size();i++)
		{
			for(int j=0;j<c.GetDataPoints().size();j++)
				proximity+=c.GetDataPoint(i).GetDistance(c.GetDataPoint(j));
		}
		return proximity;
	}
	
	/** Calculate the cohesion between points in a given cluster to the centroid
	 * @param Cluster c
	 * @return double Cohesion between points in a cluster to the centroid
	 */
	private double CentroidBasedCohesion(Cluster c)
	{
		double proximity = 0.0;
		for(int i =0; i<c.GetDataPoints().size();i++)
		{
			proximity = c.GetDataPoint(i).GetDistance(c.GetCentroid());
		}
		return proximity;
	}
	
	/** Calculate the separation between a cluster centriod
	 * and the overall centriod
	 * @param Cluster c
	 * @param Datapoint center
	 * @return double separation between centriod of c and center
	 */
	private double Separation(Cluster c ,DataPoint center)
	{
		return c.GetCentroid().GetDistance(center);
	}
	
	/** Calculate the validity of a cluster
	 * @param Cluster c
	 * @param Datapoint center
	 * @return double validity of cluster c
	 */
	public double GetValidity(Cluster c ,DataPoint center)
	{
		double prox=PointBasedCohesion(c);
		double valid= (1.0/c.GetDataPoints().size())*prox + CentroidBasedCohesion(c)+(c.GetDataPoints().size()*Separation(c,center));
		double outsideCohesionSeparation=0.0;
		for(int i = 0; i< this.clusters.size();i++)
		{
			if(i == c.GetClusterID()) continue;
			for(int j =0;j<clusters.get(i).GetDataPoints().size();j++)
			{
				outsideCohesionSeparation+=c.GetCentroid().GetDistance(clusters.get(i).GetDataPoint(j));
			}
			
			outsideCohesionSeparation/=prox;
			valid+=outsideCohesionSeparation;
		}
		return valid;
	}
}
