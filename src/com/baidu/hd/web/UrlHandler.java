package com.baidu.hd.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.baidu.hd.MainTabActivity;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.TaskFactory;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.VideoFactory;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId;
import com.baidu.hd.task.TaskManager;
import com.baidu.hd.util.Const;
import com.baidu.hd.R;

public class UrlHandler {
	
	public static class Result {
		private String result = "";
		public String getResult() {
			return result;
		}
		public void setResult(String value) {
			result = value;
		}
	}
	
	private Logger logger = new Logger("UrlHandler");

	private Context mContext = null;
	private ServiceFactory mServiceFactory = null;
	
	/** 是否可以继续响应点击 */
	private boolean isContinue = true;
	
	/** 详情页 */
	private boolean isStartNew = true;
	private String mSpecUrl = "";
	private String mSpecName = "";
	
	/** 下载选集页 */
	private String mDownPageName = "";
	private int mVideoType = NetVideo.NetVideoType.NONE;
	
	public UrlHandler(Context context, ServiceFactory serviceFactory) {
		this.mContext = context;
		mServiceFactory = serviceFactory;
	}
	
	public boolean isContinue() {
		return isContinue;
	}
	
	public void reset() {
		isContinue = true;
	}
	
	public boolean handleUrl(String message, String identity, Result result) {
		if(identity == null || !identity.startsWith("bdapi:")) {
			return false;
		}
		
		this.logger.d("handle url " + identity + " " + message);
		if(!isContinue) {
			logger.d("not continue");
			return true;
		}
		
		String service = "";
		String action = "";
		
        try {
        	JSONArray array = new JSONArray(identity.substring(6));
        	service = array.getString(0);
        	action = array.getString(1);
        } catch (JSONException e) {
        	e.printStackTrace();
        	return false;
        }
        
        if(!service.equals("bdvideo")) {
        	return false;
        }
        
        try {
        	JSONArray param = new JSONArray(message);
			if("toSearch".equalsIgnoreCase(action)) {
				toSearch(param);
			} else if("toVideoSite".equalsIgnoreCase(action)) {
				toPlayer(param);
			} else if("toSpec".equalsIgnoreCase(action)) {
				toSpec(param);
			} else if("toDownload".equalsIgnoreCase(action)) {
				toDownload(param);
			} else if("toDownloadPage".equalsIgnoreCase(action)) {
				toDownloadPage(param);
			} else if("toDownloadSelected".equalsIgnoreCase(action)) {
				toDownloadSelected(param);
			} else if("addFav".equalsIgnoreCase(action)) {
				setFavorite(param);
			} else if("isFav".equalsIgnoreCase(action)) {
			} else if("getVersion".equalsIgnoreCase(action)) {
				result.setResult("2.201");
			}
        } catch (JSONException e) {
        	e.printStackTrace();
        } catch(Exception e) {
        	e.printStackTrace();
        }
        return true;
	}
	
	public boolean isStartNew() {
		return isStartNew;
	}
	
	public void setDownloadPageName(String name) {
		this.mDownPageName = name;
	}

	public void setDownloadPageVideoType(int videoType) {
		this.mVideoType = videoType;
	}
	
	public String getSpecUrl() {
		return mSpecUrl;
	}
	
	public String getSpecName() {
		return mSpecName;
	}
	
	public void setSearchSource(int value) {
	}

	public void toSearch(String keyword) {
	}
	
	public void toVoiceSearch(ArrayList<String> voiceList) {
	}
	
	private void toSpec(JSONArray param) {
		Intent intent = new Intent();
		try {
			mSpecUrl = param.getString(0);
			mSpecName = param.getString(1);
			if(param.length() > 2) {
				isStartNew = param.getBoolean(2);
			}
			if(isStartNew) {
			} else {
				return;
			}
		} catch(JSONException e) {
			e.printStackTrace();
			return;
		}
		MainTabActivity.setOnPauseAnimation(true);
		startActivity(intent);
		prompt("toSpec", intent);
	}

	private void toSearch(JSONArray param) 
	{
	}
	
	private void toPlayer(JSONArray param) {
		
		Map<String, String> mapParam = new Hashtable<String, String>();
		try {
			mapParam.put("url", param.getString(0));
			mapParam.put("listid", param.getString(1));
			mapParam.put("id", param.getString(2));
			mapParam.put("name", param.getString(3));
			mapParam.put("image", param.getString(4));
			mapParam.put("refer", param.getString(5));
			mapParam.put("type", param.getInt(6) + "");
			mapParam.put("bdhd", param.getString(7));
			mapParam.put("newest", param.getString(8));
			mapParam.put("isfinished", param.getBoolean(9) + "");
			mapParam.put("site", param.getString(10));
			mapParam.put("year", param.getString(11));
		} catch(JSONException e) {
			e.printStackTrace();
			return;
		}
		Album a = this.createAlbum(mapParam);
		NetVideo v = this.createVideo(mapParam);
		if (a != null) a.handleName(v);
		if(v.isBdhd()) {
			PlayerLauncher.startup(mContext, null, v);
		} else {
		}
		isContinue = false;
		prompt("toVideoSite", mapParam);
	}
	
	private void toDownload(JSONArray param) {
		String url = "";
		String name = "";
		try {
			name = param.getString(0);
			url = param.getString(1);
		} catch(JSONException e) {
			e.printStackTrace();
			return;
		}
		prompt("toDownloadPage", "name:" + name + "\r\nurl:" + url);
		if("".equals(url) || "".equals(name)) {
			return;
		}
		
		Task task = null;
		if(url.startsWith("bdhd")) {
			task = TaskFactory.create(Task.Type.Small);
			task.setName(name);
			task.setUrl(url);
			task.setVideoType(NetVideo.NetVideoType.P2P_STREAM);
		} else {
			task = TaskFactory.create(Task.Type.Big);
			task.setName(name);
			task.setRefer(url);
			task.setVideoType(NetVideo.NetVideoType.P2P_STREAM);
		}
		downloadTask(task);
		Toast.makeText(mContext,
				R.string.download_tip, Toast.LENGTH_LONG).show();
	}
	
	private void toDownloadSelected(JSONArray param) {
		List<Task> tasks = new ArrayList<Task>();
		try {
			for(int i = 0; i < param.length(); ++i) {
				JSONObject obj = param.getJSONObject(i);
				String name = obj.optString("t");
				String url = obj.optString("u");
				
				Task task = TaskFactory.create(Task.Type.Big);
				task.setName(name);
				task.setRefer(url);
				tasks.add(task);
			}
		} catch(JSONException e) {
			e.printStackTrace();
			return;
		}
		for(Task task: tasks) {
			task.setName(mDownPageName + task.getName());
			task.setVideoType(mVideoType);
			downloadTask(task);
		}
		
		int textId = R.string.download_tip;
		if(tasks.isEmpty()) {
			textId = R.string.download_no_select_tip;
		}
		Toast.makeText(mContext, textId, Toast.LENGTH_LONG).show();
	}
	
	private void toDownloadPage(JSONArray param) {
		Intent intent = new Intent();
		try {
			intent.putExtra(Const.IntentExtraKey.DownloadPageUrl, param.getString(0));
			intent.putExtra(Const.IntentExtraKey.DownloadPageName, param.getString(1));
			intent.putExtra(Const.IntentExtraKey.DownloadPageType, param.getInt(2));
		} catch(JSONException e) {
			e.printStackTrace();
			return;
		}
		startActivity(intent);
		prompt("toDownloadPage", intent);
	}
	
	private void setFavorite(JSONArray param) {
		Map<String, String> mapParam = new Hashtable<String, String>();
		try {
			mapParam.put("listid", param.getString(0));
			mapParam.put("name", param.getString(1));
			mapParam.put("image", param.getString(2));
			mapParam.put("refer", param.getString(3));
			mapParam.put("type", param.getInt(4) + "");
			mapParam.put("newest", param.getString(5));
			mapParam.put("isfinished", param.getBoolean(6) + "");
			mapParam.put("site", param.getString(7));
		} catch(JSONException e) {
			e.printStackTrace();
			return;
		}
		Album a = this.createAlbum(mapParam);

		PlayListManager manager = (PlayListManager)mServiceFactory.getServiceProvider(PlayListManager.class);
		manager.setFavorite(a);

		prompt("setFavorite", mapParam);
	}

	private void startActivity(Intent intent) {
		mContext.startActivity(intent);
		isContinue = false;
	}
	
	private Album createAlbum(Map<String, String> param) {
			return null;
	}
	
	private NetVideo createVideo(Map<String, String> param) {
		
		if(!param.containsKey("id")) {
			return null;
		}
		
		NetVideo v = VideoFactory.create(false).toNet();
		v.setType(Integer.parseInt(param.get("type")));
		v.setName(param.get("name"));
		v.setRefer(param.get("url"));
		v.setEpisode(param.get("id"));
		
		if(!"".equals(param.get("bdhd"))) {
			v.setUrl(param.get("bdhd"));
		}
		return v;
	}
	
	private void downloadTask(Task task) {
		TaskManager taskManager = (TaskManager)mServiceFactory.getServiceProvider(TaskManager.class);
		if(taskManager.find(task.getKey()) == null) {
			Stat stat = (Stat)mServiceFactory.getServiceProvider(Stat.class);
			stat.incLogCount(StatId.BaseDownloadCount + task.getVideoType());
			stat.incEventCount(StatId.Download.Name, StatId.Download.Count + task.getFormatVideoType());
		}
		taskManager.start(task);
	}
	
	// debug
	private void prompt(String key, String msg) {
		if(!Const.isDebug) {
			return;
		}
		Toast.makeText(mContext, key + "\r\n" + msg, Toast.LENGTH_LONG).show();
	}
	
	private void prompt(String key, Intent intent) {
		if(!Const.isDebug) {
			return;
		}
		String msg = "";
		for(String k : intent.getExtras().keySet()) {
			msg += k + ":" + intent.getExtras().getString(k) + "\r\n";
		}
		Toast.makeText(mContext, key + "\r\n" + msg, Toast.LENGTH_LONG).show();
	}
	
	private void prompt(String key, Map<String, String> param) {
		if(!Const.isDebug) {
			return;
		}
		String msg = "";
		for(Map.Entry<String, String> entry: param.entrySet()) {
			msg += entry.getKey() + ":" + entry.getValue() + "\r\n";
		}
		Toast.makeText(mContext, key + "\r\n" + msg, Toast.LENGTH_LONG).show();
	}
}
