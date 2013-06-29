package com.baidu.hd.module.album;

import java.util.ArrayList;
import java.util.List;

import com.baidu.hd.log.Logger;
import com.baidu.hd.module.album.AlbumFactory.AlbumType;

import android.os.Bundle;


/**
 * ר����Ϣ
 */
public abstract class Album {
	private static Logger logger = new Logger("Album");

	private long id = -1;
	
	/** ����ҳ���� */
	private String refer = "";
	
	/** �缯id */
	private String listId = "";
	
	/** �缯���� */
	private String listName = "";
	
	/** �缯ͼƬ */
	private String image = "";
	
	/** վ�� */
	private String site = "";
	
	/** ���� */
	private int type = NetVideo.NetVideoType.NONE;
	
	/** ��ǰ���ż� */
	private NetVideo current = VideoFactory.create(false).toNet();
	
	/** ���¼� */
	private String newestId = "";
	
	/** �Ƿ���� */
	private boolean isFinished = false;
	
	/** �Ƿ����¾缯 */
	private boolean haveNew = false;
	
	/** �Ƿ�����ʷ��ť������ */
	private boolean push = false;
	
	/** �Ƿ��ڲ�����ʷ�б��� */
	private boolean isPersonalHistory = false;
	
	/** �Ƿ��Ѿ��ղ� */
	private boolean isFavorite = false;
	
	/**
	 * ���յ����
	 * �� ��ǰtypeΪ����ʱ��Ч
	 */
	private String year = "";
	
	/**
	 * �Ƿ��Ѿ���ȡ�˲����б�
	 */
	private boolean pulled = false;
	
	/**
	 * �Ƿ���Ҫ����ҳ��ʾ
	 */
	private boolean isHomeShow = false;
	
	/**
	 * �����б���
	 */
	private boolean isDownload = false;
	
	/**
	 * �����˶��ټ�
	 */
	private int newestCount = 0;
	
	/**
	 * �缯��Դ 0--��վ��������1--��վ���أ�2--Сվ��������3--Сվ����
	 */
	private int fromType = -1;

	/**
	 * �缯�б�
	 */
	private List<NetVideo> videos = new ArrayList<NetVideo>();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getRefer() {
		return refer;
	}

	public void setRefer(String refer) {
		this.refer = refer;
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
		
	}

	public NetVideo getCurrent() {
		return current;
	}

	public void setCurrent(NetVideo current) {
		this.current = current;
	}

	public String getNewestId() {
		return newestId;
	}

	public void setNewestId(String newestId) {
		this.newestId = newestId;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public boolean isHaveNew() {
		return haveNew;
	}

	public void setHaveNew(boolean haveNew) {
		this.haveNew = haveNew;
	}
	
	public boolean isPush() {
		return push;
	}
	
	public void setPush(boolean push) {
		this.push = push;
	}
	
	public boolean isPersonalHistory() {
		return isPersonalHistory;
	}
	
	public void setPersonalHistory(boolean isPersonalHistory) {
		this.isPersonalHistory = isPersonalHistory;
	}
	
	public boolean isFavorite() {
		return isFavorite;
	}
	
	public void setFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public boolean isPulled() {
		return pulled;
	}

	public void setPulled(boolean pulled) {
		this.pulled = pulled;
	}

	public boolean isHomeShow() {
		return isHomeShow;
	}

	public void setHomeShow(boolean isHomeShow) {
		this.isHomeShow = isHomeShow;
	}

	public boolean isDownload() {
		return isDownload;
	}

	public void setDownload(boolean isDownload) {
		this.isDownload = isDownload;
	}

	public int getNewestCount() {
		return newestCount;
	}

	public void setNewestCount(int newestCount) {
		this.newestCount = newestCount;
	}

	public int getFromType() {
		return fromType;
	}

	public void setFromType(int fromType) {
		this.fromType = fromType;
	}

	public List<NetVideo> getVideos() {
		return videos;
	}

	public void setVideos(List<NetVideo> videos) {
		this.videos = videos;
	}
	
	public Bundle toBundle() {
		
		Bundle b = new Bundle();
		b.putLong("id", this.id);
		b.putString("refer", this.refer);
		b.putString("listid", this.listId);
		b.putString("listname", this.listName);
		b.putString("image", this.image);
		b.putString("site", this.site);
		b.putString("year", this.year);
		b.putInt("type", this.type);
		b.putBundle("current", this.current.toBundle());
		b.putString("newest", this.newestId);
		b.putBoolean("havenew", this.haveNew);
		b.putBoolean("isfinish", this.isFinished);
		b.putBoolean("push", this.push);
		b.putBoolean("personalHistory", this.isPersonalHistory);
		b.putBoolean("favorite", this.isFavorite);
		b.putBoolean("isHomeShow", this.isHomeShow);
		b.putBoolean("isDownload", this.isDownload);
		b.putInt("newestCount", this.getNewestCount());
		b.putInt("fromType", this.fromType);
		return b;
	}
	
	public static Album fromBundle(Bundle b) {
		
		if(b == null) {
			return null;
		}
		Album a = null;
		int type = b.getInt("fromType");
		switch(type) {
		case 0:
			a = AlbumFactory.getInstance().createAlbum(AlbumType.BIG_SERVER);
			break;
		case 1:
			a = AlbumFactory.getInstance().createAlbum(AlbumType.BIG_NATIVE);
			break;
		case 2:
			a = AlbumFactory.getInstance().createAlbum(AlbumType.SMALL_SERVER);
			break;
		case 3:
			a = AlbumFactory.getInstance().createAlbum(AlbumType.BIG_NATIVE);
			break;
		default:
			logger.d("fromType = -1");
			a = AlbumFactory.getInstance().createAlbum(AlbumType.SMALL_SERVER);
		}
		
		a.id = b.getLong("id");
		a.refer = b.getString("refer");
		a.listId = b.getString("listid");
		a.listName = b.getString("listname");
		a.image = b.getString("image");
		a.site = b.getString("site");
		a.year = b.getString("year");
		a.type = b.getInt("type");
		a.current = NetVideo.fromBundle(b.getBundle("current")).toNet();
		a.newestId = b.getString("newest");
		a.haveNew = b.getBoolean("haveNew");
		a.isFinished = b.getBoolean("isfinish");
		a.push = b.getBoolean("push");
		a.isPersonalHistory = b.getBoolean("personalHistory");
		a.isFavorite = b.getBoolean("favorite");
		a.isHomeShow = b.getBoolean("isHomeShow");
		a.isDownload = b.getBoolean("isDownload");
		a.newestCount = b.getInt("newestCount");
		a.fromType = b.getInt("fromType");
		return a;
	}
	
	public void handleName(NetVideo video) {
		
		if(video == null) {
			return;
		}
	}
	
	public SmallServerAlbum asSmallServerAlbum() {
		return null;
	}
	
	public SmallNativeAlbum asSmallNativeAlbum() {
		return null;
	}
	
	public BigServerAlbum asBigServerAlbum() {
		return null;
	}
	
	public BigNativeAlbum asBigNativeAlbum() {
		return null;
	}
}




