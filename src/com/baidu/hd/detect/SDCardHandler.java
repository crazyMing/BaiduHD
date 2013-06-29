package com.baidu.hd.detect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.ctrl.PopupDialog;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.log.Logger;
import com.baidu.hd.R;

class SDCardHandler {

	private Logger logger = new Logger("SDCardHandler");

	private boolean canUse = false;

	private BroadcastReceiver mSDCardStateReceiver = null;
	private Context mContext = null;

	public boolean canUse() {
		return this.canUse;
	}
	
	public void create(Context context) {
		this.mContext = context;

		this.mSDCardStateReceiver = new BroadcastReceiver() {
			public void onReceive(Context paramContext, Intent paramIntent) {

				String str = paramIntent.getAction();
				logger.d("SD Card state action: " + str);
				boolean oldCanUse = canUse;
				setState();
				if(oldCanUse != canUse) {
					fireEvent();
				}
			}
		};
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		//intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		//intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
		//intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
		//intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		//intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		//intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentFilter.addDataScheme("file");
		this.mContext.registerReceiver(mSDCardStateReceiver, intentFilter);
		this.setState();
	}

	public void destroy() {
		this.mContext.unregisterReceiver(this.mSDCardStateReceiver);
	}

	private void fireEvent() {
		EventCenter eventCenter = (EventCenter) getApplication()
				.getServiceFactory().getServiceProvider(EventCenter.class);
		eventCenter.fireEvent(EventId.eSDCardStateChanged,
				new SDCardStateChangedEventArgs(this.canUse));
	}

	private void setState() {
		try {
			this.canUse = Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void prompt() {
		PopupDialog dialog = new PopupDialog(getApplication()
				.getCurrentActivity(), null);
		dialog.setTitle(dialog.createText(R.string.exit_dialog_title))
				.setMessage(dialog.createText(R.string.dialog_sdcard_message))
				.setPositiveButton(dialog.createText(R.string.ok)).show();
	}

	private BaiduHD getApplication() {
		return BaiduHD.cast(mContext);
	}
}
