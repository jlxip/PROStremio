package net.jlxip.prostremio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class GetTorrents {
	public static ArrayList<List<String>> get(String s) {
		String URL = "https://torrentz2.eu/search?f=";
		try {
			URL += URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) { 
			e.printStackTrace();
		}
		
		Pattern Pplus = Pattern.compile(Pattern.quote("+"));
		String fixed_URL = "";
		for(int i=0;i<Pplus.split(URL).length;i++) {
			fixed_URL += Pplus.split(URL)[i];
			if(i != Pplus.split(URL).length-1) {
				fixed_URL += "%20";
			}
		}
		
		ArrayList<List<String>> torrents = parseBody(getBody(fixed_URL));
		
		return torrents;
	}
	
	public static String getBody(String SURL) {
		String body = "";
		
		try {
	        URL url = new URL(SURL);
	        HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
	        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
	        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

	        String input;
	        while ((input = br.readLine()) != null){
	        	body += input + "\n";
	        }
	        br.close();
	    } catch (MalformedURLException mue) {
	         mue.printStackTrace();
	    } catch (IOException ioe) {
	         ioe.printStackTrace();
	    }
		
		return body;
	}
	
	public static ArrayList<List<String>> parseBody(String body) {
		ArrayList<List<String>> torrents = new ArrayList<List<String>>();
		
		// PATTERNS AREA
		Pattern Pdl = Pattern.compile(Pattern.quote("<dl>"));
		Pattern Pend_dl = Pattern.compile(Pattern.quote("</dl>"));
		Pattern Pdt = Pattern.compile(Pattern.quote("<dt>"));
		Pattern Pend_dt = Pattern.compile(Pattern.quote("</dt>"));
		Pattern Pbeghash = Pattern.compile(Pattern.quote("<a href=/"));
		Pattern Phash = Pattern.compile(Pattern.quote(">"));
		
		Pattern Ptitle = Pattern.compile(Pattern.quote("</a>"));
		
		Pattern Pdd = Pattern.compile(Pattern.quote("<dd>"));
		Pattern Penddd = Pattern.compile(Pattern.quote("</dd>"));
		Pattern Pbegseeds = Pattern.compile(Pattern.quote("<span>"));
		Pattern Pseeds = Pattern.compile(Pattern.quote("</span>"));
		
		Pattern Pbegcat = Pattern.compile(Pattern.quote("</a>"));
		Pattern Pcat = Pattern.compile(Pattern.quote("</dt>"));
		Pattern Pvideo = Pattern.compile(Pattern.quote("video"));
		// END
		
		String[] dls = Pdl.split(body);
		for(int i=1;i<dls.length;i++) {
			if(dls.length<1) {
				return torrents;				
			}
			
			String result = Pend_dl.split(dls[i])[0];
			String dt = Pdt.split(result)[1];
			String enddt = Pend_dt.split(dt)[0];
			
			String beghash = Pbeghash.split(enddt)[1];
			String hash = Phash.split(beghash)[0];
			
			// LINK = http(s)://MIRROR/hash
			
			Pattern Pbegtitle = Pattern.compile(Pattern.quote("<a href=/"+hash+">"));
			String begtitle = Pbegtitle.split(enddt)[1];
			String title = Ptitle.split(begtitle)[0];
			
			String dd = Pdd.split(result)[1];
			String enddd = Penddd.split(dd)[0];
			String begseeds = Pbegseeds.split(enddd)[3];
			String seeds = Pseeds.split(begseeds)[0];
			
			if(Pbegcat.split(enddt).length > 1) {
				String begcat = Pbegcat.split(enddt)[1];
				String cat = Pcat.split(begcat)[0];
				if(Pvideo.split(cat).length>1) {
					List<String> torrent = new ArrayList<String>();
					torrent.add(title);
					torrent.add(hash);
					torrent.add(seeds);
					torrents.add(torrent);
				}
			}
		}
		
		return torrents;
	}
}
