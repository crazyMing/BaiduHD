package com.baidu.hd.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.Video;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

/**
 * 播放器播放列表
 */
public class PlayerPlayListAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater = null;
	private List<NetVideo> mVideos = new ArrayList<NetVideo>();
	private Video mVideo = null;
	private Album mAlbum;

	private final class ViewHolder
	{
		TextView txtName;
		ImageView imageView;
	}
	
	public PlayerPlayListAdapter(Context context)
	{
		this.mInflater = LayoutInflater.from(context);
	}
	
	public void click(int position) {
		
	}

	@Override
	public int getCount() {
		return this.mVideos.size();
	}

	@Override
	public Object getItem(int position) {
		return this.mVideos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return this.mVideos.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder = null;
		
		if (null == convertView)
		{
			viewHolder = new ViewHolder();

			convertView = mInflater.inflate(R.layout.player_playlist_item, null);
			viewHolder.txtName = (TextView) convertView.findViewById(R.id.video_name);
			viewHolder.imageView = (ImageView)convertView.findViewById(R.id.player_playlist_play_image);
			convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final NetVideo netVideo = this.mVideos.get(position).toNet();
		String name=StringUtil.getVideoName(netVideo,mAlbum);
		if(name.contains(".")){
			name=(position+1)+"-"+name.substring(0,name.lastIndexOf("."));
		}else{
			name=(position+1)+"-"+name;
		}
		viewHolder.txtName.setText(name);
		
		if (netVideo.getRefer().equals(mVideo.toNet().getRefer())) {
			
			viewHolder.imageView.setVisibility(View.VISIBLE);
			viewHolder.txtName.setTextColor(0xff058bfc);
		}
		else {
			viewHolder.imageView.setVisibility(View.INVISIBLE);
			viewHolder.txtName.setTextColor(0xff929292);
		}
		
		return convertView;

	}

	public void fillList(Album album) {
		this.mAlbum=album;
		this.mVideos = album.getVideos();
	}
	
	public void getCurrentVideo(Video video) {
		mVideo = video;
	}
}
