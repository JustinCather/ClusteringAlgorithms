package algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import gui.UserGUI;

import struct.Cluster;
import struct.DataModel;
import struct.DataPoint;
import struct.DataSet;
import struct.Results;


/**
 * Implementation of a Fuzzy Clustering algorithm.
 *
 */
public class FuzzyClustering implements I_Algorithm {

	private DataSet set;
	private volatile boolean isAborted;
	private boolean isRunning;
	private int desiredClusterNumber;

	private ArrayList<DataPoint> prvCentroids;
	private ArrayList<DataPoint> crtCentroids;
	private LinkedList<double[]> fuzzyMatrix;
	private double fuzzynessFactor;
	private DataModel model;
	private double stoppingDistance;
	private State algState;
	private Results result;
	private ArrayList<Cluster> current;
	private transient double validity,sse;
	
	public FuzzyClustering() 
	{
		this.isRunning = false;
		this.isAborted = false;
	}
	
	@Override
	public void run() {
			current = new ArrayList<Cluster>();
			algState = State.Initializing;
			isRunning = true;
			try
			{
				this.model.GetDataFromExcel();
			}
			catch(Exception ex)
			{}
			this.set=model.GetTrainingSet();
			DataPoint centroid = set.GetCenter(model.GetUsedAttributes().toArray(new String[model.GetUsedAttributes().size()]));
			pickInitCentroids(set);
			
			this.fuzzyMatrix = new LinkedList<double[]>();
			for(int i=0;i<set.GetDataSetSize();i++)
			{
				fuzzyMatrix.add(new double[this.desiredClusterNumber]);
			}
			int it=1;
			algState = State.Running;
			while (isRunning && !isAborted)
			{
				System.out.println("Iteration " +it);
				it++;
				System.out.println("Assigning points");
				AssignPoints();
				System.out.println("Calculating centers");
				RecalcCentroids();
				System.out.println("Checking stop");
				boolean done=CheckStoppingCondition();
				if(done) break;
			}
			
			algState = State.Analyzing;
			ArrayList<Cluster> clusters = new ArrayList<Cluster>();
			for(int i =0; i< this.desiredClusterNumber; i++)
			{
				clusters.add(new Cluster(i));
				clusters.get(i).SetCentroid(crtCentroids.get(i));
			}
			int size = this.set.GetDataSetSize();
			for(int dp = 0; dp<size;dp++)
			{
				DataPoint temp = this.set.GetPoint(size-1-dp);
				int clusterNumber = 0;
				double strength = this.fuzzyMatrix.get(size-1-dp)[0];
				for(int i=1;i<this.desiredClusterNumber;i++)
				{
					if(this.fuzzyMatrix.get(size-1-dp)[i]>strength)
					{
						strength=this.fuzzyMatrix.get(size-1-dp)[i];
						clusterNumber =i;
					}
				}
				clusters.get(clusterNumber).AddDataPoint(temp);
				clusters.get(clusterNumber).SetAttributeNames(new ArrayList<String>(Arrays.asList(temp.GetAttributeNames())));
			}
			model.GetTrainingSet().SetIsPlotting(false);
			
			System.out.println("Results...");
			algState=State.Analyzing;
			sse=0.0;
			validity=0.0;
			for (int x = 0; x < clusters.size(); x++)
			{
				validity+= GetValidity(clusters.get(x), centroid);
				sse+=clusters.get(x).CalcSquaredError();
				clusters.get(x).SetClusterType();
				System.out.println("Cluster " + clusters.get(x).GetClusterID());
				System.out.println(clusters.get(x).ClusterStats());
			}
			current = clusters;
			
			GenerateResult();
		
			isRunning = false;
		}
	
	@Override
	public void Stop()
	{
		this.isAborted = true;
	}

	@Override
	public void Set(DataModel set, int numClusters) {
		
		this.desiredClusterNumber = numClusters;
		this.model = set;
		this.algState = State.Idle;
	}
	
	@Override
	public int GetDesiredClusters()
	{
		return desiredClusterNumber;
	}
	
	@Override
	public boolean IsRunning() {
		return this.isRunning;
	}

	@Override
	public ArrayList<Cluster> CurrentSolution() {
		return current;
	}

	@Override
	public boolean CheckStoppingCondition() {
		boolean isSame = true;
		String[] attr = set.GetPoint(0).GetAttributeNames();
		double maxDifference =0;
		if (crtCentroids.size() == prvCentroids.size()) 
		{
			for (int i = 0; i < crtCentroids.size(); i++) 
			{
				double delta  = crtCentroids.get(i).GetDistance(prvCentroids.get(i));
				if(delta>maxDifference)
					maxDifference=delta;
			} 
			System.out.println("Max change in centriods " + maxDifference);
			if(maxDifference > this.stoppingDistance)
				isSame=false;
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
	 * Assigns the data points the data set to clusters.
	 */
	private void AssignPoints()
	{
		//for each data point
		for(int dp=0; dp<set.GetDataSetSize();dp++)
		{
			//calculate the fuzzyness value for each centroid
			double[] fuzzy = new double[this.desiredClusterNumber];
			double fuzzySum=0;
			for(int i =0;i<this.crtCentroids.size();i++)
			{
				double distance = 1.0;
				double tempDistance = set.GetPoint(dp).GetDistance(this.crtCentroids.get(i));
				
				distance= Math.pow(distance/tempDistance, 2);
				distance = Math.pow(distance, (1/(this.fuzzynessFactor-1)));
				fuzzy[i]=distance;
				fuzzySum+=distance;
			}
			for(int i =0;i<this.crtCentroids.size();i++)
			{
				double distance = 1.0;
				double tempDistance = set.GetPoint(dp).GetDistance(this.crtCentroids.get(i));
				
				distance= Math.pow(distance/tempDistance, 2);
				distance = Math.pow(distance, (1/(this.fuzzynessFactor-1)));
				this.fuzzyMatrix.get(dp)[i]=(distance/fuzzySum);
				if(Double.isNaN(this.fuzzyMatrix.get(dp)[i]) || this.fuzzyMatrix.get(dp)[i]<.000001)
					this.fuzzyMatrix.get(dp)[i]=0;
			}
			
			
		}
		
	}
	
	/**
	 * Takes the data points in the cluster and recalculates the centroid.
	 */
	private void RecalcCentroids()
	{
		this.prvCentroids = this.crtCentroids;
		this.crtCentroids = new ArrayList<DataPoint>();
		
		for (int i = 0; i < this.desiredClusterNumber; i++)
		{
			double summationOfWeights =0;
			double[] summationofPoints = new double[this.set.GetPoint(0).GetNumberOfAttributes()];
			for(int dp=0;dp<this.set.GetDataSetSize();dp++)
			{
			//	System.out.println("Adding point " + (dp+1)+ " out of " +this.set.GetDataSetSize());
				DataPoint data = this.set.GetPoint(dp);
				String[] attributes = data.GetAttributeNames();
				summationOfWeights+=Math.pow(this.fuzzyMatrix.get(dp)[i],this.fuzzynessFactor);
				//System.out.println(summationOfWeights);
				for(int att = 0; att<attributes.length;att++)
				{
					summationofPoints[att]+= data.GetAttribute(attributes[att])*Math.pow(this.fuzzyMatrix.get(dp)[i],this.fuzzynessFactor);
				}
			}
			DataPoint data = this.set.GetPoint(0);
			String[] attributes = data.GetAttributeNames();
			DataPoint newCenter = new DataPoint();
			for(int att = 0; att<attributes.length;att++)
			{
				newCenter.AddAttribute(attributes[att], summationofPoints[att]/summationOfWeights);
				summationofPoints[att]/=summationOfWeights;
			}
			this.crtCentroids.add(newCenter);
		}
	}
	
	
	/** Picks the initial centroids for the start of the algorithm.
	 * @param s The data set to choose the centroids from.
	 */
	private void pickInitCentroids(DataSet s)
	{
		crtCentroids = new ArrayList<DataPoint>();
		
		for (int i = 0; i < desiredClusterNumber; i++)
		{
			boolean validIndex = false;
			
			
			while(!validIndex)
			{
				double temp = Math.random() * s.GetDataSetSize() - 1;
				int index = (int)Math.floor(temp);
				DataPoint tempPoint;
				tempPoint = s.GetPoint(index);
				
				if(!crtCentroids.contains(tempPoint))
				{
					validIndex = true;
					tempPoint.SetCentroid(true);
					crtCentroids.add(tempPoint);
					
				}
			}
		}
	}
	
	/** Sets the stopping distance at which the algorithm will terminate.
	 * @param dis The distance to terminate the algorithm at.
	 */
	public void SetStoppingDistance(double dis)
	{
		this.stoppingDistance=dis;
	}
	
	/** Gets the stopping distance at which the algorithm will terminate.
	 * @return The stopping distance that the algorithm will stop.
	 */
	public double GetStoppingDistance()
	{
		return stoppingDistance;
	}
	
	/** Sets the influence of the weights of data points when recalculating the centroid.
	 * @param fuz The value for the influence of the weight.
	 */
	public void SetFuzzyFactor(double fuz)
	{
		this.fuzzynessFactor=fuz;
	}
	
	/** Gets the influence of the weights of data points when recalculating the centroid.
	 * @return The value for the influence of the weight.
	 */
	public double GetFuzzyFactor()
	{
		return fuzzynessFactor;
	}
	
	@Override
	public Algorithm GetType()
	{
		return Algorithm.FuzzyLogic;
	}
	
	public String toString()
	{
		return GetType().toString() + " " + model.GetExcelFileName();
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
			algState = State.Finished;
		return result;
	}
	
	@Override
	public void GenerateResult()
	{
		result = new Results();
		result.alg = GetType();
		result.clusters=new Cluster[this.desiredClusterNumber];
		result.clusters=current.toArray(result.clusters);
		result.dataFileName=this.model.GetExcelFileName();
		int clusterNum=1;
		result.dataModel = model;
		result.stoppingDistance = this.stoppingDistance;
		result.fuzzyFactor=this.fuzzynessFactor;
		result.desiredClusters=this.desiredClusterNumber;
		result.output="Overall Validity = " + validity+"\nSum of Squared Error = " +sse+"\n";
		for (Cluster c : current)
		{
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
		for(int i = 0; i< this.current.size();i++)
		{
			if(i == c.GetClusterID()) continue;
			for(int j =0;j<current.get(i).GetDataPoints().size();j++)
			{
				outsideCohesionSeparation+=c.GetCentroid().GetDistance(current.get(i).GetDataPoint(j));
			}
			
			outsideCohesionSeparation/=prox;
			valid+=outsideCohesionSeparation;
		}
		return valid;
	}
}
