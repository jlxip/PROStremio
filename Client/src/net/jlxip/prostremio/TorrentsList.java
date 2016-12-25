package net.jlxip.prostremio;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
		model.addColumn("*");
		model.addColumn("Name");
		model.addColumn("Seeds");
		for(int i=0;i<torrents.size();i++) {
			model.addRow(new Object[]{"", torrents.get(i).get(0), torrents.get(i).get(2)});
		}
		JTable tabla = new JTable(model);
		tabla.getTableHeader().setReorderingAllowed(false);
		tabla.getColumnModel().getColumn(0).setPreferredWidth(3);	// This makes * column thiner.
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
		
		setSize(1280, 720);
		setLocationRelativeTo(null);
		setVisible(true);
		toFront();
	}
}
