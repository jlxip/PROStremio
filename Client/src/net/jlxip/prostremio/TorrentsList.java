package net.jlxip.prostremio;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class TorrentsList extends JFrame {
	private static final long serialVersionUID = 1L;
	
	final Socket socket;
	static JTable starred;
	InputStream is;
	private JTextField customInfohash;
	private JTextField customInfohashName;

	public TorrentsList(Socket socket) {
		super("PROStremio");
		
		this.socket = socket;
		
		// GET DATA
		String recv = "";
		try {
			is = socket.getInputStream();
			byte[] firstByte = new byte[1];
			is.read(firstByte);
			byte[] rest = new byte[is.available()];
			is.read(rest);
			recv = new String(firstByte) + new String(rest);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// END
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);
		setResizable(false);
		
		JLabel query = new JLabel("Query: "+recv);
		query.setBounds(new Rectangle(8, 8, 43, 16));
		getContentPane().add(query);
		
		ArrayList<List<String>> torrents = GetTorrents.get(recv);
		class TorrentsTableModel extends DefaultTableModel {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		}
		TorrentsTableModel model = new TorrentsTableModel();
		model.addColumn("Name");
		model.addColumn("Seeds");
		for(int i=0;i<torrents.size();i++) {
			model.addRow(new Object[]{torrents.get(i).get(0), torrents.get(i).get(2)});
		}
		JTable tabla = new JTable(model);
		tabla.getTableHeader().setReorderingAllowed(false);
		JScrollPane scrollpane = new JScrollPane(tabla);
		scrollpane.setBounds(new Rectangle(8, 37, 640, 480));
		getContentPane().add(scrollpane);
		
		final String Frecv = recv;
		
		TorrentsList me = this;
		tabla.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(arg0.getClickCount() == 2) {
					JTable target = (JTable)arg0.getSource();
					int row = target.getSelectedRow();
					Callback.run(socket, Frecv, torrents.get(row));
					try {
						socket.close();
					} catch(IOException ioe) {}
					me.dispose();
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseReleased(MouseEvent arg0) {}
		});
		
		TorrentsTableModel starredModel = new TorrentsTableModel();
		starredModel.addColumn("Name");
		starredModel.addColumn("Seeds");
		
		File Fstarred = new File("starred.dat");
		if(!Fstarred.exists()) {
			try {
				Fstarred.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		starred = new JTable(starredModel);
		updateStarredTorrents(Frecv);
		starred.getTableHeader().setReorderingAllowed(false);
		starred.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(arg0.getClickCount() == 2) {
					JTable target = (JTable)arg0.getSource();
					
					if(target.getSelectedRowCount() < 1) {
						return;
					}
					
					int row = target.getSelectedRow();
					
					Callback.run(socket, Frecv, readStarredTorrents(Frecv).get(row));
					try {
						socket.close();
					} catch(IOException ioe) {}
					me.dispose();
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(arg0.getButton() != MouseEvent.BUTTON3) {
					return;
				}
				
				JTable target = (JTable)arg0.getSource();
				
				if(target.getSelectedRowCount() < 1) {
					return;
				}
				
				int row = target.getSelectedRow();
				String clearQuery = getClearQuery(Frecv);
				String toRemove = clearQuery+"|"+readStarredTorrents(Frecv).get(row).get(0)+"|"+readStarredTorrents(Frecv).get(row).get(1);
				
				File Fstarred = new File("starred.dat");
				
				try {
					String toPrint = "";
					FileReader FRstarred = new FileReader(Fstarred);
					BufferedReader BRstarred = new BufferedReader(FRstarred);
					String line = "";
					while((line = BRstarred.readLine()) != null) {
						if(!line.equals(toRemove)) {
							if(line.equals("")) {
								continue;
							}
							toPrint += line + "\n";							
						}
					}
					BRstarred.close();
					FRstarred.close();
					
					
					FileWriter FWstarred = new FileWriter(Fstarred);
					PrintWriter PWstarred = new PrintWriter(FWstarred);
					
					PWstarred.println(toPrint);
					
					PWstarred.close();
					FWstarred.close();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
				
				updateStarredTorrents(Frecv);
			}
		});
		JScrollPane starredScrollpane = new JScrollPane(starred);
		starredScrollpane.setBounds(new Rectangle(660, 37, 320, 240));
		getContentPane().add(starredScrollpane);
		
		JButton star = new JButton("*");
		star.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(tabla.getSelectedRowCount() == 0) {	// If no row has been selected
					return;
				}
				
				int row = tabla.getSelectedRow();
				addToStarred(Frecv, torrents.get(row).get(0), torrents.get(row).get(1));
				
				updateStarredTorrents(Frecv);
			}
		});
		star.setBounds(new Rectangle(10, 530, 41, 25));
		getContentPane().add(star);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(Color.GRAY));
		panel.setBounds(660, 290, 317, 189);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblCustomInfohash = new JLabel("Custom infohash:");
		lblCustomInfohash.setBounds(12, 13, 100, 16);
		panel.add(lblCustomInfohash);
		
		customInfohash = new JTextField();
		customInfohash.setBounds(12, 42, 293, 28);
		panel.add(customInfohash);
		customInfohash.setColumns(10);
		
		JButton btnNewButton = new JButton("OK");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(customInfohash.getText().equals("")) {
					return;
				}
				
				List<String> selectedCustomInfohash = new ArrayList<String>();
				selectedCustomInfohash.add(GetData.getName(customInfohash.getText()));	// NAME
				selectedCustomInfohash.add(customInfohash.getText()); // HASH
				selectedCustomInfohash.add(GetData.getSeeds(customInfohash.getText()));	// Seeds
				
				Callback.run(socket, Frecv, selectedCustomInfohash);
				try {
					socket.close();
				} catch(IOException ioe) {}
				me.dispose();
			}
		});
		btnNewButton.setBounds(12, 153, 240, 25);
		panel.add(btnNewButton);
		
		JButton button = new JButton("*");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(customInfohash.getText().equals("")) {
					return;
				}
				if(customInfohashName.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "A name for this infohash is required.");
					return;
				}
				
				addToStarred(Frecv, customInfohashName.getText(), customInfohash.getText());
				customInfohash.setText("");
				customInfohashName.setText("");
				updateStarredTorrents(Frecv);
			}
		});
		button.setBounds(264, 153, 41, 25);
		panel.add(button);
		
		JLabel lblName = new JLabel("Name (only needed if starred):");
		lblName.setBounds(12, 83, 177, 16);
		panel.add(lblName);
		
		customInfohashName = new JTextField();
		customInfohashName.setBounds(12, 112, 293, 28);
		panel.add(customInfohashName);
		customInfohashName.setColumns(10);
		
		setSize(995, 595);
		setLocationRelativeTo(null);
		setVisible(true);
		toFront();
	}
	
	public static void updateStarredTorrents(String query) {
		ArrayList<List<String>> data = readStarredTorrents(query);
		
		DefaultTableModel model = (DefaultTableModel)starred.getModel();
		
		// CLEAN ROWS
		final int rowCount = model.getRowCount();
		for(int i=rowCount-1;i>=0;i--) {
			model.removeRow(i);
		}
		
		for(int i=0;i<data.size();i++) {
			model.addRow(new Object[]{data.get(i).get(0), data.get(i).get(2)});
		}
		
		starred.setModel(model);
	}
	
	public static ArrayList<List<String>> readStarredTorrents(String query) {
		ArrayList<List<String>> output = new ArrayList<List<String>>();
		File Fstarred = new File("starred.dat");
		try {
			FileReader FRstarred = new FileReader(Fstarred);
			BufferedReader BRstarred = new BufferedReader(FRstarred);
			
			String line = "";
			while((line=BRstarred.readLine()) != null) {
				if(line.equals("")) {
					continue;
				}
				
				Pattern Pparte = Pattern.compile(Pattern.quote("|"));
				// query|name|info_hash
				String[] partes = Pparte.split(line);
				
				if(!getClearQuery(partes[0]).equals(getClearQuery(query))) {	// Only show torrents linked to the query.
					continue;
				}
				
				// name, info_hash, seeds
				List<String> toAdd = new ArrayList<String>();
				toAdd.add(partes[1]);
				toAdd.add(partes[2]);
				toAdd.add(GetData.getSeeds(partes[2]));
				output.add(toAdd);
			}
			
			BRstarred.close();
			FRstarred.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		return output;
	}
	
	public static String getClearQuery(String query) {
		String clearQuery = "";
		
		if(GetData.MovieOrSeries(query) == 0) {	// Movie
			clearQuery = query;
		} else {	// Series
			Pattern Pspace = Pattern.compile(Pattern.quote(" "));
			String[] spaces = Pspace.split(query);
			for(int i=0;i<spaces.length-1;i++) {
				clearQuery += spaces[i];
				if(i != spaces.length-2) {
					clearQuery += " ";
				}
			}
		}
		
		return clearQuery;
	}
	
	public static void addToStarred(String Frecv, String originalName, String infohash) {
		File Fstarred = new File("starred.dat");
		
		try {
			String before = "";
			FileReader FRstarred = new FileReader(Fstarred);
			BufferedReader BRstarred = new BufferedReader(FRstarred);
			String line = "";
			while((line = BRstarred.readLine()) != null) {
				if(line.equals("")) {
					continue;
				}
				before += line + "\n";
			}
			BRstarred.close();
			FRstarred.close();
			
			
			FileWriter FWstarred = new FileWriter(Fstarred);
			PrintWriter PWstarred = new PrintWriter(FWstarred);
			
			String clearQuery = getClearQuery(Frecv);
			String toPrint = before+clearQuery+"|"+originalName+"|"+infohash;
			PWstarred.print(toPrint);
			
			PWstarred.close();
			FWstarred.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
