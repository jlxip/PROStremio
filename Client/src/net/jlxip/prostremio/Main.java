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

public class Main {
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
		
		TrayIcon trayIcon = new TrayIcon(image, "SystemTray DEMO", trayPopupMenu);
		trayIcon.setImageAutoSize(true);
		
		try {
			systemTray.add(trayIcon);
		} catch(AWTException awte) {
			awte.printStackTrace();
		}
		
		while(true) {
			try {
				ServerSocket ss = new ServerSocket(6371);
				Socket s = ss.accept();
				new TorrentsList(s);
				ss.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
