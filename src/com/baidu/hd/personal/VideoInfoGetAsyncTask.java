package com.baidu.hd.personal;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.module.album.LocalVideo;
import com.baidu.hd.player.CyberPlayerHolder;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.video.CyberPlayer;

public class VideoInfoGetAsyncTask extends AsyncTask<Void, Void, Void>
{
	public interface Callback {
		void onUpdate();
	}
	
	private Callback mCallback = null;
	private Context mContext = null;
	
	public VideoInfoGetAsyncTask(Context context, Callback callback) {		
		mContext = context;
		mCallback = callback;
		
	}

	@Override
	protected void onCancelled() {
		getHolder().setUsing(false);
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(Void result) {
		getHolder().setUsing(false);
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		getHolder().setUsing(true);
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		CyberPlayer player = getHolder().getPlayer();
		if(player == null) {
			return null;
		}

		PlayListManager playListManager = (PlayListManager)((BaseActivity)mContext).getServiceProvider(PlayListManager.class);
		List<LocalVideo> videos = playListManager.getAllLocal();

		synchronized (videos) {
			
			for (LocalVideo v : videos)
			{
				boolean modified = false;
				if (v.getDuration() == 0)
				{
					v.setDuration(CyberPlayer.getDurationForFile(v.getFullName()));
					modified = true;
				}
				if (v.getTotalSize() == 0)
				{
					v.setTotalSize(new File(v.getFullName()).length());
					modified = true;
				}
				if (modified)
				{
					playListManager.updateLocal(v);
					this.publishProgress();
				}
				if (this.isCancelled())
				{
					break;
				}
			}
			return null;
		}
	}

	@Override
	protected void onProgressUpdate(Void... values)
	{
		mCallback.onUpdate();
	}
	
	private CyberPlayerHolder getHolder() {
		return (CyberPlayerHolder)((BaseActivity)mContext).getServiceProvider(CyberPlayerHolder.class);
	}
}