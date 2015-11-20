package algorithms;

import java.util.ArrayList;

import gui.UserGUI;
import struct.Cluster;
import struct.DataSet;

public interface I_Algorithm extends Runnable {
	
	
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
	void Set(DataSet set, int numClusters, UserGUI gui);
	
	/** Checks if the algorithm has started and is still running.
	 * @return True if running, false if not.
	 */
	boolean IsRunning();
	
	
	/** Gets the current clusters of the solution.
	 * @return An ArrayList of clusters.
	 */
	ArrayList<Cluster> CurrentSolution();
	
	/**
	 * Checks if the algorithm has reached its stoping condition.
	 */
	void CheckStoppingCondition();

}
