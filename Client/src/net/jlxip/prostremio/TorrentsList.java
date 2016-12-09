package net.jlxip.prostremio;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
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
		
		ArrayList<List<String>> torrents = GetTorrents.get(recv);
		class TorrentsTableModel extends DefaultTableModel {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		}
		TorrentsTableModel model = new TorrentsTableModel();
		model.addColumn("Name");
		model.addColumn("Seeds");
		for(int i=0;i<torrents.size();i++) {
			model.addRow(new String[]{torrents.get(i).get(0), torrents.get(i).get(2)});
		}
		JTable tabla = new JTable(model);
		tabla.getTableHeader().setReorderingAllowed(false);
		JScrollPane scrollpane = new JScrollPane(tabla);
		scrollpane.setBounds(8, 8, 640, 480);
		add(scrollpane);
		
		setSize(1280, 720);
		setLocationRelativeTo(null);
		setVisible(true);
		toFront();
	}
}
