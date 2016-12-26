package net.jlxip.prostremio;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class TorrentsList extends JFrame {
	private static final long serialVersionUID = 1L;
	
	final Socket socket;
	InputStream is;

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
		setLayout(null);
		setResizable(false);
		
		JLabel query = new JLabel("Query: "+recv);
		Point Pquery = new Point(8, 8);
		Dimension Dquery = query.getPreferredSize();
		Rectangle Rquery = new Rectangle(Pquery, Dquery);
		query.setBounds(Rquery);
		add(query);
		
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
		int Pscrollpane_x = 8;
		int Pscrollpane_y = query.getBounds().y + query.getBounds().height + 8;
		Point Pscrollpane = new Point(Pscrollpane_x, Pscrollpane_y);
		Dimension Dscrollpane = new Dimension(640, 480);
		Rectangle Rscrollpane = new Rectangle(Pscrollpane, Dscrollpane);
		scrollpane.setBounds(Rscrollpane);
		add(scrollpane);
		
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
		
		updateStarredTorrents(starredModel);
		
		JTable starred = new JTable(starredModel);
		starred.getTableHeader().setReorderingAllowed(false);
		starred.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(arg0.getClickCount() == 2) {
					JTable target = (JTable)arg0.getSource();
					int row = target.getSelectedRow();
					
					List<String> currentTorrent = new ArrayList<String>();
					currentTorrent.add(null);	// This is not needed ;)
					currentTorrent.add(readStarredTorrents().get(row).get(1)); // HASH
					currentTorrent.add(readStarredTorrents().get(row).get(2));	// Seeds
					
					Callback.run(socket, Frecv, readStarredTorrents().get(row));
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
		JScrollPane starredScrollpane = new JScrollPane(starred);
		int PstarredScrollpane_x = scrollpane.getBounds().x + scrollpane.getBounds().width + 8;
		int PstarredScrollpane_y = scrollpane.getBounds().y;
		Point PstarredScrollpane = new Point(PstarredScrollpane_x, PstarredScrollpane_y);
		Dimension DstarredScrollpane = new Dimension(320, 240);
		Rectangle RstarredScrollpane = new Rectangle(PstarredScrollpane, DstarredScrollpane);
		starredScrollpane.setBounds(RstarredScrollpane);
		add(starredScrollpane);
		
		JButton star = new JButton("*");
		star.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = tabla.getSelectedRow();
				if(tabla.getSelectedRowCount() == 0) {	// If no row has been selected
					return;
				}
				try {
					FileWriter FWstarred = new FileWriter(Fstarred);
					PrintWriter PWstarred = new PrintWriter(FWstarred);
					
					String toPrint = torrents.get(row).get(0)+"|"+torrents.get(row).get(1);
					PWstarred.println(toPrint);
					updateStarredTorrents(starredModel);	// TODO: This doesn't work.
					
					PWstarred.close();
					FWstarred.close();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		});
		int Pstar_x = 8;
		int Pstar_y = scrollpane.getBounds().y + scrollpane.getBounds().height + 8;
		Point Pstar = new Point(Pstar_x, Pstar_y);
		Dimension Dstar = star.getPreferredSize();
		Rectangle Rstar = new Rectangle(Pstar, Dstar);
		star.setBounds(Rstar);
		add(star);
		
		Component last_component = star;
		Component largest_component = starredScrollpane;
		int WINDOW_WIDTH = largest_component.getBounds().x + largest_component.getBounds().width + 8;
		int WINDOW_HEIGHT = last_component.getBounds().y + last_component.getBounds().height + 32; // 24?
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setLocationRelativeTo(null);
		setVisible(true);
		toFront();
	}
	
	public static void updateStarredTorrents(DefaultTableModel model) {
		ArrayList<List<String>> data = readStarredTorrents();
		
		// TODO: Find a better way of clearing the table.
		for(int i=0;i<model.getRowCount();i++) {
			model.removeRow(i);
		}
		
		for(int i=0;i<data.size();i++) {
			model.addRow(new Object[]{data.get(i).get(0), data.get(i).get(2)});
		}
	}
	
	public static ArrayList<List<String>> readStarredTorrents() {
		ArrayList<List<String>> output = new ArrayList<List<String>>();
		File Fstarred = new File("starred.dat");
		try {
			FileReader FRstarred = new FileReader(Fstarred);
			BufferedReader BRstarred = new BufferedReader(FRstarred);
			
			String line = "";
			while((line=BRstarred.readLine()) != null) {
				Pattern Pparte = Pattern.compile(Pattern.quote("|"));
				// name|info_hash
				String[] partes = Pparte.split(line);
				
				// name, info_hash, seeds
				List<String> toAdd = new ArrayList<String>();
				toAdd.add(partes[0]);
				toAdd.add(partes[1]);
				toAdd.add(GetData.getSeeds(partes[1]));
				output.add(toAdd);
			}
			
			BRstarred.close();
			FRstarred.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		return output;
	}
}
