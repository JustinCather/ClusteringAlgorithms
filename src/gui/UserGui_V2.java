package gui;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.jfree.chart.ChartPanel;

import algorithms.Algorithm;
import algorithms.FuzzyClustering;
import algorithms.Hierarchical;
import algorithms.I_Algorithm;
import algorithms.K_Means;
import algorithms.State;
import jxl.read.biff.BiffException;
import plotting.ScatterPlotEmbedded;
import struct.Cluster;
import struct.DataModel;
import struct.DataModel.SplitMethod;
import struct.DataSet;
import struct.Results;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JTextArea;
import javax.swing.JCheckBox;
import javax.swing.JTabbedPane;
import javax.swing.JTree;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

import javax.swing.Box;
import javax.swing.JSeparator;
import javax.swing.border.LineBorder;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.AbstractAction;
import javax.swing.Action;
import utilities.Preprocessing.SmoothMethod;

public class UserGui_V2 {

	private I_Algorithm algorithm;
	private DataModel dataModel;
	private ScatterPlotEmbedded plot;

	private TestingPanel testingPanel;
	private UserGui_V2 passable;
	private JFormattedTextField textFieldNumBins;
	private JCheckBox chckbxSmooth;
	private JComboBox<SmoothMethod> comboBoxSmoothMethod;
	private JCheckBox chckbxNormalizeData;
	private JComboBox<SplitMethod> dataSplitMethod;
	private JFormattedTextField trainingPercentField;
	private JFrame frmClusteringAlgorithms;
	private DefaultListModel<String> attr;
	private Thread background;
	private JTextField txtPathOfDataset;
	private JTextField textField_1;
	private JTextField textField_2;
	private JPanel DataSet, Algorthim,Graph,ResultsPanel;
	private JList availble,selected,list_1;
	JComboBox<Algorithm> algorithmComboBox;
	JComboBox<String> yComboBox,xComboBox;
	JTextArea resultsTextPane;
	JPanel graphPanel;
	JLabel label_4;
	JFormattedTextField clusterField;
	Results f;
	
	private final Action action = new SwingAction();
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					UserGui_V2 window = new UserGui_V2();
					window.frmClusteringAlgorithms.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	
	public UserGui_V2() {
		initialize();
		passable=this;
		 int delay = 10; //milliseconds
		 ActionListener taskPerformer = new ActionListener() {
		      public void actionPerformed(ActionEvent evt) {

		        	  if(algorithm!=null)
		        	  {
		        		  State s = algorithm.GetState();
		        		  label_4.setText(s.toString());
		        		  if(!algorithm.IsRunning() && s==State.Analyzing)
		        		  {
		        			Results finalResults = algorithm.GetResults();
		        			finalResults = Results.Deserialize(finalResults.path);
		        			LoadGraphsAndResults(algorithm.GetResults());
		        			if(((DefaultListModel)list_1.getModel()).size()>0)
		        			{
			        			algorithm = (I_Algorithm)((DefaultListModel)list_1.getModel()).remove(0);
			      				xComboBox.removeAllItems();
			      				yComboBox.removeAllItems();
			      				background = new Thread(algorithm);
			    				background.start();
		        			}
		      				
		      				//resultsTextPane.setText("");
		        		  }
		        	  }
		        	  
		          }
		      
		  };
		 Timer t= new Timer(delay, taskPerformer);
		 t.start();
		 f=null;
	}
	
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
		frmClusteringAlgorithms.getContentPane().setBackground(SystemColor.menu);
		frmClusteringAlgorithms.setTitle("Clustering Algorithms");
		frmClusteringAlgorithms.setResizable(false);
		frmClusteringAlgorithms.setBounds(100, 100, 942, 610);
		frmClusteringAlgorithms.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmClusteringAlgorithms.getContentPane().setLayout(null);
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Clustering");
		JTree tree = new JTree(root);
		tree.getSelectionModel().setSelectionMode
        (TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent e) {
		        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		                           tree.getLastSelectedPathComponent();

		    /* if nothing is selected */ 
		        if (node == null) return;
		        //hide panels
		        Algorthim.setVisible(false);
		        DataSet.setVisible(false);
		        ResultsPanel.setVisible(false);
		        Graph.setVisible(false);
		        testingPanel.setVisible(false);
		        if(node.toString().equals("Data Set"))
		        {
		        	DataSet.setVisible(true);
		        }
		        if(node.toString().equals("Algorithm"))
		        {
		        	Algorthim.setVisible(true);
		        }
		        if(node.toString().equals("Graphs"))
		        {
		        	Graph.setVisible(true);
		        }
		        if(node.toString().equals("Results"))
		        {
		        	ResultsPanel.setVisible(true);
		        }
		        if (node.toString().equals("Testing"))
		        {
		        	testingPanel.setVisible(true);
		        }
		    /* retrieve the node that was selected */ 
		        Object nodeInfo = node.getUserObject();
		      //  ...
		    /* React to the node selection. */
		       // ...
		    }
		});
		tree.setBounds(0, 110, 134, 350);
		DefaultMutableTreeNode subCat = new DefaultMutableTreeNode("Data Set");
		root.add(subCat);
		subCat = new DefaultMutableTreeNode("Algorithm");
		root.add(subCat);
		subCat = new DefaultMutableTreeNode("Results");
		root.add(subCat);
		subCat = new DefaultMutableTreeNode("Graphs");
		root.add(subCat);
		subCat = new DefaultMutableTreeNode("Testing");
		root.add(subCat);
		
		Graph = new JPanel();
		Graph.setVisible(false);
		
		JButton btnX = new JButton("Remove");
		btnX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				DefaultListModel model = (DefaultListModel) list_1.getModel();

				
				int selectedIndex = list_1.getSelectedIndex();
				if (selectedIndex != -1) {
					String s = model.getElementAt(selectedIndex).toString();
				    model.remove(selectedIndex);
				}
				
			}
		});
		
		Algorthim = new JPanel();
		Algorthim.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		Algorthim.setVisible(false);
		

		ResultsPanel = new JPanel();
		ResultsPanel.setVisible(false);
		
				ResultsPanel.setBounds(154, 114, 764, 327);
				frmClusteringAlgorithms.getContentPane().add(ResultsPanel);
				ResultsPanel.setLayout(null);
				
				JScrollPane scrollPane3 = new JScrollPane();
				scrollPane3.setViewportBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Results", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
				scrollPane3.setBounds(0, 0, 764, 327);
				ResultsPanel.add(scrollPane3);
				resultsTextPane = new JTextArea();
				resultsTextPane.setBounds(0, 0, 731, 327);
				resultsTextPane.setEditable(false);
				resultsTextPane.setBackground(SystemColor.text);
				scrollPane3.setViewportView(resultsTextPane);

		DataSet = new JPanel();
		DataSet.setBounds(154, 114, 770, 327);
		frmClusteringAlgorithms.getContentPane().add(DataSet);
		DataSet.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Data", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		panel.setBounds(10, 9, 451, 53);
		DataSet.add(panel);
		panel.setLayout(null);
		
		JButton btnDataset = new JButton("Select Dataset");
		btnDataset.setBounds(6, 19, 117, 23);
		panel.add(btnDataset);
		
		txtPathOfDataset = new JTextField();
		txtPathOfDataset.setBounds(124, 20, 321, 20);
		txtPathOfDataset.setEditable(false);
		txtPathOfDataset.setColumns(10);
		panel.add(txtPathOfDataset);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Attributes", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(10, 68, 451, 257);
		DataSet.add(panel_1);
		panel_1.setLayout(null);
		
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new TitledBorder(null, "Available", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPane.setBounds(10, 22, 155, 224);
		panel_1.add(scrollPane);
		//private JList availble,selected;
		availble = new JList<String>(attr);
		availble.setBorder(null);
		availble.setBackground(SystemColor.menu);
		availble.setBounds(10, 22, 155, 224);
		//panel_1.add(list_2);
		scrollPane.setViewportView(availble);
		availble.setBackground(SystemColor.menu);
		availble.setVisibleRowCount(20);
		
		
		JScrollPane scrollPane1 = new JScrollPane();
		scrollPane1.setViewportBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Selected", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		scrollPane1.setBounds(232, 22, 155, 224);
		panel_1.add(scrollPane1);
		selected = new JList();
		selected.setBorder(null);
		selected.setModel(new DefaultListModel());
		selected.setBackground(SystemColor.menu);
		selected.setBounds(232, 22, 155, 224);
		scrollPane1.setViewportView(selected);
		panel_1.add(scrollPane1);
		
		JButton btnNewButton = new JButton(">>");
		btnNewButton.setBounds(170, 69, 55, 23);
		panel_1.add(btnNewButton);
		
		JButton button_1 = new JButton("<<");
		button_1.setBounds(170, 163, 55, 23);
		panel_1.add(button_1);
		
		JPanel optionsPanel = new JPanel();

		optionsPanel.setBounds(465, 11, 293, 202);


		DataSet.add(optionsPanel);
		optionsPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Options", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		optionsPanel.setLayout(null);
		
		JLabel lblTrainingPercent = new JLabel("Training Percent");
		lblTrainingPercent.setHorizontalAlignment(SwingConstants.CENTER);
		lblTrainingPercent.setBounds(12, 26, 110, 14);
		optionsPanel.add(lblTrainingPercent);
		
		JLabel lblTestingPercent = new JLabel("Testing Percent");
		lblTestingPercent.setHorizontalAlignment(SwingConstants.CENTER);
		lblTestingPercent.setBounds(134, 26, 110, 14);
		optionsPanel.add(lblTestingPercent);
		
		JLabel lblDataSplitMethod = new JLabel("Data Split Method");
		lblDataSplitMethod.setBounds(10, 75, 112, 14);
		optionsPanel.add(lblDataSplitMethod);
		
		dataSplitMethod = new JComboBox<SplitMethod>();
		dataSplitMethod.setModel(new DefaultComboBoxModel<SplitMethod>(SplitMethod.values()));
		dataSplitMethod.setBounds(10, 92, 112, 20);
		optionsPanel.add(dataSplitMethod);
		
		trainingPercentField = new JFormattedTextField(percentFormatter);
		trainingPercentField.getDocument().putProperty("number", "Text Area");
		trainingPercentField.setHorizontalAlignment(SwingConstants.CENTER);
		trainingPercentField.setValue(75);
		trainingPercentField.setBounds(10, 44, 112, 23);
		optionsPanel.add(trainingPercentField);
		
		JFormattedTextField testingPercentField = new JFormattedTextField(percentFormatter);
		testingPercentField.setHorizontalAlignment(SwingConstants.CENTER);
		testingPercentField.setValue(25);
		testingPercentField.setEditable(false);
		testingPercentField.setBounds(132, 44, 112, 23);
		optionsPanel.add(testingPercentField);
		
		chckbxNormalizeData = new JCheckBox("Normalize Data");
		chckbxNormalizeData.setBounds(132, 91, 130, 23);
		optionsPanel.add(chckbxNormalizeData);
		

		comboBoxSmoothMethod = new JComboBox<SmoothMethod>();
		comboBoxSmoothMethod.setModel(new DefaultComboBoxModel<SmoothMethod>(SmoothMethod.values()));
		comboBoxSmoothMethod.setBounds(169, 133, 112, 20);
		optionsPanel.add(comboBoxSmoothMethod);
		
		textFieldNumBins = new JFormattedTextField(clusterFormatter);
		textFieldNumBins.setHorizontalAlignment(SwingConstants.CENTER);
		textFieldNumBins.setBounds(97, 133, 43, 20);
		optionsPanel.add(textFieldNumBins);
		textFieldNumBins.setColumns(10);
		
		chckbxSmooth = new JCheckBox("Smooth");
		chckbxSmooth.setBounds(10, 132, 78, 23);
		optionsPanel.add(chckbxSmooth);
		
		JLabel lblBins = new JLabel("# Bins");
		lblBins.setBounds(97, 119, 61, 14);
		optionsPanel.add(lblBins);
		
		JLabel lblMethod = new JLabel("Method");
		lblMethod.setBounds(202, 119, 46, 14);
		optionsPanel.add(lblMethod);
		

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
		btnNewButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				DefaultListModel model = (DefaultListModel) availble.getModel();
				DefaultListModel m2 = (DefaultListModel) selected.getModel();
				
				int selectedIndex = availble.getSelectedIndex();
				if (selectedIndex != -1) {
					String s = model.getElementAt(selectedIndex).toString();
				    model.remove(selectedIndex);
				    m2.addElement(s);
				    
				}
				
			}
			
		});
		button_1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				DefaultListModel model = (DefaultListModel) selected.getModel();
				DefaultListModel m2 = (DefaultListModel) availble.getModel();
				
				int selectedIndex = selected.getSelectedIndex();
				if (selectedIndex != -1) {
					String s = model.getElementAt(selectedIndex).toString();
				    model.remove(selectedIndex);
				    m2.addElement(s);
				    
				}
				
			}
			
		});
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
		testingPanel = new TestingPanel();
		testingPanel.setBounds(154, 114, 739, 370);
		frmClusteringAlgorithms.getContentPane().add(testingPanel);
		testingPanel.setVisible(false);

		Algorthim.setBounds(154, 114, 337, 164);
		frmClusteringAlgorithms.getContentPane().add(Algorthim);
		Algorthim.setLayout(null);
		
		JLabel lblFuzzynessFactor = new JLabel("Fuzzyness Factor:");
		lblFuzzynessFactor.setVisible(false);
		lblFuzzynessFactor.setBounds(29, 89, 112, 16);
		Algorthim.add(lblFuzzynessFactor);
		
		algorithmComboBox = new JComboBox<Algorithm>();
		algorithmComboBox.setModel(new DefaultComboBoxModel<Algorithm>(Algorithm.values()));
		algorithmComboBox.setBounds(29, 29, 112, 20);
		
				Algorthim.add(algorithmComboBox);
				clusterField = new JFormattedTextField(clusterFormatter);
				clusterField.setText("1");
				clusterField.setValue(1);
				clusterField.setHorizontalAlignment(SwingConstants.CENTER);
				clusterField.setBounds(151, 29, 140, 20);
				Algorthim.add(clusterField);
				
				JLabel label = new JLabel("Number of Clusters");
				label.setBounds(159, 12, 132, 14);
				Algorthim.add(label);
				
				JLabel label_1 = new JLabel("Algorithm");
				label_1.setBounds(63, 12, 60, 14);
				Algorthim.add(label_1);
				
				JLabel lblMinimunStoppingDistance = new JLabel("Minimun Stopping Distance:");
				lblMinimunStoppingDistance.setBounds(29, 61, 167, 16);
				Algorthim.add(lblMinimunStoppingDistance);
				
				textField_1 = new JTextField();
				textField_1.setHorizontalAlignment(SwingConstants.CENTER);
				textField_1.setText("0");
				textField_1.setBounds(193, 59, 114, 20);
				Algorthim.add(textField_1);
				textField_1.setColumns(10);
				
				algorithmComboBox.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent arg0) {
						if((Algorithm)algorithmComboBox.getSelectedItem()==Algorithm.FuzzyLogic)
						{
							lblFuzzynessFactor.setVisible(true);
							textField_2.setVisible(true);
						}
						else
						{
							lblFuzzynessFactor.setVisible(false);
							textField_2.setVisible(false);
						}
						
						if((Algorithm)algorithmComboBox.getSelectedItem()==Algorithm.Hierarchical)
						{
							lblMinimunStoppingDistance.setVisible(false);
							textField_1.setVisible(false);
						}
						else
						{
							lblMinimunStoppingDistance.setVisible(true);
							textField_1.setVisible(true);
						}
					}
					
				});
				
				textField_2 = new JTextField();
				textField_2.setHorizontalAlignment(SwingConstants.CENTER);
				textField_2.setText("2");
				textField_2.setVisible(false);
				textField_2.setBounds(141, 87, 114, 20);
				Algorthim.add(textField_2);
				textField_2.setColumns(10);
				
				JButton btnAddToQueue = new JButton("Add to Queue");
				btnAddToQueue.setBounds(29, 122, 112, 26);
				Algorthim.add(btnAddToQueue);
				btnAddToQueue.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					
					System.gc();
					DataModel m = new DataModel(txtPathOfDataset.getText());
					
						ArrayList<String> attributes = new ArrayList<String>();
						ArrayList<String>unused = new ArrayList<String>();
						
						if(selected.getModel().getSize()==0)
						{
							MessageBox.show("Please select an attribute", "Error");
							return;
						}
						for(int i = 0;i<selected.getModel().getSize();i++)
							attributes.add(selected.getModel().getElementAt(i).toString());
						
						for(int i=0;i<availble.getModel().getSize();i++)
						{
							unused.add(availble.getModel().getElementAt(i).toString());
						}
						m.SetAttributes(attributes,unused);
						
						int trainingPercent = (int) trainingPercentField.getValue();
						int numberOfClusters = (int) clusterField.getValue();
						SplitMethod splitMethod = dataSplitMethod.getItemAt(dataSplitMethod.getSelectedIndex());
						m.SetSplitMethod(splitMethod, trainingPercent);
						
						Algorithm algorithmType = algorithmComboBox.getItemAt(algorithmComboBox.getSelectedIndex());
					
					try 
					{
						//dataModel.GetDataFromExcel(splitMethod, trainingPercent);
						
						// Check normalize.
						if (chckbxNormalizeData.isSelected())
						{
							m.SetNormalized(true);
						}
						else
						{
							m.SetNormalized(false);
						}
						
						// Check smoothing.
						if (chckbxSmooth.isSelected())
						{
							m.EnableSmoothing(Integer.parseInt(textFieldNumBins.getText()), comboBoxSmoothMethod.getItemAt(comboBoxSmoothMethod.getSelectedIndex()));
						}
						else
						{
							m.DisableSmoothing();
						}
						
						if (attributes.size() > 1) 
						{		
							switch (algorithmType) 
							{
								case FuzzyLogic:
									algorithm = new FuzzyClustering();
									((FuzzyClustering)algorithm).SetStoppingDistance(Double.parseDouble(textField_1.getText()));
									((FuzzyClustering)algorithm).SetFuzzyFactor(Double.parseDouble(textField_2.getText()));
									break;
	
								case Hierarchical:
									algorithm = new Hierarchical();
									break;
	
								case K_Means:
									algorithm = new K_Means();
									((K_Means)algorithm).SetStoppingDistance(Double.parseDouble(textField_1.getText()));
									break;
	
								default:
									break;
							}
							
							
							algorithm.Set(m, numberOfClusters,passable);
							((DefaultListModel)list_1.getModel()).addElement(algorithm);
						} 
						else 
						{
							MessageBox.show("You need to select at least two attribute.", "No attributes selected.");
						} 
					}
					catch (Exception e) 
					{
						MessageBox.show("Error: " + e.getMessage(), "ERROR");
					}

				}});
		btnX.setFont(new Font("Dialog", Font.PLAIN, 12));
		btnX.setBounds(783, 82, 110, 20);
		frmClusteringAlgorithms.getContentPane().add(btnX);
		Graph.setBounds(154, 114, 770, 370);
		frmClusteringAlgorithms.getContentPane().add(Graph);
		Graph.setLayout(null);
		
		graphPanel = new JPanel();
		graphPanel.setBounds(0, 0, 727, 314);
		graphPanel.setBorder(null);
		graphPanel.setLayout(null);
		ChartPanel temp = plot.DrawChart();
		temp.setBounds(0, 0, 727, 314);
		graphPanel.add(temp);
		Graph.add(graphPanel);
		
		
		JPanel panel_2 = new JPanel();
		panel_2.setLayout(null);
		panel_2.setBorder(new TitledBorder(null, "Graph Properties", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		panel_2.setBounds(0, 317, 505, 53);
		Graph.add(panel_2);
		
		xComboBox = new JComboBox<String>();
		xComboBox.setBounds(54, 22, 124, 20);
		panel_2.add(xComboBox);
		xComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(algorithm==null && f!=null)
				{
					RefreshPlot();
				}
				else if(algorithm!=null)
				{
					if(algorithm.GetState()!=State.Initializing && algorithm.GetState()!=State.Idle)
					{
						if(!algorithm.GetDataModel().GetTrainingSet().GetIsPlotting())
						{
							if(xComboBox.getItemCount()>0 && yComboBox.getItemCount()>0)
								RefreshPlot();
						}
					}
				}
				
			}
			
		});
		
		JLabel label_2 = new JLabel("X Value:");
		label_2.setBounds(10, 25, 46, 14);
		panel_2.add(label_2);
		
		JLabel label_3 = new JLabel("Y Value:");
		label_3.setBounds(188, 25, 46, 14);
		panel_2.add(label_3);
		
		yComboBox = new JComboBox<String>();
		yComboBox.setBounds(234, 22, 124, 20);
		panel_2.add(yComboBox);
		
		yComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(algorithm==null && f!=null)
				{
					RefreshPlot();
				}
				else if(algorithm!=null)
				{
					if(algorithm.GetState()!=State.Initializing && algorithm.GetState()!=State.Idle)
					{
						if(!algorithm.GetDataModel().GetTrainingSet().GetIsPlotting())
						{
							if(xComboBox.getItemCount()>0 && yComboBox.getItemCount()>0)
								RefreshPlot();
						}
					}
				}
			}
			
		});
		
		clusterFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
		clusterFormatter.setValueClass(Integer.class);
		clusterFormatter.setAllowsInvalid(false);
		clusterFormatter.setMinimum(1);
		clusterFormatter.setMaximum(Integer.MAX_VALUE);
		
		frmClusteringAlgorithms.getContentPane().add(tree);
		
		JPanel image = new JPanel();
		image.setBackground(SystemColor.menu);
		JLabel imgLabel = new JLabel(new ImageIcon("src\\gui\\logo.png"));
		imgLabel.setBackground(Color.LIGHT_GRAY);
		image.add(imgLabel);
		image.setBounds(0, 0, 440, 95);
		frmClusteringAlgorithms.getContentPane().add(image);
		
		JList list = new JList();
		list.setBounds(909, 79, -236, -67);
		frmClusteringAlgorithms.getContentPane().add(list);
		
		
		JScrollPane scrollPanel4 = new JScrollPane();
		//scrollPane14.setViewportBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Selected", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		scrollPanel4.setBounds(507, 11, 386, 68);
		frmClusteringAlgorithms.getContentPane().add(scrollPanel4);
		
		list_1 = new JList();
		list_1.setModel(new DefaultListModel());
		list_1.setBackground(SystemColor.text);
		list_1.setBorder(new TitledBorder(null, "Queue", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		list_1.setBounds(507, 11, 386, 68);
		scrollPanel4.setViewportView(list_1);
		
		JSeparator separator = new JSeparator();
		separator.setForeground(Color.BLACK);
		separator.setBackground(Color.BLACK);
		separator.setBounds(0, 106, 1069, 2);
		frmClusteringAlgorithms.getContentPane().add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setBounds(142, 108, 10, 376);
		frmClusteringAlgorithms.getContentPane().add(separator_1);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBackground(Color.BLACK);
		separator_2.setForeground(Color.BLACK);
		separator_2.setBounds(0, 485, 1069, 36);
		frmClusteringAlgorithms.getContentPane().add(separator_2);
		
		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(((DefaultListModel)list_1.getModel()).getSize()==0)
				{
					MessageBox.show("Queue Empty", "Error");
					return;
				}
				algorithm = (I_Algorithm)((DefaultListModel)list_1.getModel()).remove(0);
				xComboBox.removeAllItems();
				yComboBox.removeAllItems();
				background = new Thread(algorithm);
				background.start();
				resultsTextPane.setText("");
			}
		});
		btnStart.setBounds(689, 508, 98, 26);
		frmClusteringAlgorithms.getContentPane().add(btnStart);
		
		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (algorithm != null)
				{
					algorithm.Stop();
					try {
						background.join();
						MessageBox.show("Algorithm aborted.", "Algorithm Aborted");
						algorithm = null;
						background = null;
					} 
					catch (InterruptedException ex) {ex.printStackTrace();}	
				}				

			}
		});
		btnStop.setBounds(783, 508, 98, 26);
		frmClusteringAlgorithms.getContentPane().add(btnStop);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "Status", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		panel_3.setBounds(526, 493, 129, 53);
		frmClusteringAlgorithms.getContentPane().add(panel_3);
		panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		label_4 = new JLabel(State.Idle.toString());
		label_4.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel_3.add(label_4);
		
		JMenuBar menuBar = new JMenuBar();
		frmClusteringAlgorithms.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.setAction(action);
		mnFile.add(mntmOpen);
		tree.expandRow(0);
	}
	
	private void LoadDataModel()
	{
		try 
		{
			this.dataModel = new DataModel(txtPathOfDataset.getText());
			this.dataModel.GetAttributesFromExcel();
			//Cluster.SetAttributeNames(dataModel.GetAllAttributes());
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
			((DefaultListModel)this.selected.getModel()).removeAllElements();
			for (String s : this.dataModel.GetAllAttributes())
			{
				this.attr.addElement(s);
			}
		}
	}
	public void SetAlgorithmFinished()
	{
	//	this.lblStatus.setText("Ready");
		Results s = new Results();
		s.alg = this.algorithm.GetType();
		s.clusters=new Cluster[this.algorithm.CurrentSolution().size()];
		s.clusters=this.algorithm.CurrentSolution().toArray(s.clusters);
		s.dataFileName=this.dataModel.GetExcelFileName();
		int clusterNum=1;
		s.dataModel = this.algorithm.GetDataModel();
		s.output="";
		for (Cluster c : s.clusters)
		{
			s.output+="Cluster " + clusterNum + "\n" + c.ClusterStats()+"\n";
			s.output+="Gini = " + c.CaclGiniIndex() + "\n";
			this.resultsTextPane.insert("Cluster " + clusterNum + " Gini=" + c.CaclGiniIndex() + "\n" + c.ClusterStats() + "\n\n", 0);
			clusterNum++;
		}
		s.Serialize();
	}
	public void CurrentSolution(ArrayList<Cluster> clusters)
	{
		int clusterNum = 1;
		f=null;
		//this.dataModel.GetTrainingSet().SetIsPlotting(true);	
		// Update graph.
		if(this.xComboBox.getItemCount()==0||this.yComboBox.getItemCount()==0)
		{
			for (String s : this.algorithm.GetDataModel().GetUsedAttributes())
			{
				xComboBox.addItem(s);
				yComboBox.addItem(s);
			}
			
			if (yComboBox.getItemCount() > 1)
			{
				yComboBox.setSelectedIndex(1);
			}
		}
		this.plot=new ScatterPlotEmbedded();
		this.plot.SetXY(this.xComboBox.getItemAt(this.xComboBox.getSelectedIndex()), this.yComboBox.getItemAt(this.yComboBox.getSelectedIndex()) );
		this.graphPanel.removeAll();
		ChartPanel temp = plot.DrawChart(clusters);
		temp.setBounds(0, 0, 727, 314);
		this.graphPanel.add(temp);		
		this.graphPanel.revalidate();
		
		// Update output
		for (Cluster c : clusters)
		{
			this.resultsTextPane.insert("Cluster " + clusterNum + " Gini=" + c.CaclGiniIndex() + "\n" + c.ClusterStats() + "\n\n", 0);
			clusterNum++;
		}
		this.resultsTextPane.insert("______________________________________\n", 0);
		
		this.algorithm.GetDataModel().GetTrainingSet().SetIsPlotting(false);
	}
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "Open");
			putValue(SHORT_DESCRIPTION, "Open Serialized Result File");
		}
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			File selectedFile = null;
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
			
			int result = fileChooser.showOpenDialog(null);
			
			if (result == JFileChooser.APPROVE_OPTION)
			{
				f=null;
				selectedFile = fileChooser.getSelectedFile();
				Results s = struct.Results.Deserialize(selectedFile.getAbsolutePath());
				//f=s;
				testingPanel.SetResult(s);
				resultsTextPane.setText(s.output);
				Algorithm a = s.alg;//.GetType();
				 algorithmComboBox.setSelectedItem(a);
				
				 if(a==Algorithm.FuzzyLogic)
				 {
					 textField_1.setText(((Double)s.stoppingDistance).toString());
					 textField_2.setText(((Double)s.fuzzyFactor).toString());
					 
				 }
				else
				 {
					 if(a==Algorithm.K_Means)
					{
						
						 textField_1.setText(((Double)s.stoppingDistance).toString());
					 }
				 }
				 txtPathOfDataset.setText(s.dataModel.GetExcelPath());
				 
				 if (s.dataModel != null)
				{
						attr.clear();
						((DefaultListModel)selected.getModel()).removeAllElements();
						for (String sz : s.dataModel.GetUsedAttributes())
						{
							((DefaultListModel)selected.getModel()).addElement(sz);
						}
						
						for (String sz : s.dataModel.GetAttributesNotUsed())
						{
							attr.addElement(sz);
						}
						dataSplitMethod.setSelectedItem(s.dataModel.GetSplitMethod());
						
						trainingPercentField.setText(((Integer)s.dataModel.GetSplitPercent()).toString());
						clusterField.setText(((Integer)s.desiredClusters).toString());
						chckbxNormalizeData.setSelected(s.dataModel.GetNormalized());
				}
				 LoadGraphsAndResults(s);
			}
			/*

				
				if (attributes.size() > 1) 
				{		
					switch (algorithmType) 
					{
	
						case Hierarchical:
							algorithm = new Hierarchical();
							break;
	
						case K_Means:
							algorithm = new K_Means();
							((K_Means)algorithm).SetStoppingDistance(Double.parseDouble(textField_1.getText()));
							break;
	
						default:
							break;
					}
					
					
					algorithm.Set(m, numberOfClusters,passable);
					((DefaultListModel)list_1.getModel()).addElement(algorithm);
				} */
			
			
		}
	}
	private void LoadGraphsAndResults(Results s)
	{
			
			testingPanel.SetResult(s);
			xComboBox.removeAllItems();
			yComboBox.removeAllItems();
			for (String sz : s.dataModel.GetUsedAttributes())
			{
				xComboBox.addItem(sz);
				yComboBox.addItem(sz);
			}
			this.resultsTextPane.setText(s.output);
			yComboBox.setSelectedIndex(1);
			f=s;
			RefreshPlot();
	}
	private void RefreshPlot()
	{
		boolean graphPanelWasOpen = Graph.isVisible();
		
		ArrayList<Cluster> c;
		if(algorithm!=null)
			c= this.algorithm.CurrentSolution();
		else
		{
			if(f!=null)
				c= new ArrayList<Cluster>(Arrays.asList(f.clusters));
			else
				return;
		}
		this.plot.SetXY(this.xComboBox.getItemAt(this.xComboBox.getSelectedIndex()), this.yComboBox.getItemAt(this.yComboBox.getSelectedIndex()) );
		this.graphPanel.removeAll();	
		ChartPanel temp = plot.DrawChart(c);
		temp.setBounds(0, 0, 727, 314);
		this.graphPanel.add(temp);	
		this.graphPanel.revalidate();
		Graph.setVisible(false);
		Graph.revalidate();
		Graph.setVisible(graphPanelWasOpen);
		Graph.revalidate();
	}
}
