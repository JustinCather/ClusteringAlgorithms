package gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

import algorithms.Algorithm;
import algorithms.FuzzyClustering;
import algorithms.Hierarchical;
import algorithms.I_Algorithm;
import algorithms.K_Means;
import jxl.read.biff.BiffException;
import plotting.ScatterPlotEmbedded;
import struct.Cluster;
import struct.DataModel;
import struct.DataModel.SplitMethod;
import struct.DataSet;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JTextArea;

public class UserGUI {
	
	private I_Algorithm algorithm;
	private DataModel dataModel;
	private ScatterPlotEmbedded plot;

	private JTextArea resultsTextPane;
	private JComboBox<String> xComboBox;
	private JComboBox<String> yComboBox;
	private JPanel graphPanel;
	private JLabel lblStatus;
	private JComboBox<Algorithm> algorithmComboBox;
	private JComboBox<SplitMethod> dataSplitMethod;
	private JFormattedTextField clusterField;
	private JFormattedTextField trainingPercentField;
	private JFrame frmClusteringAlgorithms;
	private JTextField txtPathOfDataset;
	private JList<String> attributeList;
	private DefaultListModel<String> attr;
	private Thread background;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					UserGUI window = new UserGUI();
					window.frmClusteringAlgorithms.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void CurrentSolution(ArrayList<Cluster> clusters)
	{
		int clusterNum = 1;
		
		this.dataModel.GetTrainingSet().SetIsPlotting(true);	
		// Update graph.
		this.plot.SetXY(this.xComboBox.getItemAt(this.xComboBox.getSelectedIndex()), this.yComboBox.getItemAt(this.yComboBox.getSelectedIndex()) );
		this.graphPanel.removeAll();
		this.graphPanel.add(this.plot.DrawChart(clusters));		
		this.graphPanel.revalidate();
		
		// Update output
		for (Cluster c : clusters)
		{
			this.resultsTextPane.append("Cluster " + clusterNum + "\n" + c.ClusterStats());
			clusterNum++;
		}
		this.resultsTextPane.append("______________________________________\n");
		
		this.dataModel.GetTrainingSet().SetIsPlotting(false);
	}

	/**
	 * Create the application.
	 */
	public UserGUI() {
		initialize();
		 int delay = 1000; //milliseconds
		 ActionListener taskPerformer = new ActionListener() {
		      public void actionPerformed(ActionEvent evt) {
		          if(background!=null)
		          {
		        	  if(background.isAlive())return;
		        	  
		          }
		      }
		  };
		  new Timer(delay, taskPerformer).start();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		attr = new DefaultListModel<String>();
		plot = new ScatterPlotEmbedded();
		
		// for input of percents
		NumberFormatter percentFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
		percentFormatter.setValueClass(Integer.class);
		percentFormatter.setAllowsInvalid(false);
		percentFormatter.setMinimum(1);
		percentFormatter.setMaximum(99);
		
		// for cluster number input
		NumberFormatter clusterFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
		clusterFormatter.setValueClass(Integer.class);
		clusterFormatter.setAllowsInvalid(false);
		clusterFormatter.setMinimum(1);
		clusterFormatter.setMaximum(Integer.MAX_VALUE);
		
		frmClusteringAlgorithms = new JFrame();
		frmClusteringAlgorithms.setTitle("Clustering Algorithms");
		frmClusteringAlgorithms.setResizable(false);
		frmClusteringAlgorithms.setBounds(100, 100, 1070, 554);
		frmClusteringAlgorithms.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmClusteringAlgorithms.getContentPane().setLayout(null);
		
		graphPanel = new JPanel();
		graphPanel.setBorder(new TitledBorder(null, "Live Graph", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		graphPanel.setBounds(549, 127, 505, 334);
		frmClusteringAlgorithms.getContentPane().add(graphPanel);
		
		JPanel configPanel = new JPanel();
		configPanel.setBorder(new TitledBorder(null, "Configuration", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		configPanel.setBounds(10, 11, 529, 450);
		frmClusteringAlgorithms.getContentPane().add(configPanel);
		configPanel.setLayout(null);
		
		JPanel dataSetPanel = new JPanel();
		dataSetPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Dataset", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		dataSetPanel.setBounds(10, 27, 509, 56);
		configPanel.add(dataSetPanel);
		dataSetPanel.setLayout(null);
		
		JButton btnDataset = new JButton("Select Dataset");
		btnDataset.setBounds(10, 21, 124, 23);
		dataSetPanel.add(btnDataset);
		
		txtPathOfDataset = new JTextField();
		txtPathOfDataset.setEditable(false);
		txtPathOfDataset.setBounds(144, 22, 355, 20);
		dataSetPanel.add(txtPathOfDataset);
		txtPathOfDataset.setColumns(10);
		
		JPanel optionsPanel = new JPanel();
		optionsPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Options", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		optionsPanel.setBounds(10, 94, 255, 345);
		configPanel.add(optionsPanel);
		optionsPanel.setLayout(null);
		
		JLabel lblAlgorithm = new JLabel("Algorithm");
		lblAlgorithm.setBounds(44, 25, 45, 14);
		optionsPanel.add(lblAlgorithm);
		
		algorithmComboBox = new JComboBox<Algorithm>();
		algorithmComboBox.setModel(new DefaultComboBoxModel<Algorithm>(Algorithm.values()));
		lblAlgorithm.setLabelFor(algorithmComboBox);
		algorithmComboBox.setBounds(10, 42, 112, 20);
		optionsPanel.add(algorithmComboBox);
		
		JLabel lblClusters = new JLabel("Number of Clusters");
		lblClusters.setBounds(140, 25, 97, 14);
		optionsPanel.add(lblClusters);
		
		JLabel lblTrainingPercent = new JLabel("Training Percent");
		lblTrainingPercent.setBounds(27, 85, 78, 14);
		optionsPanel.add(lblTrainingPercent);
		
		JLabel lblTestingPercent = new JLabel("Testing Percent");
		lblTestingPercent.setBounds(149, 85, 78, 14);
		optionsPanel.add(lblTestingPercent);
		
		JLabel lblDataSplitMethod = new JLabel("Data Split Method");
		lblDataSplitMethod.setBounds(24, 134, 85, 14);
		optionsPanel.add(lblDataSplitMethod);
		
		dataSplitMethod = new JComboBox<SplitMethod>();
		dataSplitMethod.setModel(new DefaultComboBoxModel<SplitMethod>(SplitMethod.values()));
		dataSplitMethod.setBounds(10, 151, 112, 20);
		optionsPanel.add(dataSplitMethod);
		
		trainingPercentField = new JFormattedTextField(percentFormatter);
		trainingPercentField.getDocument().putProperty("number", "Text Area");
		trainingPercentField.setHorizontalAlignment(SwingConstants.CENTER);
		trainingPercentField.setValue(75);
		trainingPercentField.setBounds(10, 103, 112, 20);
		optionsPanel.add(trainingPercentField);
		
		JFormattedTextField testingPercentField = new JFormattedTextField(percentFormatter);
		testingPercentField.setHorizontalAlignment(SwingConstants.CENTER);
		testingPercentField.setValue(25);
		testingPercentField.setEditable(false);
		testingPercentField.setBounds(132, 103, 112, 20);
		optionsPanel.add(testingPercentField);
		
		clusterField = new JFormattedTextField(clusterFormatter);
		clusterField.setHorizontalAlignment(SwingConstants.CENTER);
		clusterField.setBounds(132, 42, 112, 20);
		clusterField.setValue(3);
		optionsPanel.add(clusterField);
		
		JPanel attributesPanel = new JPanel();
		attributesPanel.setBorder(new TitledBorder(null, "Attributes", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		attributesPanel.setBounds(275, 94, 244, 345);
		configPanel.add(attributesPanel);
		attributesPanel.setLayout(null);
		
		JButton btnAll = new JButton("All");
		btnAll.setBounds(129, 21, 75, 23);
		attributesPanel.add(btnAll);
		
		JButton btnNone = new JButton("None");
		btnNone.setBounds(39, 21, 75, 23);
		attributesPanel.add(btnNone);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 55, 224, 279);
		attributesPanel.add(scrollPane);
		
		attributeList = new JList<String>(attr);
		scrollPane.setViewportView(attributeList);
		attributeList.setBackground(SystemColor.menu);
		attributeList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		attributeList.setVisibleRowCount(20);
		
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new TitledBorder(null, "Controls", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		controlPanel.setBounds(10, 461, 398, 53);
		frmClusteringAlgorithms.getContentPane().add(controlPanel);
		controlPanel.setLayout(null);
		
		JButton btnStart = new JButton("Start");
		btnStart.setBounds(10, 19, 120, 23);
		controlPanel.add(btnStart);
		
		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				background.stop();
				CurrentSolution(algorithm.CurrentSolution());
			}
		});
		btnStop.setBounds(140, 19, 120, 23);
		controlPanel.add(btnStop);
		
		JButton btnGraphWindow = new JButton("Graph Window");
		btnGraphWindow.setBounds(270, 19, 120, 23);
		controlPanel.add(btnGraphWindow);
		
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new TitledBorder(null, "Status", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		statusPanel.setBounds(418, 461, 121, 53);
		frmClusteringAlgorithms.getContentPane().add(statusPanel);
		statusPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		lblStatus = new JLabel("STATUS");
		lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 18));
		statusPanel.add(lblStatus);
		
		JPanel graphPropertiesPanel = new JPanel();
		graphPropertiesPanel.setBorder(new TitledBorder(null, "Graph Properties", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		graphPropertiesPanel.setBounds(549, 461, 505, 53);
		frmClusteringAlgorithms.getContentPane().add(graphPropertiesPanel);
		graphPropertiesPanel.setLayout(null);
		
		xComboBox = new JComboBox<String>();
		xComboBox.setBounds(54, 22, 124, 20);
		graphPropertiesPanel.add(xComboBox);
		
		JLabel lblXValue = new JLabel("X Value:");
		lblXValue.setLabelFor(xComboBox);
		lblXValue.setBounds(10, 25, 46, 14);
		graphPropertiesPanel.add(lblXValue);
		
		JLabel lblYValue = new JLabel("Y Value:");
		lblYValue.setBounds(188, 25, 46, 14);
		graphPropertiesPanel.add(lblYValue);
		
		yComboBox = new JComboBox<String>();
		lblYValue.setLabelFor(yComboBox);
		yComboBox.setBounds(234, 22, 124, 20);
		graphPropertiesPanel.add(yComboBox);
		
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.setBounds(406, 21, 89, 23);
		graphPropertiesPanel.add(btnRefresh);
		
		JPanel resultsPanel = new JPanel();
		resultsPanel.setBorder(new TitledBorder(null, "Results", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		resultsPanel.setBounds(549, 11, 505, 105);
		frmClusteringAlgorithms.getContentPane().add(resultsPanel);
		resultsPanel.setLayout(null);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 15, 485, 79);
		resultsPanel.add(scrollPane_1);
		
		resultsTextPane = new JTextArea();
		scrollPane_1.setViewportView(resultsTextPane);
		resultsTextPane.setBackground(SystemColor.control);
		
		//////////////////////////////////////////////////////////////////////////
		////////////////////////////////EVENTS////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////
		
		// Start button clicked
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				RunModel();
			}
		});
		
		// Select data set button.
		btnDataset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				File selectedFile = null;
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
				
				int result = fileChooser.showOpenDialog(null);
				
				if (result == JFileChooser.APPROVE_OPTION)
				{
					selectedFile = fileChooser.getSelectedFile();
					txtPathOfDataset.setText(selectedFile.getAbsolutePath());
					LoadDataModel();
					PopulateAttributeBox();
				}
			}
		});
		
		// Select all attributes.
		btnAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				int[] indices = new int[attr.getSize()];
				for (int i = 0; i < indices.length; i++)
				{
					indices[i] = i;
				}
				
				attributeList.setSelectedIndices(indices);
			}
		});
		
		// Deselect all attributes.
		btnNone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				attributeList.clearSelection();
			}
		});
		
		// Update the testing set when the training set percent changes.
		trainingPercentField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				UpdateTestingSet();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				UpdateTestingSet();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				UpdateTestingSet();
			}
			
			public void UpdateTestingSet()
			{
				int val = (int)trainingPercentField.getValue();
				val = 100 - val;			
				testingPercentField.setValue(val);
			}
		});
		
		// Attribute selection changed.
		attributeList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) 
			{
				List<String> temp  = attributeList.getSelectedValuesList();
				xComboBox.removeAllItems();
				yComboBox.removeAllItems();
				
				for (String s : temp)
				{
					xComboBox.addItem(s);
					yComboBox.addItem(s);
				}
				
				if (yComboBox.getItemCount() > 1)
				{
					yComboBox.setSelectedIndex(1);
				}

			}
		});
		
		// Plot button refresh.
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				RefreshPlot();
			}
		});
	}
	
	private void RunModel()
	{
		System.gc();
		if(this.algorithm!=null)
		{
		if(this.algorithm.IsRunning())
			return;
		}
		if (this.dataModel != null) 
		{
			List<String> attributes = this.attributeList.getSelectedValuesList();
			int trainingPercent = (int) this.trainingPercentField.getValue();
			int numberOfClusters = (int) this.clusterField.getValue();
			SplitMethod splitMethod = this.dataSplitMethod.getItemAt(this.dataSplitMethod.getSelectedIndex());
			Algorithm algorithmType = this.algorithmComboBox.getItemAt(this.algorithmComboBox.getSelectedIndex());
			
			try 
			{
				this.lblStatus.setText("Running");
				this.dataModel.GetDataFromExcel(splitMethod, trainingPercent);
				
				if (attributes.size() > 1) 
				{
					this.plot.SetXY(attributes.get(0), attributes.get(1));
								
					switch (algorithmType) 
					{
						case FuzzyLogic:
							this.algorithm = new FuzzyClustering();
							break;
	
						case Hierarchical:
							this.algorithm = new Hierarchical();
							break;
	
						case K_Means:
							this.algorithm = new K_Means();
							break;
	
						default:
							break;
					}
					
					
					this.algorithm.Set(this.dataModel.GetTrainingSet(), numberOfClusters, this);
					background = new Thread(algorithm);
					background.start();
					
					this.lblStatus.setText("Ready");
				} 
				else 
				{
					MessageBox.show("You need to select at least one attribute.", "No attributes selected.");
				} 
			}
			catch (Exception e) 
			{
				MessageBox.show("Error: " + e.getMessage(), "ERROR");
			}
		}
		else
		{
			MessageBox.show("You need to select a data set first.", "No data set selected.");
		}
	}
	
	private void RefreshPlot()
	{
		this.plot.SetXY(this.xComboBox.getItemAt(this.xComboBox.getSelectedIndex()), this.yComboBox.getItemAt(this.yComboBox.getSelectedIndex()) );
		this.graphPanel.removeAll();	
		this.graphPanel.add(this.plot.DrawChart(this.algorithm.CurrentSolution()));	
		this.graphPanel.revalidate();
	}
	
	private void LoadDataModel()
	{
		try 
		{
			this.dataModel = new DataModel(txtPathOfDataset.getText());
			this.dataModel.GetAttributesFromExcel();
			Cluster.SetAttributeNames(dataModel.GetAttributes());
		} 
		catch (BiffException | IOException e) {
			MessageBox.show("Tried to get attributes from excel file, but an error occured. \n" + e.getMessage(), "Error Reading Excel File");
			this.dataModel = null;
			e.printStackTrace();
		}
	}
	
	private void PopulateAttributeBox()
	{
		if (this.dataModel != null)
		{
			this.attr.clear();
			
			for (String s : this.dataModel.GetAttributes())
			{
				this.attr.addElement(s);
			}
		}
	}
}
