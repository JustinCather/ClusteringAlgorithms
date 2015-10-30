package gui;

import java.awt.EventQueue;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextPane;

import struct.DataSet;

public class UserGUI {

	private static final String DATA_PATH = System.getProperty("user.dir") + "\\data\\";
	private String[] dataList;
	private DataSet dataSet;
	
	private JFrame frmClusteringAlgorithm;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UserGUI window = new UserGUI();
					window.frmClusteringAlgorithm.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public UserGUI() {
		fillDataBox();
		initialize();
	}
	
	public void currentSolution(DataSet set)
	{
		dataSet = set;
	}
	
	// Scans the DATA_PATH directory for excel data files
		private void fillDataBox(){
			File f = new File(DATA_PATH);
			dataList = f.list();
		}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmClusteringAlgorithm = new JFrame();
		frmClusteringAlgorithm.setTitle("Clustering Algorithm");
		frmClusteringAlgorithm.setResizable(false);
		frmClusteringAlgorithm.setBounds(100, 100, 314, 385);
		frmClusteringAlgorithm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmClusteringAlgorithm.getContentPane().setLayout(null);
		
		JComboBox algorithmComboBox = new JComboBox();
		algorithmComboBox.setModel(new DefaultComboBoxModel(new String[] {"K-Means"}));
		algorithmComboBox.setBounds(10, 26, 115, 20);
		frmClusteringAlgorithm.getContentPane().add(algorithmComboBox);
		
		JLabel algorithmLabel = new JLabel("Clustering Algorithm");
		algorithmLabel.setLabelFor(algorithmComboBox);
		algorithmLabel.setBounds(10, 11, 97, 14);
		frmClusteringAlgorithm.getContentPane().add(algorithmLabel);
		
		JButton btnRun = new JButton("Run");
		btnRun.setBounds(10, 315, 89, 23);
		frmClusteringAlgorithm.getContentPane().add(btnRun);
		
		JButton btnGraph = new JButton("Graph");
		btnGraph.setBounds(109, 315, 89, 23);
		frmClusteringAlgorithm.getContentPane().add(btnGraph);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(208, 315, 89, 23);
		frmClusteringAlgorithm.getContentPane().add(btnCancel);
		
		JTextPane output = new JTextPane();
		output.setBounds(10, 57, 287, 226);
		frmClusteringAlgorithm.getContentPane().add(output);
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(64, 294, 180, 14);
		frmClusteringAlgorithm.getContentPane().add(progressBar);
		
		JComboBox dataComboBox = new JComboBox<String>(dataList);
		dataComboBox.setBounds(182, 26, 115, 20);
		frmClusteringAlgorithm.getContentPane().add(dataComboBox);
		
		JLabel lblDataFile = new JLabel("Data File");
		lblDataFile.setLabelFor(dataComboBox);
		lblDataFile.setBounds(182, 11, 46, 14);
		frmClusteringAlgorithm.getContentPane().add(lblDataFile);
	}
}
