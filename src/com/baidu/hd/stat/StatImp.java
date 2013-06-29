package com.baidu.hd.stat;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.hd.conf.Configuration;
import com.baidu.hd.detect.Detect;
import com.baidu.hd.log.Logger;
import com.baidu.hd.net.HttpComm;
import com.baidu.hd.service.ServiceConsumer;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.FileUtil;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.SystemUtil;
import com.baidu.hd.util.Turple;
import com.baidu.hd.util.UrlUtil;
import com.baidu.mobstat.StatService;
import com.baidu.player.download.DownloadServiceAdapter;

public class StatImp implements Stat, ServiceConsumer {

	private Context mContext = null;
	private ServiceFactory mServiceFactory = null;
	
	/**
	 * 统计相关
	 * 事件监听器用于设置数据
	 * 数据容器用于保存数据
	 */
	private EventStatListener mEventListener = new EventStatListener();
	private LogDataHolder mLogData = new LogDataHolder();
	private UdpDataHolder mUdpData = new UdpDataHolder();

	/**
	 * 上传组件
	 */
	private StatUploader mUploader = new StatUploader();
	
	/**
	 * 网络可用上传
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Detect detect = (Detect)mServiceFactory.getServiceProvider(Detect.class);
			if(detect.isNetAvailabe()) {
				upload();
			} else {
				sendEmptyMessageDelayed(0, Const.Elapse.StatNetAvailabeCheck);
			}
		}
	};

	@Override
	public void setContext(Context ctx) {
		mContext = ctx;
	}

	@Override
	public void onCreate() {
		
		if(!new File(StatConst.path).exists()) {
			new File(StatConst.path).mkdirs();
		}

		mEventListener.create(mServiceFactory, mLogData);
		mUploader.create(mServiceFactory);
		
		mHandler.sendEmptyMessageDelayed(0, Const.Elapse.StatNetAvailabeCheck);
	}

	@Override
	public void onDestory() {
		
		mHandler.removeMessages(0);
		mEventListener.destroy();
		
		FileUtil.write(StatConst.snifferPath, mLogData.getSnifferFail(), true);
		FileUtil.write(StatConst.playPath, mLogData.getPlayFail(), true);
		
		mLogData.appendStat(FileUtil.read(StatConst.statPath));
		FileUtil.write(StatConst.statPath, mLogData.getStat(), false);
	}

	@Override
	public void onSave() {
		// TODO 写如文件
	}

	@Override
	public void setServiceFactory(ServiceFactory factory) {
		mServiceFactory = factory;
	}

	@Override
	public void postLog() {
		Logger.flush();
		mUploader.postLog(FileUtil.read(Logger.getFileName()));
	}
	
	@Override
	public void addSnifferFail(String refer) {
		mLogData.addSnifferFail(refer);
	}

	@Override
	public void setLocalVideoCount(int value) {
		mLogData.setStat(StatId.LocalVideoCount, value);
	}

	@Override
	public void setNetState(NetState state) {
		mLogData.setStat(StatId.Wifi, state == NetState.eWifi ? 1 : 0);
		mLogData.setStat(StatId.NoWifi, state == NetState.eNoWifi ? 1 : 0);
	}

	@Override
	public void setHaveSDCard(boolean value) {
		mLogData.setStat(StatId.NoSDCard, (long)(value ? 0 : 1));
	}

	@Override
	public void incLogCount(int id) {
		mLogData.incStat(id);
	}

	@Override
	public void incEventCount(String type, String subType) {
		StatService.onEvent(mContext, type + "_" + subType, "");
	}

	@Override
	public void incEventValue(String type, String subType, int value) {
		StatService.onEvent(mContext, type + "_" + subType, "", value);
	}
	
	private boolean bAppValid = true;
	@Override
	public void incEventAppValid()
	{
		if (bAppValid)
		{
			incEventCount(StatId.App.Name, StatId.App.ValidCount);
		}
		else 
		{
			bAppValid = false;
		}
	}

	@Override
	public void incUdpCount(int id) {
		mUdpData.incStat(id);
	}

	@Override
	public void incUdpValue(int id, long value) {
		mUdpData.addStat(id, value);
	}

	@Override
	public void setUdpString(int id, String value) {
		mUdpData.setStatString(id, value);
	}

	@Override
	public void sendUdp() {
		DownloadServiceAdapter adapter = (DownloadServiceAdapter)mServiceFactory.getServiceProvider(DownloadServiceAdapter.class);
		adapter.statReport(StatId.Playing.Name, mUdpData.getStat());
		mUdpData.clear();
	}

	private void upload() {
		String message = FileUtil.read(StatConst.snifferPath);
		if(!StringUtil.isEmpty(message)) {
			mUploader.postSnifferFail(message);
		}
		new File(StatConst.snifferPath).delete();
		message = FileUtil.read(StatConst.playPath);
		if(!StringUtil.isEmpty(message)) {
			mUploader.postPlayFail(message);
		}
		new File(StatConst.playPath).delete();
		message = FileUtil.read(StatConst.crashPath);
		if(!StringUtil.isEmpty(message)) {
			mUploader.postLog(message);
		}
		new File(StatConst.crashPath).delete();
		
		// 统计信息
		
		Configuration conf = (Configuration)mServiceFactory.getServiceProvider(Configuration.class);
		String format = conf.getStartActivateUrl();
		
		String start = "activate=1&install=0&";
		String install = "activate=1&install=1&";
		String messageStart = buildStat();
		String messageActivate = "";
		String statMessgae = FileUtil.read(StatConst.statPath);
		if(!StringUtil.isEmpty(statMessgae)) {
			messageStart += "&" + statMessgae;
		}
		new File(StatConst.statPath).delete();

		// 首次激活统计
		SharedPreferences preference = this.mContext.getSharedPreferences(
				"stat", Context.MODE_WORLD_WRITEABLE);
		if(preference.getBoolean("isfirst", true)) {

			messageActivate = install + messageStart + buildActivate();
			
			DownloadServiceAdapter adapter = (DownloadServiceAdapter)mServiceFactory.getServiceProvider(DownloadServiceAdapter.class);
			adapter.statReport("Device", StatId.Computer.Device + "=" + UrlUtil.encode(SystemUtil.getMobileInfo()));
			adapter.statReport("Cpuinfo", StatId.Computer.CPUInfo + "=" + UrlUtil.encode(SystemUtil.getCPUInfo()));
			
			preference.edit().putBoolean("isfirst", false).commit();
			
			String url = String.format(format, messageActivate);
			new HttpComm().get(url, null);
			
		}
		else 
		{
			messageStart = start + messageStart;
			String url = String.format(format, messageStart);
			new HttpComm().get(url, null);
		}
	}
	
	/** 构建首次激活上报信息 */
	private String buildActivate() {
		StringBuilder sb = new StringBuilder();
		sb.append(StatId.Device + "=" + UrlUtil.encode(SystemUtil.getMobileInfo()));
		sb.append(StatConst.LogSep);
		Turple<Integer, Integer> resolution = SystemUtil.getResolution(mContext);
		sb.append(StatId.Resolution + "=" + resolution.getX() + "," + resolution.getY());
		sb.append(StatConst.LogSep);
		sb.append(StatId.CPUInfo + "=" + UrlUtil.encode(SystemUtil.getCPUInfo()));
		return StatConst.LogSep + sb.toString();
	}
	
	/** 构建通用统计上报信息 */
	private String buildStat() {
		StringBuilder sb = new StringBuilder();
		sb.append("deviceId=" + SystemUtil.getEmid(mContext));
		sb.append(StatConst.LogSep);
		sb.append("md=android");
		sb.append(StatConst.LogSep);
		sb.append("appver=" + SystemUtil.getAppVerison(mContext));
		sb.append(StatConst.LogSep);
		sb.append("chl=" + MarketChannelHelper.getInstance(mContext).getChannelID());
		return sb.toString();
	}
}
