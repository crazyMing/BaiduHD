package com.baidu.hd.player;

import android.content.Context;
import android.content.Intent;

import com.baidu.hd.log.Logger;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.Video;
import com.baidu.hd.module.album.VideoFactory;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.StringUtil;

public class PlayerLauncher {
	static Logger log = new Logger(PlayerLauncher.class.getSimpleName());

	public static void startup(Context context, Album album) {
		startup(context, album, album.getCurrent());
	}

	public static void startup(Context context, Video video) {
		startup(context, null, video);
	}

	public static void startup(Context context, Album album, Video video) {
		try{
			if(video.toNet()!=null){
				if(video.toNet().getUrl()!=null){
					if(video.getName()==null||video.getName().equals("")){
						video.setName(StringUtil.getNameForUrl(video.toNet().getUrl()));
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		Intent intent = new Intent();
		intent.setClass(context, PlayerActivity.class);
		intent.putExtra(Const.IntentExtraKey.VideoAlbum, album == null ? null
				: album.toBundle());
		intent.putExtra(Const.IntentExtraKey.VideoVideo, video == null ? null
				: video.toBundle());
		try {
			log.d(album.getVideos().size() + ":" + video.getName());
		} catch (Exception e) {
			log.d("album null " + (album == null));
			log.d("video null " + (video == null));
		}
		context.startActivity(intent);
	}

	public static void startup(Context context, String bdhd) {
		NetVideo video = VideoFactory.create(false).toNet();
		video.setType(NetVideo.NetVideoType.P2P_STREAM);
		video.setName(StringUtil.getNameForUrl(bdhd));
		video.setUrl(bdhd);
		startup(context, video);
	}

}
