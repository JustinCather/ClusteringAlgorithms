package algorithms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import gui.MessageBox;
import gui.UserGUI;
import gui.UserGui_V2;
import struct.Cluster;
import struct.DataModel;
import struct.DataPoint;
import struct.DataSet;
import struct.Results;

public class FuzzyClustering implements I_Algorithm {

	private DataSet set;
	private volatile boolean isAborted;
	private boolean isRunning;
	private int desiredClusterNumber;
	private UserGui_V2 userGUI;

	private ArrayList<DataPoint> prvCentroids;
	private ArrayList<DataPoint> crtCentroids;
	private LinkedList<double[]> fuzzyMatrix;
	private double fuzzynessFactor;
	private DataModel model;
	private double stoppingDistance;
	private State algState;
	private Results result;
	private ArrayList<Cluster> current;
	public FuzzyClustering() 
	{
		this.isRunning = false;
		this.isAborted = false;
	}
	@Override
	public void run() {
			isRunning = true;
			
			try
			{
				this.model.GetDataFromExcel();
			}
			catch(Exception ex)
			{}
			
			if (this.model.GetTrainingSet().GetDataSetSize() > this.desiredClusterNumber)
			{
				current = new ArrayList<Cluster>();
				algState = State.Initializing;
				
				this.set=model.GetTrainingSet();
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
					CheckStoppingCondition();	
				}
				
				algState = State.Analyzing;
				ArrayList<Cluster> clusters = new ArrayList<Cluster>();
				for(int i =0; i< this.desiredClusterNumber; i++)
					clusters.add(new Cluster(i));
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
				}
				model.GetTrainingSet().SetIsPlotting(true);
				userGUI.CurrentSolution(clusters);
				
				System.out.println("Results...");
				for (int x = 0; x < clusters.size(); x++)
				{
					clusters.get(x).SetClusterType();
					System.out.println("Cluster " + clusters.get(x).GetClusterID());
					System.out.println(clusters.get(x).ClusterStats());
				}
				current = clusters;
				algState=State.Analyzing;
				GenerateResult();
			}
			else
			{
				MessageBox.show("Cannot have more clusters than there are datapoints!", "To many clusters.");
			}
			
			//this.userGUI.SetAlgorithmFinished();	
			isRunning = false;
		}
	
	@Override
	public void Stop()
	{
		this.isAborted = true;
	}

	@Override
	public void Set(DataModel set, int numClusters, UserGui_V2 gui) {
		
		//dataSet = set;
		this.desiredClusterNumber = numClusters;
		userGUI = gui;
		this.model = set;
		fuzzynessFactor = 3;
		algState = State.Idle;
	}

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
	public void CheckStoppingCondition() {
		boolean isSame = true;
		String[] attr = set.GetPoint(0).GetAttributeNames();
		
		if (crtCentroids.size() == prvCentroids.size()) 
		{
			for (int i = 0; i < crtCentroids.size(); i++) 
			{
				for (int j = 0; j < attr.length; j++) 
				{
					if (Math.abs(crtCentroids.get(i).getAttribute(attr[j])- prvCentroids.get(i).getAttribute(attr[j]))>stoppingDistance) 
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
		
		// if not the same keep running.
		isRunning = !isSame;
	}
	
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
				System.out.println("Adding point " + (dp+1)+ " out of " +this.set.GetDataSetSize());
				DataPoint data = this.set.GetPoint(dp);
				String[] attributes = data.GetAttributeNames();
				summationOfWeights+=Math.pow(this.fuzzyMatrix.get(dp)[i],this.fuzzynessFactor);
				System.out.println(summationOfWeights);
				for(int att = 0; att<attributes.length;att++)
				{
					summationofPoints[att]+= data.getAttribute(attributes[att])*Math.pow(this.fuzzyMatrix.get(dp)[i],this.fuzzynessFactor);
				}
			}
			DataPoint data = this.set.GetPoint(0);
			String[] attributes = data.GetAttributeNames();
			DataPoint newCenter = new DataPoint();
			for(int att = 0; att<attributes.length;att++)
			{
				newCenter.addAttribute(attributes[att], summationofPoints[att]/summationOfWeights);
				summationofPoints[att]/=summationOfWeights;
			}
			this.crtCentroids.add(newCenter);
		}
	}
	
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
					tempPoint.setCentroid(true);
					crtCentroids.add(tempPoint);
					
				}
			}
		}
	}
	
	public void SetStoppingDistance(double dis)
	{
		this.stoppingDistance=dis;
	}
	public double GetStoppingDistance()
	{
		return stoppingDistance;
	}
	public void SetFuzzyFactor(double fuz)
	{
		this.fuzzynessFactor=fuz;
	}
	public double GetFuzzyFactor()
	{
		return fuzzynessFactor;
	}
	public Algorithm GetType()
	{
		return Algorithm.FuzzyLogic;
	}
	public String toString()
	{
		return GetType().toString() + " " + model.GetExcelFileName();
	}
	public DataModel GetDataModel()
	{
		return model;
	}
	
	public State GetState()
	{
		return algState;
	}
	
	public Results GetResults()
	{
		if(!isRunning)
			algState = State.Finished;
		return result;
	}
	private void GenerateResult()
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
		result.output="";
		for (Cluster c : current)
		{
			//result.output+="Cluster " + clusterNum + "\n" + c.ClusterStats()+"\n";
			//result.output+="Gini = " + c.CaclGiniIndex() + "\n";
			result.output += "Cluster " + clusterNum + " Gini=" + c.CaclGiniIndex() + "\n" + c.ClusterStats() + "\n\n";
			clusterNum++;
		}
		result.Serialize();
	}
}
