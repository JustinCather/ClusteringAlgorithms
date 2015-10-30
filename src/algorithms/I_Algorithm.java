package algorithms;

import gui.UserGUI;
import struct.DataSet;

public interface I_Algorithm {
	
	void start() throws InterruptedException;
	
	void set(DataSet set, int clusters, UserGUI gui);
	
	boolean isRunning();
	
	DataSet currentSolution();
	
	void stop();

}
