package com.baidu.hd.player;

import android.widget.ImageButton;

import com.baidu.hd.util.SystemUtil;
import com.baidu.hd.util.Turple;
import com.baidu.hd.R;


class SizeChanger {
	
	private PlayerAccessor mAccessor = null;

	private boolean isFullScreen = true;
	
	private int mVideoWidth = 0;
	private int mVideoHeight = 0;
	private int mScreenWidth = 0;
	private int mScreenHeight = 0;
	
	private boolean mVideoSizeSetted = false;
	
	private PlayerCore mPlayerCore = null;
	
	public SizeChanger(PlayerAccessor accessor, PlayerCore playerCore) {
		
		this.mAccessor = accessor;
		this.mPlayerCore = playerCore;
		
		this.setImage();
		
		Turple<Integer, Integer> value = SystemUtil.getResolution(mAccessor.getHost());
		this.mScreenWidth = value.getX();
		this.mScreenHeight = value.getY();
	}
 
	public void setVideoSize() {
		this.mVideoWidth = this.mPlayerCore.getVideoWidth();
		this.mVideoHeight = this.mPlayerCore.getVideoHeight();
		this.mVideoSizeSetted = true;
	}
	
	public Turple<Integer, Integer> getVideoSize() {
		return new Turple<Integer, Integer>(mVideoWidth, mVideoHeight);
	}
	
	public Turple<Integer, Integer> getScreenSize() {
		return new Turple<Integer, Integer>(mScreenWidth, mScreenHeight);
	}
	
	public void change() {
		
		if(this.mVideoSizeSetted) {
			
			boolean result  = this.mPlayerCore.setVideoSize(
					this.isFullScreen ? this.mVideoWidth : this.mScreenWidth, 
					this.isFullScreen ? this.mVideoHeight : this.mScreenHeight);
			if(result) {
				this.isFullScreen = !this.isFullScreen;
				this.setImage();
			}
		}
	}

	private void setImage() {
		final ImageButton btnFullScreen = (ImageButton) this.mAccessor.getHost().findViewById(R.id.btn_fullscreen);
		btnFullScreen.setImageResource(this.isFullScreen ? R.drawable.ic_zoom_out_btn_videoplayer_style: R.drawable.ic_zoom_in_btn_videoplayer_style);
	}
}
