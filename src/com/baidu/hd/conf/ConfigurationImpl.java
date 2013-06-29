package com.baidu.hd.conf;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.baidu.hd.util.Const;

public class ConfigurationImpl implements Configuration {

	private Context mContext = null;

	@Override
	public void setContext(Context ctx) {
		this.mContext = ctx;
		
		if(ctx != null) {
		}
	}

	@Override
	public void onCreate() {
 
	}

	@Override
	public void onDestory() {
	}

	@Override
	public void onSave() {
	}

	private Field appUpgradeUrl = new Field(
			"http://php.player.baidu.com/android/upgrade.xml?deviceId=%s&md=android&ver=%s&chl=%s");

	private Field playerCoreUpgradeUrl = new Field(
			"http://fsreport.p2sp.baidu.com/android/playerCore.php"
			);


	private Field bigSiteSnifferUrl = new Field(
			"http://gate.baidu.com/tc?m=8&video_app=1&ajax=1&src=%s");
	

	private Field bigSiteAlbumInfoUrl = new Field(
			"http://album.ikan.baidu.com/albumInfo?url=%s");
	private Field bigSitePlayInfoUrl = new Field(
			"http://album.ikan.baidu.com/playInfo?url=%s");
	private Field bigSiteUpdateInfoUrl = new Field(
			"http://album.ikan.baidu.com/update?req=%s");
	

	private Field smallSiteSnifferUrl = new Field(
			"http://idmap.p2sp.baidu.com/vsniffer?from=5&version=%s&pccode=%s&url=%s");
//	private Field smallSiteSnifferUrl = new Field(
//			"http://tjyx-testing-platqa1071.vm.baidu.com:8080/android/vsniffer.php?from=5&version=%s&pccode=%s&url=%s");
	
	private Field searchUrl = new Field(
			"http://vsniffer.p2sp.baidu.com/vsniffer?from=5&version=%s&pccode=%s&url=%s");
	
	private Field adressleisteSuggestServer = new Field(
			"http://nssug.baidu.com/su?prod=iphone_video&callback=SuggestionService.defaultDataProcessor&wd=%s");

	private Field startActivateUrl = new Field(
			"http://php.player.baidu.com/android/log.php?%s");
	
	private Field feedbackUrl = new Field(
			"http://netreport.p2sp.baidu.com/mobilepost_v2");

	private Field uploadLogUrl = new Field(
			"http://netreport.p2sp.baidu.com/mobilepost_v2%s");

	private Field imagePath = new Field(
			Environment.getExternalStorageDirectory() + "/baidu/baiduplayer/image/");

	private Field taskFileNameExt = new Field(".bdv");
	
	private Field snifferUA = new Field("Mozilla/5.0 (iPhone; U; CPU iPhone OS 5_0_1 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7");
	
	private Field playUA = new Field("Lavf54.14.100");

	private Field taskMaxDownload = new Field(3);

	@Override
	public String getFeedbackUrl() {
		return this.feedbackUrl.getString();
	}

	@Override
	public String getUploadLogUrl() {
		return this.uploadLogUrl.getString();
	}

	@Override
	public String getTaskSavePath() {
		SharedPreferences sharedPreferences = 	mContext.getSharedPreferences(Const.SharedPreferences.NAME_NetVideoBufferPath, Context.MODE_PRIVATE);
		return sharedPreferences.getString(Const.SharedPreferences.KEY_NetVideoBufferPath, Const.Path.NetVideoBufferFilePath);
	}

	@Override
	public int getTaskMaxDownload() {
		return this.taskMaxDownload.getInt();
	}

	@Override
	public String getImagePath() {
		return this.imagePath.getString();
	}

	@Override
	public String getTaskFileNameExt() {
		return this.taskFileNameExt.getString();
	}

	@Override
	public String getAdressleisteSuggestServer() {
		return this.adressleisteSuggestServer.getString();
	}
	
	@Override
	public String getAppUpgradeUrl() {
		return this.appUpgradeUrl.getString();
	}

	@Override
	public String getPlayerCoreUpgradeUrl() {
		return this.playerCoreUpgradeUrl.getString();
	}
	
	@Override
	public String getSearchUrl()
	{
		return this.searchUrl.getString();
	}

	@Override
	public String getBigSiteSnifferUrl() {
		return bigSiteSnifferUrl.getString();
	}
	

	@Override
	public String getAlbumInfoUrl() {
		return bigSiteAlbumInfoUrl.getString();
	}

	@Override
	public String getPlayInfoUrl() {
		return bigSitePlayInfoUrl.getString();
	}

	@Override
	public String getUpdateInfoUrl() {
		return bigSiteUpdateInfoUrl.getString();
	}

	@Override
	public String getSmallSiteSnifferUrl() {
		return smallSiteSnifferUrl.getString();
	}

	@Override
	public String getSnifferUA() {
		return snifferUA.getString();
	}

	@Override
	public String getPlayUA() {
		return playUA.getString();
	}
	
	@Override
	public String getStartActivateUrl()
	{
		return this.startActivateUrl.getString();
	}

	private class Field {
		private String strValue = "";
		private int intValue = 0;

		public String getString() {
			return strValue;
		}
 

		public int getInt() {
			return intValue;
		}

		public Field(String value) {
			strValue = value;
		}

		public Field(int value) {
			intValue = value;
		}
	}
}
