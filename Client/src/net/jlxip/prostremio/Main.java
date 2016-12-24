package net.jlxip.prostremio;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

public class Main {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		SystemTray systemTray = SystemTray.getSystemTray();
		Image image = Toolkit.getDefaultToolkit().getImage("src/net/jlxip/prostremio/icon.png");
		PopupMenu trayPopupMenu = new PopupMenu();
		
		MenuItem exit = new MenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		trayPopupMenu.add(exit);
		
		TrayIcon trayIcon = new TrayIcon(image, "PROStremio", trayPopupMenu);
		trayIcon.setImageAutoSize(true);
		
		try {
			systemTray.add(trayIcon);
		} catch(AWTException awte) {
			awte.printStackTrace();
		}
		
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(6371);
		} catch(IOException ioe) {
			JOptionPane.showMessageDialog(null, "The port is already in use.");
			System.exit(1);
		}
		
		while(true) {
			try {
				Socket s = ss.accept();
				new TorrentsList(s);				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
