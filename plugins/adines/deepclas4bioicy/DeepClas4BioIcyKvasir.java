package plugins.adines.deepclas4bioicy;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import icy.gui.dialog.ActionDialog;
import icy.plugin.abstract_.PluginActionable;

public class DeepClas4BioIcyKvasir extends PluginActionable {

	private String pathImages;
	
	private ActionDialog adAPI2;
	private JDialog dMain=null;

	@Override
	public void run() {


		try {	
			JFileChooser pathAPIFileChooser2=new JFileChooser();
			pathAPIFileChooser2.setCurrentDirectory(new java.io.File("."));
			pathAPIFileChooser2.setDialogTitle("Select the path of the images");
			pathAPIFileChooser2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			GridLayout glAPI2 = new GridLayout(2, 2);
			JPanel apiPanel2 = new JPanel(glAPI2);

			JLabel lPath2 = new JLabel();
			JButton bPath2=new JButton("Select");
			apiPanel2.add(new JLabel("Select the path of the images"));
			apiPanel2.add(new JLabel());
			apiPanel2.add(lPath2);
			apiPanel2.add(bPath2);
			
			bPath2.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(pathAPIFileChooser2.showOpenDialog(apiPanel2)==JFileChooser.APPROVE_OPTION) {
						lPath2.setText(pathAPIFileChooser2.getSelectedFile().getAbsolutePath());
					}
					
				}
			});

			adAPI2 = new ActionDialog("Path images", apiPanel2);
			adAPI2.pack();
			adAPI2.setVisible(true);
			if (adAPI2.isCanceled()) {
				return;
			}

			
			pathImages=lPath2.getText();

			File folder=new File(pathImages);
			File images[]=folder.listFiles();

			
			JSONObject json = new JSONObject();
			json.put("framework", "PyTorch");
			json.put("model", "ResNet34Kvasir");
			JSONArray array = new JSONArray();
			for(File s:images)
			{
				array.add(s.getAbsolutePath());
			}
			json.put("images", array);
			
			try (FileWriter file = new FileWriter("data.json")) {
				file.write(json.toJSONString());
				
			}
			
			File f=new File("data.json");
			
			
			String comando ="deepclas4bio-predictBatch " + f.getAbsolutePath();
			Process p = Runtime.getRuntime().exec(comando);
			p.waitFor();

			
			JTable table;
            DefaultTableModel tableModel = new DefaultTableModel();
            tableModel.addColumn("Image");
    		tableModel.addColumn("Class");
            String names[]= {"Image","Class"};
            Object row[] = new Object[2];
			
			
			JSONParser parser3 = new JSONParser();
			JSONObject jsonObject3 = (JSONObject) parser3.parse(new FileReader("data.json"));
			JSONArray results=(JSONArray)jsonObject3.get("results");
			for(int i=0; i<results.size();i++)
			{
				JSONObject image=(JSONObject)results.get(i);
				row[0]=(String)image.get("image");
				row[1]=(String)image.get("class");
				tableModel.addRow(row);
			}
			
			String filas[][] = new String[tableModel.getRowCount()][2];
            for (int j = 0; j < tableModel.getRowCount(); j++) {
                for (int k = 0; k < 2; k++) {
                    filas[j][k] = (String) tableModel.getValueAt(j, k);
                }
            }
            table = new JTable(filas, names);
            
            JScrollPane result = new JScrollPane(table);

            JOptionPane jresult = new JOptionPane(result, JOptionPane.PLAIN_MESSAGE, JOptionPane.CANCEL_OPTION);
            dMain = jresult.createDialog("Result");
            dMain.setVisible(true);
            dMain.dispose();

		} catch (FileNotFoundException ex) {
			Logger.getLogger(DeepClas4BioIcyKvasir.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(DeepClas4BioIcyKvasir.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InterruptedException ex) {
			Logger.getLogger(DeepClas4BioIcyKvasir.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ParseException ex) {
			Logger.getLogger(DeepClas4BioIcyKvasir.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

}
