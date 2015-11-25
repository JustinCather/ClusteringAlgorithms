package algorithms;

import java.io.Serializable;
import java.util.ArrayList;

import gui.UserGUI;
import gui.UserGui_V2;
import struct.Cluster;
import struct.DataModel;
import struct.DataSet;
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
	void Set(DataModel m, int numClusters, UserGui_V2 gui);
	
	/** Checks if the algorithm has started and is still running.
	 * @return True if running, false if not.
	 */
	boolean IsRunning();
	
	State GetState();
	DataModel GetDataModel();
	
	/** Gets the current clusters of the solution.
	 * @return An ArrayList of clusters.
	 */
	ArrayList<Cluster> CurrentSolution();
	
	/**
	 * Checks if the algorithm has reached its stoping condition.
	 */
	void CheckStoppingCondition();
	int GetDesiredClusters();

	Results GetResults();
	Algorithm GetType();

}
