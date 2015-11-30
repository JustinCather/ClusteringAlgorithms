package gui;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import struct.Cluster;
import struct.DataSet;
import struct.Results;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class TestingPanel extends JPanel 
{
	private Results result;
	private SwingWorker worker;
	private JTextArea textArea;
	private JProgressBar progressBar;
	private JButton btnRunTestSet;
	
	public TestingPanel()
	{
		super();
		this.setBounds(154, 114, 731, 327);
		this.setLayout(null);
		
		btnRunTestSet = new JButton("Run Test Set");
		btnRunTestSet.setBounds(10, 11, 110, 23);
		add(btnRunTestSet);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(130, 11, 591, 280);
		add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(130, 302, 591, 14);
		add(progressBar);
		
		btnRunTestSet.addActionListener(new ActionListener() 
		{
			
			public void actionPerformed(ActionEvent e) {
				worker = new SwingWorker<Double, String>() {

					@Override
					protected Double doInBackground() throws Exception 
					{
						Cluster[] temp = result.clusters;
						ArrayList<Cluster> clusters = new ArrayList<Cluster>();//(ArrayList<Cluster>)Arrays.asList(result.clusters);
						DataSet testingSet = result.dataModel.GetTestingSet();
						String selectedClass = null;
						int numCorrect = 0;
						int removed = 0;
						for(int i =0; i < temp.length;i++)
						{
							if(temp[i]!=null)
								clusters.add(temp[i]);
						}
						if (clusters.size() > 0) 
						{
							for (int i = 0; i < clusters.size(); i++) 
							{
								clusters.get(i).SetClusterType();
							}
							
							
							for (int i = 0; i < testingSet.GetDataSetSize(); i++) 
							{
								double smallestDistance = clusters.get(0).GetDistance(testingSet.GetPoint(i));
								selectedClass = clusters.get(0).GetClusterType();

								for (int j = 1; j < clusters.size(); j++) 
								{
									double tempDist = clusters.get(j).GetDistance(testingSet.GetPoint(i));

									if (tempDist < smallestDistance) 
									{
										smallestDistance = tempDist;
										selectedClass = clusters.get(j).GetClusterType();
									}
								}
								
								if (testingSet.GetPoint(i).GetType().equals(selectedClass))
									numCorrect++;
								
								publish("Point " + i + " is of class " + testingSet.GetPoint(i).GetType() + " and was assigned to " + selectedClass + ".\n");
							}
						}
									
						return numCorrect / ((double)testingSet.GetDataSetSize()) * 100;
					}
					
					@Override
					 protected void process(List<String> chunks)
					 {
						for (String s: chunks)
						{
							textArea.append(s);
							progressBar.setValue(progressBar.getValue() + 1);
						}
					 }
					
					@Override
					protected void done()
					{
						try 
						{
							double correctPercent = get();
							double incorrectPercent = 100.0 - correctPercent;
							DecimalFormat df = new DecimalFormat("##.##");
							
							textArea.append("\nCorrect Percent: " + df.format(correctPercent) + "\n" + 
											"Incorrect Percent: " + df.format(incorrectPercent) + "\n");
							
						} catch (InterruptedException | ExecutionException e) {}
						
						btnRunTestSet.setEnabled(true);	
					}

				};
				
				if (result != null) 
				{
					btnRunTestSet.setEnabled(false);
					textArea.setText("");
					progressBar.setMinimum(0);
					progressBar.setMaximum(result.dataModel.GetTestingSet().GetDataSetSize());
					progressBar.setValue(0);
					worker.execute();
				}
			
			}
		});
	}
		
	public void SetResult(Results r)
	{
		this.result = r;
		textArea.setText("");
	}
}
