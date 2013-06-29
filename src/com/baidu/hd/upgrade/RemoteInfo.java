package com.baidu.hd.upgrade;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class RemoteInfo {
	
	private String mVersion = "";
	private String mUrl = "";
	private String mComment = "";

	public String getVersion() {
		return this.valid() ? this.mVersion : "";
	}

	public String getUrl() {
		return this.valid() ? this.mUrl : "";
	}
	
	public String getComment() {
		return this.valid() ? this.mComment : "";
	}

	public RemoteInfo(String remoteMessage) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(new ByteArrayInputStream (remoteMessage.getBytes()));
			Element root = dom.getDocumentElement();

			NodeList nl = root.getChildNodes();
			for(int i = 0; i < nl.getLength(); ++i) {
				
				Node node = nl.item(i);
				if(node.getNodeName().equalsIgnoreCase("version")) {
					this.mVersion = this.getValue(node);
				}
				if(node.getNodeName().equalsIgnoreCase("url")) {
					this.mUrl = this.getValue(node);
				}
				if(node.getNodeName().equalsIgnoreCase("comment")) {
					this.mComment = this.getValue(node);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean valid() {
		// comment ¿ÉÒÔÎªempty
		return !"".equals(this.mVersion) && !"".equals(this.mUrl);
	}

	private String getValue(Node parentNode) {
		
		NodeList nodeList = parentNode.getChildNodes();
		if(nodeList.getLength() != 0) {
			for(int i = 0; i < nodeList.getLength(); ++i) {
				Node node = nodeList.item(i);
				if(node.getNodeType() == Node.CDATA_SECTION_NODE) {
					return node.getNodeValue();
				}
			}
		}
		return null;
	}
}
