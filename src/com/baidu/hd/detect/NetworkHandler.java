package com.baidu.hd.detect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.BaseActivity;
import com.baidu.hd.ctrl.PopupDialog;
import com.baidu.hd.ctrl.PopupDialog.ReturnType;
import com.baidu.hd.detect.FilterCallback.DetectPromptReturn;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.log.Logger;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.util.PlayerTools;
import com.baidu.hd.R;

class NetworkHandler {

	private enum NetState {
		eNone,
		eWifi,
		eNoWifi,
	}
	
	private Logger logger = new Logger("NetworkHandler");

	/** ��ǰ����״�� */
	private NetState mState = NetState.eNone;

	private Context mContext = null;
	private BroadcastReceiver mNetWorkStateReceiver = null;

	public void create(Context context) {
		this.mContext = context;

		this.mNetWorkStateReceiver = new BroadcastReceiver() {
			public void onReceive(Context paramContext, Intent paramIntent) {

				String str = paramIntent.getAction();
				logger.d("Net work state action: " + str);
				onNetChanged();
			}
		};
		IntentFilter networkIntentFilter = new IntentFilter(
				"android.net.conn.CONNECTIVITY_CHANGE");
		this.mContext.registerReceiver(mNetWorkStateReceiver,
				networkIntentFilter);

		// ��ʼ������һ��
		mState = getState();

		// ͳ��
		Stat stat = (Stat) BaiduHD.cast(mContext).getServiceFactory().getServiceProvider(Stat.class);
		switch(mState) {
		case eNone:
			stat.setNetState(Stat.NetState.eDisable);
			break;
		case eWifi:
			stat.setNetState(Stat.NetState.eWifi);
			break;
		case eNoWifi:
			stat.setNetState(Stat.NetState.eNoWifi);
			break;
		}
	}

	public void destroy() {
		this.mContext.unregisterReceiver(this.mNetWorkStateReceiver);
	}
	
	public NetUsable getUsable() {
		return getUsable(mState);
	}

	public boolean isAvailable() {
		return mState == NetState.eWifi || mState == NetState.eNoWifi;
	}
	
	public boolean isAvailableWithWifi() {
		return mState == NetState.eWifi;
	}

	public boolean isPrompt() {
		return mContext.getSharedPreferences(DetectConst.FileName,
				Context.MODE_WORLD_READABLE).getBoolean(DetectConst.NoWifiPrompt, true);
	}
	
	public void setPrompt(boolean value) {
		mContext.getSharedPreferences(DetectConst.FileName, 
				Context.MODE_WORLD_WRITEABLE).edit().putBoolean(DetectConst.NoWifiPrompt, value).commit();
		if(value && mState == NetState.eNoWifi) {
			fireEvent(NetUsable.eEnable, NetUsable.ePrompt);
		}
	}

	public void filter(FilterCallback callback) {
		if(callback == null) {
			callback = new EmptyFilterCallback();
		}
		switch(mState) {
		case eNone:
			callback.onDetectPromptReturn(DetectPromptReturn.eNoNetAvailable);
			break;
		case eNoWifi:
			if(isPrompt()) {
				showDialog(callback);
			} else {
				callback.onDetectPromptReturn(DetectPromptReturn.eTrue);
			}
			break;
		case eWifi:
			callback.onDetectPromptReturn(DetectPromptReturn.eTrue);
			break;
		}
	}

	private void onNetChanged() {
		
		NetState oldState = mState;
		NetState newState = getState();
		
		if(newState == oldState) {
			return;
		}
		mState = newState;

		NetUsable oldValue = getUsable(oldState);
		NetUsable newValue = getUsable(newState);
		if(oldState != newState) {
			fireEvent(oldValue, newValue);
		}
	}

	private void showDialog(final FilterCallback callback) {
		
		BaiduHD app = (BaiduHD) this.mContext;
		BaseActivity act = app.getCurrentActivity();
		Activity context = act;
		if (act.isChild()) {
			context = act.getParent();
		}
		
		PopupDialog dialog = new PopupDialog(context, new PopupDialog.Callback() {
			
					@Override
					public void onReturn(ReturnType type, boolean checked) {
						
						boolean isOK = (type == PopupDialog.ReturnType.OK);
						callback.onDetectPromptReturn(isOK ? DetectPromptReturn.eTrue : DetectPromptReturn.eFalse);
					}
				});
		dialog.setTitle(dialog.createText(R.string.exit_dialog_title))
				.setMessage(dialog.createText(R.string.dialog_3g_message))
				.setPositiveButton(dialog.createText(R.string.ok))
				.setNegativeButton(dialog.createText(R.string.cancel))
				.show();
	}

	private NetState getState() {

		switch (PlayerTools.getNetWorkConnected(this.mContext)) {
		case 0:
			if (PlayerTools.isNetworkConnected(this.mContext,
					PlayerTools.NetworkType.NETWORK_TYPE_GPRS)) {
				this.logger.d("GPRS network connected");
			} else if (PlayerTools.isNetworkConnected(this.mContext,
					PlayerTools.NetworkType.NETWORK_TYPE_3G)) {
				this.logger.d("3G network connected");
			} else if (PlayerTools.isNetworkConnected(this.mContext,
					PlayerTools.NetworkType.NETWORK_TYPE_4G)) {
				this.logger.d("4G network connected");
			} else {
				this.logger.d("Unknow network connected (mobile)");
			}
			return NetState.eNoWifi;

		case 1:
			if (PlayerTools.isNetworkConnected(this.mContext,
					PlayerTools.NetworkType.NETWORK_TYPE_WIFI)) {
				this.logger.d("WiFi network connected");
			} else {
				this.logger.d("Unknow network connected (wifi)");
			}
			return NetState.eWifi;

		default:
			return NetState.eNone;
		}
	}
	
	private void fireEvent(NetUsable oldValue, NetUsable newValue) {
		EventCenter eventCenter = (EventCenter) BaiduHD.cast(mContext).getServiceFactory().getServiceProvider(EventCenter.class);
		eventCenter.fireEvent(EventId.eNetStateChanged,	
				new NetUsableChangedEventArgs(oldValue, newValue));	
	}

	private NetUsable getUsable(NetState state) {
		switch(state) {
		case eNone:
			return NetUsable.eDisable;
		case eWifi:
			return NetUsable.eEnable;
		case eNoWifi:
			if(isPrompt()) {
				return NetUsable.ePrompt;
			} else {
				return NetUsable.eEnable;
			}
		default:
			return NetUsable.eDisable;
		}
	}
}
