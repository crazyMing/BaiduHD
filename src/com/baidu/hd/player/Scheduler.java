package com.baidu.hd.player;

import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.detect.Detect;
import com.baidu.hd.detect.FilterCallback;
import com.baidu.hd.detect.NetUsable;
import com.baidu.hd.detect.NetUsableChangedEventArgs;
import com.baidu.hd.detect.SDCardStateChangedEventArgs;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.Video;
import com.baidu.hd.module.album.VideoFactory;
import com.baidu.hd.personal.SDCardUtil;
import com.baidu.hd.player.TaskHandler.State;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.UrlUtil;

public class Scheduler {

	public interface Callback {
		void onSetTitle(String value);
		void onSniffer();
		void onPlay(String url, Video video);
		void onError(int errorCode);
		void onNew(Video video, Album album);
		void onRetry();
	}

	private static final int PlayMsg = 1;
	private static final int ErrorMsg = 2;
	private static final int NewMsg = 3;

	/**
	 * 主要用于本地任务的延迟发送消息，否则播放器会还未创建好
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (msg.what == PlayMsg) {
				//Log.e("qq", "video is "+mVideo.getName());
				mCallback.onPlay(msg.obj.toString(),mVideo);
			}
			if (msg.what == ErrorMsg) {
				mCallback.onError(msg.arg1);
			}
			if (msg.what == NewMsg) {
				NewPackage pack = (NewPackage) msg.obj;
				mCallback.onNew(pack.video, pack.album);
			}
		}
	};

	private EventListener mEventListener = new EventListener() {

		@Override
		public void onEvent(EventId id, EventArgs args) {
			
			if(id == EventId.eNetStateChanged) {
				NetUsableChangedEventArgs netArgs = (NetUsableChangedEventArgs) args;
				if (netArgs.getNew() == NetUsable.ePrompt) {
					notifyError(ErrorCode.NetNotUseable);
					getDetect().filterByNet(new FilterCallback() {

						@Override
						public void onDetectPromptReturn(DetectPromptReturn canUse) {
							if (canUse == FilterCallback.DetectPromptReturn.eTrue) {
								mCallback.onRetry();
							}
						}
					});
				}
			}
			if(id == EventId.eSDCardStateChanged) {
				SDCardStateChangedEventArgs sdArgs = (SDCardStateChangedEventArgs)args;
				if(!sdArgs.isCanUse()) {
					mCallback.onError(ErrorCode.SDCardNotUseable);
				}
			}
		}
	};

	private Logger logger = new Logger("Scheduler");

	private Video mVideo = null;

	private Album mAlbum = null;

	/**
	 * handlers
	 */
	private BaseActivity mBaseActivity = null;
	private ServiceFactory mServiceFactory = null;
	//private SnifferHandler mSnifferHandler = null;
	private TaskHandler mTaskHandler = null;
	private PlayListHandler mPlayListHandler = null;
	private boolean mCreateByIntentData = false;

	private Callback mCallback = null;
	public ArrayList<Task> waitArr = new ArrayList<Task>();

//	private SnifferHandler.Callback mSnifferCallback = new SnifferHandler.Callback() {
//
//		@Override
//		public void onComplete(boolean success, String url) {
//			onSnifferComplete(success, url);
//		}
//	};

	private TaskHandler.Callback mTaskCallback = new TaskHandler.Callback() {

		@Override
		public void onComplete(String url, int errorCode) {
			onTaskComplete(url, errorCode);
		}

		@Override
		public void onSetTitle(String value) {
			if("".equals(mVideo.getName())) {
				mVideo.setName(value);
				if(mCallback != null) {
					mCallback.onSetTitle(value);
				}
			}
		}

		@Override
		public Album getAlbum() {
			return mAlbum;
		}
	};

	public Scheduler(BaseActivity baseActivity, Callback callback) {
		mBaseActivity = baseActivity;
		mServiceFactory = (ServiceFactory) mBaseActivity.getPlayerApp()
				.getServiceFactory();
		mCallback = callback;
		//mSnifferHandler = new SnifferHandler(mServiceFactory, mSnifferCallback);
		mTaskHandler = new TaskHandler(mServiceFactory, mTaskCallback);
		mPlayListHandler = new PlayListHandler(mServiceFactory);
	}

	public void create(Intent intent) {

		Video video = null;
		Album album = null;
		
		String schema = intent.getScheme();
		Uri uriData = intent.getData();
		if("file".equals(schema)) {
			logger.d("create from associated file");
			if (uriData == null) {
				logger.e("null uri data in file");
				return;
			}
			mCreateByIntentData = true;
			video = VideoFactory.create(true);
			video.toLocal().setFullName(uriData.getPath());
			
		} else if("bdvideo".equals(schema)) {
			logger.d("create from bdvideo:play");
			if(uriData == null) {
				logger.e("null uri data in bdvideo");
				return;
			}
			String data = uriData.toString();
			if(!data.startsWith("bdvideo://play/")) {
				logger.e("invalid start with " + data);
				return;
			}
			data = data.substring("bdvideo://play/".length());
			mCreateByIntentData = true;
			//<modify by sunjianshun 2012.11.22 BEGIN
			// 修改前
//			video = VideoFactory.create(false);
//			video.toNet().setType(NetVideo.NetVideoType.SERVER_SNIFFERED);
//			video.toNet().setUrl(UrlUtil.decode(data));
			// 修改后
			PlayListManager playListManager = (PlayListManager)mServiceFactory.getServiceProvider(PlayListManager.class);
			
			video = playListManager.findNetVideo(null, UrlUtil.decode(data));
			if (video == null) {
				video = VideoFactory.create(false);
				video.toNet().setType(NetVideo.NetVideoType.P2P_STREAM);
				video.toNet().setUrl(UrlUtil.decode(data));
			}
			//modify by sunjianshun 2012.11.22 END>
			
		} else {

			Bundle bundle = intent.getExtras();
			if (bundle == null) {
				logger.e("null extra");
				notifyError(ErrorCode.InvalidParam);
				return;
			}
			video = Video.fromBundle(bundle.getBundle(Const.IntentExtraKey.VideoVideo));
			
			album = Album.fromBundle(bundle.getBundle(Const.IntentExtraKey.VideoAlbum));
		}
		request(video, album);
	}

	public void create(Video video, Album album) {

		if (video == null) {
			logger.e("null video");
			notifyError(ErrorCode.InvalidParam);
			return;
		}
		request(video, album);
	}

	public void destroy(boolean hasError) {

		//mSnifferHandler.destroy();
		if(!hasError){
		mPlayListHandler.destroy();}
		mTaskHandler.destroy(waitArr);
		getEventCenter().removeListener(mEventListener);
		fireEvent(EventId.eStopPlay);
	}

	public Video getVideo() {
		return mVideo;
	}

	public Album getAlbum() {
		return mAlbum;
	}

	public Task getTask() {
		return mTaskHandler.getTask();
	}

	public String getTaskKey(){
		String res="";
		if(mTaskHandler.getTask()!=null){
			res=mTaskHandler.getTask().getKey();
		}else if(!mVideo.isLocal()){
			if(mVideo.toNet().isBdhd()) {
				res = mVideo.toNet().getUrl();
			} else {
				res = mVideo.toNet().getRefer();
			}
		}
		return res;
	}
	public void last() {
		Video v = mPlayListHandler.last();
		if (v != null) {
			NewPackage pack = new NewPackage(v, mAlbum);
			mHandler.sendMessage(mHandler.obtainMessage(NewMsg, pack));
		}
	}

	public void next() {
		Video v = mPlayListHandler.next();
		if (v != null) {
			NewPackage pack = new NewPackage(v, mAlbum);
			mHandler.sendMessage(mHandler.obtainMessage(NewMsg, pack));
		}
	}

	public boolean isCanLast() {
		return null != mPlayListHandler.last();
	}

	public boolean isCanNext() {
		return null != mPlayListHandler.next();
	}
	
	public boolean isCreateByIntentData() {
		return mCreateByIntentData;
	}

	public void playNewVideo(Video v) {
		NewPackage pack = new NewPackage(v, mAlbum);
		mHandler.sendMessage(mHandler.obtainMessage(NewMsg, pack));
	}

	public boolean download() {
		if (mVideo.isLocal()) {
			return false;
		}
		return mTaskHandler.download();
	}
	public boolean isDownLoading(){
		return mTaskHandler.isDownLoading();
	}
	public void cancleDownload(){
		mTaskHandler.setDownload(false);
	}
	public void setVideoDuration(int value) {
		mTaskHandler.setVideoDuration(value);
	}

	private void onSnifferComplete(boolean success, String url) {
		if (!success) {
			notifyError(ErrorCode.SnifferFail);
			return;
		}
		if ("".equals(url)) {
			notifyError(ErrorCode.SnifferFail);
			return;
		}
		mVideo.toNet().setUrl(url);
		notifySuccess(url);
	}

	private void onTaskComplete(String url, int errorCode) {
		if (errorCode != ErrorCode.None) {
			notifyError(errorCode);
		} else {
			notifySuccess(url);
			// 不需要对playlist做操作，都在stop时做
		}
	}

	private void request(Video video, Album album) {

		mVideo = mPlayListHandler.fromHolder(video);
		mAlbum = mPlayListHandler.fromHolder(album);
		mPlayListHandler.init();

		if (mVideo.isLocal()) {

			String path = mVideo.toLocal().getFullName();
			logger.d("want play local video " + path);
			if (StringUtil.isEmpty(path)) {
				notifyError(ErrorCode.InvalidPath);
				return;
			}
			if (checkSDCard()) {
				if (!SDCardUtil.getInstance().isFileExist(path)) {
					notifyError(ErrorCode.InvalidPath);
				}
			}
			else {
				return;
			}
			notifySuccess("file://" + path);
		} else {
			
			// <add by sunjianshun 2012.11.14 BEGIN
			PlayListManager playListManager = (PlayListManager)mServiceFactory.getServiceProvider(PlayListManager.class);
			if (mAlbum != null) {
				mAlbum.setVideos(playListManager.getNetVideos(album.getId(), album.getListId(), album.getRefer()));
			}
			// add by sunjianshun 2012.11.14 END>

			NetVideo netVideo = mVideo.toNet();
			logger.d("want play net video " + netVideo.getRefer() + " " + netVideo.getUrl());
			mTaskHandler.request(netVideo);

			if (!checkSDCard()) {
				return;
			}
			if (!checkNet()) {
				return;
			}
		}
		fireEvent(EventId.eStartPlay);
	}
	
	private void startPlay() {
		if (mTaskHandler.getState() == TaskHandler.State.BigSiteStream) {
			// TODO 提高性能，付出的代价是如果嗅探失败，就永远失败了，因为着东西写到db了
			notifySuccess(mVideo.toNet().getUrl());
//			if (StringUtil.isEmpty(mVideo.toNet().getUrl())) {
//				mSnifferHandler.request(mVideo.toNet().getRefer());
//				mCallback.onSniffer();
//			} else {
//				
//			}
		}
		mTaskHandler.startTask();
		getEventCenter().addListener(EventId.eNetStateChanged,
				mEventListener);
		fireEvent(EventId.eStartPlay);
	}

	private boolean checkSDCard() {
		if (!needSDCard()) {
			return true;
		}
		Detect detect = getDetect();
		if (!detect.isSDCardAvailable()) {
			detect.SDCardPrompt();
			notifyError(ErrorCode.SDCardNotUseable);
			return false;
		}
		getEventCenter().addListener(EventId.eSDCardStateChanged,
				mEventListener);
		return true;
	}

	private boolean checkNet() {
		if (!needNet()) {
			return true;
		}
		getDetect().filterByNet(new FilterCallback() {

			@Override
			public void onDetectPromptReturn(
					DetectPromptReturn detectPromptReturn) {

				if (DetectPromptReturn.eTrue == detectPromptReturn) {
					startPlay();
				} else {
					notifyError(ErrorCode.NetNotUseable);
				}
			}
		});
		return false;
	}

	/** 是否需要网络 */
	private boolean needNet() {
		if(		mTaskHandler.getState() == State.BigSiteStream ||
				mTaskHandler.getState() == State.BigSitePart ||
				mTaskHandler.getState() == State.SmallSiteStream ||
				mTaskHandler.getState() == State.SmallSitePart) {
			return true;
		}
		return false;
	}
	
	/** 是否需要SDCard */
	private boolean needSDCard() {
		if(		mVideo.isLocal() || 
				mTaskHandler.getState() == State.BigSiteLocal || 
				mTaskHandler.getState() == State.BigSitePart ||
				mTaskHandler.getState() == State.SmallSiteLocal || 
				mTaskHandler.getState() == State.SmallSitePart) {
			return true;
		}
		return false;
	}
	
	private void notifySuccess(String url) {
		mHandler.sendMessage(mHandler.obtainMessage(PlayMsg, url));
	}

	private void notifyError(int errorCode) {
		mHandler.sendMessage(mHandler.obtainMessage(ErrorMsg, errorCode, 0));
	}

	private void fireEvent(EventId id) {
		getEventCenter().fireEvent(id,
				new PlayerEventArgs(mVideo, mTaskHandler.getTask()));
	}

	private EventCenter getEventCenter() {
		return (EventCenter) mServiceFactory
				.getServiceProvider(EventCenter.class);
	}
	
	private Detect getDetect() {
		return (Detect) mServiceFactory
				.getServiceProvider(Detect.class);
	}

	private class NewPackage {
		Video video = null;
		Album album = null;

		public NewPackage(Video v, Album a) {
			video = v;
			album = a;
		}
	}
}
