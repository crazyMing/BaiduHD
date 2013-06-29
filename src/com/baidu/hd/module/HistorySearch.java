package com.baidu.hd.module;

public class HistorySearch {

	private long id = -1;
	private String text = "";
	private boolean isUrl = false;
	private boolean isHistory = false;
	
	public  HistorySearch(){}
	
	public  HistorySearch(String value)
	{
		setText(value);
	}
	
	public long getId() 
	{
		return id;
	}
	
	public void setId(long id)
	{
		this.id = id;
	}
	
	public String getText() 
	{
		return text;
	}
	
	public void setText(String value) 
	{
		this.text = value;
	}
	
	public boolean isUrl() 
	{
		return isUrl;
	}
	
	public void setUrl(boolean isUrl) 
	{
		this.isUrl = isUrl;
	}
	
	public boolean isHistory() 
	{
		return isHistory;
	}
	
	public void setHistory(boolean isHistory) 
	{
		this.isHistory = isHistory;
	}
}
