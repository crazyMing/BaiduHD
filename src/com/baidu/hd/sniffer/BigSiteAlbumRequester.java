package com.baidu.hd.sniffer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.baidu.hd.conf.Configuration;
import com.baidu.hd.module.AlbumInfo;
import com.baidu.hd.module.PlayInfo;
import com.baidu.hd.module.UpdateInfo;
import com.baidu.hd.module.AlbumInfo.DetailInfo;
import com.baidu.hd.module.UpdateInfo.Request;
import com.baidu.hd.module.UpdateInfo.Response;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.util.Const;

public class BigSiteAlbumRequester {
	
	private static String mPlayInfoUrlFormate = "";
	private static String mAlbumInfoUrlFormate= "";
	private static String mUpdateInfoUrlFormate = "";

	private static BigSiteAlbumRequester mIntance = null;
	public static BigSiteAlbumRequester getIntance(ServiceFactory factory) {
		if (mIntance == null) {
			mIntance = new BigSiteAlbumRequester(factory);
		}
		return mIntance;
	}
	
	private BigSiteAlbumRequester(ServiceFactory serviceFactory) {
		Configuration conf = (Configuration)serviceFactory.getServiceProvider(Configuration.class);
		mPlayInfoUrlFormate = conf.getPlayInfoUrl();
		mAlbumInfoUrlFormate = conf.getAlbumInfoUrl();
		mUpdateInfoUrlFormate = conf.getUpdateInfoUrl();
	}
	
	public PlayInfo getPlayInfo(String url) {
		PlayInfo res = null;
		try {
//			String str = get(playInfoUrl + url);
			String str = get(String.format(mPlayInfoUrlFormate, url));
			JSONObject jobj = new JSONObject(str);
			res = (PlayInfo) fillByJson(new PlayInfo(), jobj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public AlbumInfo getAlbumInfo(String url) {
		AlbumInfo res = null;
		try {
//			String str = get(albumInfoUrl + url);
			String str = get(String.format(mAlbumInfoUrlFormate, url));
			JSONObject jobj = new JSONObject(str);
			String str1 = jobj.optJSONObject("info").optString("albumInfo");
			str = str1.substring(0, str1.length() - 1) + ","
					+ str.substring(1, str.length());
			res = (AlbumInfo) fillByJson(new AlbumInfo(), new JSONObject(str));
			JSONArray arr = jobj.optJSONObject("info").getJSONArray(
					"detailInfo");
			for (int i = 0; i < arr.length(); i++) {
				AlbumInfo.DetailInfo d = new DetailInfo();
				res.getDetailInfo().add(
						(DetailInfo) fillByJson(d, (JSONObject) arr.get(i)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public List<UpdateInfo.Response> getUpdateInfo(
			List<UpdateInfo.Request> requestList) {
		List<UpdateInfo.Response> res = new ArrayList<UpdateInfo.Response>();
		try {
//			StringBuffer sb = new StringBuffer(updateInfoUrl);
			StringBuffer sb = new StringBuffer("");
			sb.append("[");
			for (int i = 0; i < requestList.size(); i++) {
				Request request = requestList.get(i);
				sb.append("{");
				Field[] farr = request.getClass().getFields();
				for (Field f : farr) {
					if (f.getType().equals(String.class)) {
						sb.append("\"" + f.getName() + "\":" + "\""
								+ f.get(request) + "\"");
					} else {
						sb.append("\"" + f.getName() + "\":" + f.get(request));
					}
					if (f != farr[farr.length - 1]) {
						sb.append(",");
					}
				}
				sb.append(i == requestList.size() - 1 ? "}" : "},");
			}
			sb.append("]");
			System.out.println(sb.toString());
			JSONArray jarr = new JSONArray(get(String.format(mUpdateInfoUrlFormate, sb.toString())));
			for (int i = 0; i < jarr.length(); i++) {
				Response r = (Response) fillByJson(new Response(),
						jarr.getJSONObject(i));
				res.add(r);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	private Object fillByJson(Object res, JSONObject jobj) throws Exception {
		Field[] farr = res.getClass().getDeclaredFields();
		for (Field f : farr) {
			f.setAccessible(true);
			String name = f.getName();
			if (jobj.opt(name) != null) {
				f.set(res, jobj.opt(name));
			}
		}
		return res;
	}

	private String get(String url) throws IOException {
		/*
		 * in java use urlConnection HttpURLConnection urlConnection =
		 * (HttpURLConnection) new URL(url) .openConnection(); InputStream in =
		 * urlConnection.getInputStream(); BufferedReader bufferedReader = new
		 * BufferedReader( new InputStreamReader(in)); StringBuffer temp = new
		 * StringBuffer(); String line = bufferedReader.readLine(); while (line
		 * != null) { temp.append(line).append("\r\n"); line =
		 * bufferedReader.readLine(); } bufferedReader.close(); return
		 * temp.toString();
		 */

		// in android use httpClient
		HttpGet request = new HttpGet(url.replace("\"", "%22").replace("{", "%7b").replace("}", "%7d"));
		
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
		String key=response.getFirstHeader("X-BDYY").getValue();
		InputStream in=response.getEntity().getContent();
		ArrayList<Byte> arr=new ArrayList<Byte>();
		int i=0;
		while ((i = in.read())!=-1){  
            arr.add((byte) i);  
		}
		byte[] res=new byte[arr.size()];
		for(int j=0;j<arr.size();j++){
			res[j]=arr.get(j);
		}
			return com.baidu.hd.util.RC4.decry_RC4(res, key+"vieckslovelongzeluola");
		}
		return "";
	}
}
