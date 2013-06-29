package com.baidu.hd.sniffer.smallsniffer;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class Geter {

	public static String get(String url) throws Exception{
//		HttpURLConnection urlConnection=(HttpURLConnection) new URL(url).openConnection();
//		InputStream in = urlConnection.getInputStream();
//		BufferedReader bufferedReader = new BufferedReader(
//				new InputStreamReader(in));
//		StringBuffer temp = new StringBuffer();
//		String line = bufferedReader.readLine();
//		while (line != null) {
//			temp.append(line).append("\r\n");
//			line = bufferedReader.readLine();
//		}
//		bufferedReader.close();
//		return temp.toString();
		
		HttpGet request = new HttpGet(url);
		
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(request);
		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			return EntityUtils.toString(response.getEntity(), "UTF-8");
		}
		
		return "";
	}
}
