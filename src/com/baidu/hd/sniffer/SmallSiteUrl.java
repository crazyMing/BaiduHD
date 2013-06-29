package com.baidu.hd.sniffer;

import java.util.ArrayList;
import java.util.List;

import com.baidu.hd.util.Turple;

/**
 * Сվ����֧�ַ��ص����ݽṹ��ʵ��
 */
public class SmallSiteUrl {

	private boolean isNativeSniffer = false;
	
	private String bdhd = "";   // ������̽��ַ
	
	private String link = "";
	
	// �����б�ҳrefer
	private String refer = "";
	
	private List<Turple<String, String>> playUrls = new ArrayList<Turple<String, String>>();

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getRefer() {
		return refer;
	}

	public void setRefer(String refer) {
		this.refer = refer;
	}

	public List<Turple<String, String>> getPlayUrls() {
		return playUrls;
	}

	public void setPlayUrls(List<Turple<String, String>> playUrls) {
		this.playUrls = playUrls;
	}

	@Override
	public String toString() {
		return "SmallSiteUrl [link=" + link + ", refer=" + refer
				+ ", playUrls=" + playUrls + "]";
	}
	
	public boolean isSuccess()
	{
		if (!"".equals(bdhd) || !"".equals(link) || playUrls.size() != 0)
		{
			return true;
		}
		return false;
	}
	
	public void setBdhd(String bdhd)
	{
		this.bdhd = bdhd;
	}
	
	public String getBdhd()
	{
		return this.bdhd;
	}
	
	public void setSnifferType(boolean bNative)
	{
		this.isNativeSniffer = bNative;
	}
	
	public boolean getSnifferType()
	{
		return this.isNativeSniffer;
	}
	
} 