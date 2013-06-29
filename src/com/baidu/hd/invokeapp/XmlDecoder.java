package com.baidu.hd.invokeapp;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import android.util.Log;
/**
 * 白名单xml操作类
 * @author juqiang
 *
 */
public class XmlDecoder {
	
	private static final String lastName = "wname";
	private static final String root = "/data/data/com.baidu.hd/files";

	/**
	 * 
	 * @param updataTime 更新间隔 ms
	 * @param url 在线xml地址
	 */
	public XmlDecoder(long updataTime,String url){
		this.updataTime=updataTime;
		this.onlineXml=url;
	}
	
	/**
	 * 检查.
	 * 若到达updataTime,更新文件
	 */
	public void checkForUpdate() {
		try {
			if (shouldFileUpdate(searchForFile())) {
				log("should update");
				SAXReader reader = new SAXReader();
				Document doc = reader.read(new URL(onlineXml));
				clear();
				OutputFormat outputFormat = OutputFormat.createPrettyPrint();
				outputFormat.setLineSeparator("\r\n");
				Writer writer = new FileWriter(createName());
				XMLWriter outPut = new XMLWriter(writer, outputFormat);
				outPut.write(doc);
				outPut.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 判断某host是否合法
	 * @param host
	 * @return
	 */
	public boolean isNameValid(String host) {
		File xml = searchForFile();
		SAXReader reader = new SAXReader();
		try {
			Document doc = null;
			if (xml != null) {
				doc = reader.read(xml);
			} else {
				doc = reader.read(new URL(onlineXml));
				if (doc != null) {
					clear();
					OutputFormat outputFormat = OutputFormat
							.createPrettyPrint();
					outputFormat.setLineSeparator("\r\n");
					Writer writer = new FileWriter(createName());
					XMLWriter outPut = new XMLWriter(writer, outputFormat);
					outPut.write(doc);
					outPut.close();
				}
			}
			return isDocConents(doc, host);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//----------------------------私有方法---------------------------------//
	private String createName() {
		return root + "/" + (new Date().getTime() + "." + lastName);
	}

	private boolean shouldFileUpdate(File f) {
		if (f == null || !f.exists()) {
			return true;
		}
		try {
			long curr = System.currentTimeMillis();
			String name = f.getName();
			long last = Long
					.parseLong(name.substring(0, name.lastIndexOf(".")));
			return curr - last >= updataTime;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	private File searchForFile() {
		File r = new File(root);
		if (!r.exists()) {
			r.mkdirs();
			return null;
		}
		for (File f : r.listFiles()) {
			if (f.getName().endsWith("." + lastName)) {
				log("search result is "+f.getName());
				return f;
			}
		}
		return null;
	}

	private void clear() {
		File r = new File(root);
		if (r.exists()) {
			for (File f : r.listFiles()) {
				if (f.getName().endsWith("." + lastName)) {
					boolean res=f.delete();
					log("del "+f.getName()+" "+res);
				}
			}
		}
	}

	private boolean isDocConents(Document doc, String value) {
		if (doc == null) {
			log("doc is null in isDocConents()");
			return false;
		} else {
			Element root = doc.getRootElement();
			@SuppressWarnings("unchecked")
			List<Element> clist = root.elements();
			for (int i = 0; i < clist.size(); i++) {
				if (clist.get(i).attribute("url").getValue().equals(value)) {
					return true;
				}
			}
			return false;
		}
	}
	
	private void log(String str){
		Log.d("xml", str);
	}
	
	private long updataTime;
	private String onlineXml;;
}
