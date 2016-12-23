package net.jlxip.prostremio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.regex.Pattern;

public class Callback {
	public static void run(Socket socket, String query, List<String> torrent) {
		try {
			OutputStream os = socket.getOutputStream();
			
			int MovieOrSeries = GetData.MovieOrSeries(query);
			
			String hash = torrent.get(1);
			System.out.println(hash);
			String seeds = torrent.get(2);
			
			Pattern Pspace = Pattern.compile(Pattern.quote(" "));
			
			int mapidx = 0;	// For movies :)
			if(MovieOrSeries == 1) {
				mapidx = GetData.getIdx(Pspace.split(query)[Pspace.split(query).length-1], hash); 
			}
			System.out.println("MAPIDX: "+mapidx);
			
			
			String quality = GetData.getQuality(torrent.get(0));
			
			String trackers = GetData.getTrackers(hash);
			
			String toReturn = MovieOrSeries+"|"+hash+"|"+mapidx+"|"+quality+"@"+seeds+"|"+trackers;
			
			os.write(toReturn.getBytes());
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
