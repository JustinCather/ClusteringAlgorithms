package algorithms;

import java.util.ArrayList;

import gui.UserGUI;

import struct.Cluster;
import struct.DataModel;
import struct.Results;

public interface I_Algorithm extends Runnable   {
	
	
	/** Start the algorithm.
	 */
	void run();	
	
	/**
	 * Stops the algorithm safely.
	 */
	void Stop();
	
	/** Set the dataset for the algorithm. Must set before calling start!
	 * @param set The dataset to use.
	 * @param numClusters How many clusters the data should be clustered into.
	 * @param gui The GUI that algorithm should interface with.
	 */
	void Set(DataModel m, int numClusters);
	
	/** Checks if the algorithm has started and is still running.
	 * @return True if running, false if not.
	 */
	boolean IsRunning();
	
	/** Gets the state of the algorithm
	 * @return
	 */
	State GetState();
	
	/** Gets the data model being used by the algorithm.
	 * @return
	 */
	DataModel GetDataModel();
	
	/** Gets the current clusters of the solution.
	 * @return An ArrayList of clusters.
	 */
	ArrayList<Cluster> CurrentSolution();
	
	/**
	 * Checks if the algorithm has reached its stoping condition.
	 */
	boolean CheckStoppingCondition();
	
	/** Gets the desired number of clusters for the algorithm.
	 * @return The desired number of clusters.
	 */
	int GetDesiredClusters();

	/** Gets the results for this algorithm.
	 * @return
	 */
	Results GetResults();
	
	/** Gets the type of algorithm.
	 * @return
	 */
	Algorithm GetType();
	
	/**
	 * Generates a serialized result from clustering algorithm.
	 */
	void GenerateResult();
}
