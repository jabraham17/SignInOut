package SignInOutData;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

//window that shows
public class GUI extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	//init everything
	public GUI()
	{
		//init pref
		pref = Preferences.userNodeForPackage(getClass());
		
		//get control panel
		JPanel control = controlPanel();
		
		Object[] colNames = {"Last", "First", "ID", "Time Spent"};
		model = new DefaultTableModel(colNames, 0);
		table = new JTable(model) {
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column) {                
                return false;               
        };
		};
		JScrollPane scroll = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		
		//setup frame
		setSize(600, 600);
		setTitle("Sign In/Out Data");
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int)((dimension.getWidth() - getWidth()) / 2);
		int y = (int)((dimension.getHeight() - getHeight()) / 2);
		setLocation(x, y);
		
		
		//add separator line
		getContentPane().add(control, BorderLayout.WEST);
		getContentPane().add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.CENTER);
		getContentPane().add(scroll, BorderLayout.EAST);
		
	}
	//create control panel with buttons
	public JPanel controlPanel()
	{
		//create jpanel to hold all of this, vertical box layout
		JPanel control = new JPanel();
		control.setLayout(new BoxLayout(control, BoxLayout.Y_AXIS));
		//create choose file button
		choose = new JButton("Choose File");
		
		//create file name label
		fileLabel = new JLabel("File Name");
		fileLabel.setEnabled(false);
		
		//create export button
		export = new JButton("Export");
		export.setEnabled(false);
		
		addListenersToButtons();
		
		//add buttons to panel
		control.add(choose);
		control.add(fileLabel);
		control.add(export);
		
		return control;
	}
	//add listeners to buttons
	public void addListenersToButtons() 
	{
		//when choose is clicked on, open file chooser, ungrayout export, set label to filename
		class ChooseListener implements ActionListener
		{
			public void actionPerformed(ActionEvent event)
			{
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Document", "xlsx");
				chooser.setFileFilter(filter);
				chooser.setCurrentDirectory(new File(pref.get("LAST_DIR", "")));
				if(chooser.showOpenDialog(GUI.this) == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					//set pref
					pref.put("LAST_DIR", file.getParent());
					if(!(file.toString().endsWith(".xlsx")))
					{
						String filetype = chooser.getFileFilter().toString();
						if(filetype.endsWith("[xlsx]]"))
							file = new File(file + ".xlsx");
					}
					fileLabel.setText(file.getName());
					fileLabel.setEnabled(true);
					export.setEnabled(true);
					try
					{
						populateTable(file);
					}
					catch(Exception e){e.printStackTrace();}
				}
			}
		}
		choose.addActionListener(new ChooseListener());
		
		//when export is clicked on export as excel
		class ExportListener implements ActionListener
		{
			public void actionPerformed(ActionEvent event)
			{
				
			}
		}
		export.addActionListener(new ExportListener());
	}
	//populates table with data
	public void populateTable(File file) throws Exception 
	{
		FileInputStream in = new FileInputStream(file);
		XSSFWorkbook workbook = new XSSFWorkbook(in);
		XSSFSheet sheet = workbook.getSheetAt(0);

		ArrayList<Person> persons = new ArrayList<Person>();
		Iterator<Row> rowIterator = sheet.iterator();
		while(rowIterator.hasNext())
		{
			Row row = rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();

			while(cellIterator.hasNext())
			{
				ArrayList<Object> newData = new ArrayList<Object>();
				for(int i = 0; i < 6; i++)
				{
					Cell cell = cellIterator.next();
					cell.setCellType(Cell.CELL_TYPE_STRING);
					newData.add(cell.getStringCellValue());
				}
				Person newPerson = new Person();
				newPerson.firstName = (String) newData.get(0);
				newPerson.lastName = (String) newData.get(1);
				newPerson.studentID =  newData.get(2) + "";
				
				String start = newData.get(3) + " " + newData.get(4) + ":00";
				String end = newData.get(3) + " " + newData.get(5) + ":00";
				
				SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				
				Date startTime = format.parse(start);
				Date endTime = format.parse(end);

				long time = endTime.getTime() - startTime.getTime();
				
				int k = Collections.binarySearch(persons, newPerson);
				if(k >= 0)
				{
					Person p = persons.get(k);
					p.timeSpent += time;
					persons.set(k, p);
				}
				else
				{
					newPerson.timeSpent = time;
					persons.add((-(k) - 1), newPerson);
				}
			}
		}
		workbook.close();
		in.close();
		
		for(int i = model.getRowCount() - 1; i >= 0; i--)
		{
		    model.removeRow(i);
		}
		
		for(Person p : persons)
		{
			
			//formatted time
			double hrs = p.timeSpent / 3600000;
			String fmtTime = hrs + "hrs";
			
			Object[] newRowData = {p.lastName, p.firstName, p.studentID, fmtTime};
			
		    model.addRow(newRowData);
		}
		model.fireTableDataChanged();
	}
	private JButton choose;
	private JLabel fileLabel;
	private JButton export;
	private JTable table;
	private DefaultTableModel model;
	private Preferences pref;
}
