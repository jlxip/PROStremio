package net.jlxip.prostremio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;

import com.hypirion.bencode.BencodeReadException;
import com.hypirion.bencode.BencodeReader;

public class GetData {
	public static String getQuality(String name) {
		Pattern P480 = Pattern.compile(Pattern.quote("480p"));
		Pattern P720 = Pattern.compile(Pattern.quote("720p"));
		Pattern P1080 = Pattern.compile(Pattern.quote("1080p"));
		Pattern P2160 = Pattern.compile(Pattern.quote("2160p"));
		Pattern PBD = Pattern.compile(Pattern.quote("BDRip"));
		Pattern PDVD = Pattern.compile(Pattern.quote("DVDRip"));
		Pattern PHD = Pattern.compile(Pattern.quote("HDRip"));
		
		if(P480.split(name).length > 1) {
			return "480p";
		} else if(P720.split(name).length > 1) {
			return "720p";
		} else if(P1080.split(name).length > 1) {
			return "1080p";
		} else if(P2160.split(name).length > 1) {
			return "2160p";
		} else if(PBD.split(name).length > 1) {
			return "BDRip";
		} else if(PDVD.split(name).length > 1) {
			return "DVDRip";
		} else if(PHD.split(name).length > 1) {
			return "HDRip";
		} else {
			return "Unknown";
		}
	}
	
	public static String getTrackers(String hash) {
		String trackers = "";
		
		String body = GetTorrents.getBody("https://torrentz2.eu/"+hash);
		
		Pattern Prawtrackers = Pattern.compile(Pattern.quote("/announce</dt>"));
		Pattern Pdldt = Pattern.compile(Pattern.quote("<dl><dt>"));
		
		String[] rawtrackers = Prawtrackers.split(body);
		ArrayList<String> realTrackers = new ArrayList<String>();
		
		for(int i=0;i<rawtrackers.length/2;i++) {
			String thistracker = "tracker:"+Pdldt.split(rawtrackers[i])[Pdldt.split(rawtrackers[i]).length-1]+"/announce";
			realTrackers.add(thistracker);
		}
		
		realTrackers.add("http://9.rarbg.com:2710/announce");
		realTrackers.add("http://announce.torrentsmd.com:6969/announce");
		realTrackers.add("http://bt.careland.com.cn:6969/announce");
		realTrackers.add("http://explodie.org:6969/announce");
		realTrackers.add("http://mgtracker.org:2710/announce");
		realTrackers.add("http://tracker.tfile.me/announce");
		realTrackers.add("http://tracker.torrenty.org:6969/announce");
		realTrackers.add("http://tracker.trackerfix.com/announce");
		realTrackers.add("http://www.mvgroup.org:2710/announce");
		realTrackers.add("udp://9.rarbg.com:2710/announce");
		realTrackers.add("udp://9.rarbg.me:2710/announce");
		realTrackers.add("udp://9.rarbg.to:2710/announce");
		realTrackers.add("udp://coppersurfer.tk:6969/announce");
		realTrackers.add("udp://exodus.desync.com:6969/announce");
		realTrackers.add("udp://glotorrents.pw:6969/announce");
		realTrackers.add("udp://open.demonii.com:1337/announce");
		realTrackers.add("udp://tracker.coppersurfer.tk:6969/announce");
		realTrackers.add("udp://tracker.glotorrents.com:6969/announce");
		realTrackers.add("udp://tracker.leechers-paradise.org:6969/announce");
		realTrackers.add("udp://tracker.openbittorrent.com:80/announce");
		realTrackers.add("udp://tracker.opentrackr.org:1337/announce");
		realTrackers.add("udp://tracker.publicbt.com:80/announce");
		realTrackers.add("udp://tracker4.piratux.com:6969/announce");
		
		
		for(int i=0;i<realTrackers.size();i++) {
			trackers += new String(Base64.getEncoder().encode(realTrackers.get(i).getBytes()));
			if(i != realTrackers.size()-1) {
				trackers += "&";
			}
		}
		
		return trackers;
	}
	
	public static int MovieOrSeries(String query) {
		int toReturn = 0;
		
		Pattern Pspace = Pattern.compile(Pattern.quote(" "));
		String lastSpace = Pspace.split(query)[Pspace.split(query).length-1];
		if(lastSpace.toLowerCase().toCharArray()[0] == 's' && lastSpace.toLowerCase().toCharArray()[3] == 'e') {
			toReturn = 1;
		}
		
		return toReturn;
	}
	
	
	
	
	
	public static ArrayList<String> getFiles(String link) {
		ArrayList<String> files = new ArrayList<String>();
		
		try {
			URL url = new URL(link);
			HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
			httpcon.addRequestProperty("User-Agent", "Mozilla/4.0");
			InputStream in = httpcon.getInputStream();
			BencodeReader br = new com.hypirion.bencode.BencodeReader(in);
			try {
				Map<String, Object> map = br.readDict();
				
				// This really could be improved, but I don't have time for breaking my balls
				// Warning: dirty code incoming
				String info = map.get("info").toString();
				Pattern Pbegfiles = Pattern.compile(Pattern.quote(", files=["));
				Pattern Pendfiles = Pattern.compile(Pattern.quote("], piece"));
				String rawfiles = Pendfiles.split(Pbegfiles.split(info)[1])[0];
				
				Pattern Popen = Pattern.compile(Pattern.quote("{path=["));
				String[] Sfiles = Popen.split(rawfiles);
				for(int i=0;i<Sfiles.length;i++) {
					Pattern Pclose = Pattern.compile(Pattern.quote("],"));
					String thisfile = Pclose.split(Sfiles[i])[0];
					files.add(thisfile);
				}
				
				br.close();
				in.close();
			} catch (BencodeReadException e) {
				e.printStackTrace();
			}
			
			new File("tmp.torrent").delete();
		} catch(IOException ioe) {
			System.out.println("No se ha podido encontrar el torrent :(");
			ioe.printStackTrace();
		}
		
		return files;
	}
	
	public static int getIdx(String last_space, String hash) {
		String url = "http://itorrents.org/torrent/"+hash+".torrent";
		ArrayList<String> getFiles = getFiles(url);
		
		Pattern Pep = Pattern.compile(Pattern.quote(last_space));
		
		for(int i=1;i<getFiles.size();i++) {	// If something starts to fail, change the start value of i to 0
			//System.out.println(getFiles.get(i));
			if(Pep.split(getFiles.get(i).toLowerCase()).length > 1) {
				System.out.println("ENCONTRADO: "+getFiles.get(i));
				// ACCEPTED EXTENSIONS
				String[] OKextensions = new String[]{"avi", "mkv", "mp4", "m4v"};
				
				String extension = getFiles.get(i).toLowerCase().substring(getFiles.get(i).length()-3, getFiles.get(i).length());
				
				for(int j=0;j<OKextensions.length;j++) {
					if(extension.equals(OKextensions[j])) {
						return i-1;	// Has to be i-1, because of something weird I did up there.
					}
				}
			}
		}
		
		return -1;	// TODO: show list of files and let the user choose.
	}
}
