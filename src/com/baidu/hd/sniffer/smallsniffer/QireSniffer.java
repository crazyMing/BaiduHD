package com.baidu.hd.sniffer.smallsniffer;
import java.util.ArrayList;
import java.util.HashMap;

public class QireSniffer implements ISniffer {
	final String TAG = "pp_play";

	class PP_Play {
		HashMap<String, String> values = new HashMap<String, String>(32);
		ArrayList<String> bdList;
		void exeJs(String js) {
			if (js == null || "".equals(js.trim())) {
				return;
			}
			if (js.startsWith("var ")) {
				js = js.substring(4);
			}
			//System.out.println("exe:\t"+js);
			try {
				String[] jsArr = js.split("=");
				String left = jsArr[0];
				String right = jsArr[1];
				if (right.startsWith("\"")||(right.contains("+")&&!values.containsKey(left))) {
					String vArr[] = (right + "+").split("[+]");
					StringBuffer buf = new StringBuffer();
					for (String temp : vArr) {
						temp = temp.trim();
						String res = temp;
						if (temp.startsWith("\'") || temp.startsWith("\"")) {
							res = temp.replace("\"", "").replace("\'", "");
						} else if (values.containsKey(temp)) {
							res = values.get(temp);
						}
						buf.append(res);
						//System.out.println("buf is "+buf.toString());
					}
					values.put(left, buf.toString());
					//System.out.println(left+" put value "+buf.toString());
				} else if (right.contains(".replace(")) {
					String[] temp = right.replace("(", "").replace(")", "")
							.split(".replace");
					String value = values.get(temp[0].trim());
					//System.out.println(temp[0].trim()+" in map is "+value);
					for (int i = 1; i < temp.length; i++) {
						value = replace(value, temp[i]);
					}
					values.put(left, value);
				}
			} catch (Exception e) {
				e.printStackTrace();
				//System.out.println("error js:" + js);
				return;
			}
		}
		
		private String replace(String input, String param) {
			String res = "";
			try{
			
			String[] pArr = param.split(",");
			String left = pArr[0].replace("\'", "").replace("\"", "").trim();
			String right = pArr[1].replace("\'", "").replace("\"", "").trim();
			res = input.replace(left, right);}catch (Exception e) {
				e.printStackTrace();
				System.out.println(input+","+param);
			}
			return res;
		}
	}
	@Override
	public boolean Snifferable(String pageUrl) {
		return pageUrl!=null&&pageUrl.contains("qire123.com/videos/");
	}

	@Override
	public String getBdhd(String pageUrl, String html) {
		int start = html.indexOf("var pp_link=");
		int end = html.indexOf("var pp_plays=");
		html = html.substring(start, end);
		String[] scripts = html.split(";");
		PP_Play play = new PP_Play();
		for (String temp : scripts) {
			play.exeJs(temp);
		}
		int i = pageUrl.indexOf("pid-") + 3;
		int j = pageUrl.lastIndexOf(".");
		int index = Integer.parseInt(pageUrl.substring(i + 1, j));
		String code = play.values.get(TAG);
		String res = (Decoder.Utf8URLdecode(code) + "+++");
		ArrayList<String> bdhds=BDHD.searchBDHD(res);
		//res = res.split("[+][+][+]")[index];
		return bdhds.get(index);
	}

	@Override
	public ArrayList<String> getBdhdList() {
		// TODO Auto-generated method stub
		return null;
	}
}
