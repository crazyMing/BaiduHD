package com.baidu.hd.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

/**
 * @author LEIKANG
 */
public class HomeRecentAdapter extends BaseAdapter{
	
	Logger logger = new Logger("HomeRecentAdapter");
	
	private int mMaxColumns;
	private int mCount = 0;
	private List<Album> albums;
	protected LayoutInflater mInflater = null;
	private Context mContext;
	
	public HomeRecentAdapter(Context context, int maxcolumns) {
		albums = ((PlayListManager)((BaseActivity)context).getServiceProvider(PlayListManager.class)).getHomeShowAlbums();
		mCount = albums.size();
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mMaxColumns = maxcolumns;
		EventCenter ec = (EventCenter)((BaseActivity)context).getServiceProvider(EventCenter.class);
		ec.addListener(mEventListener);
	}

	@Override
	public int getCount() {
		if (mCount>mMaxColumns)
			return mMaxColumns;
		else
			return mCount;
	}

	@Override
	public Object getItem(int position) {
		return albums.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		final Album album = albums.get(position);
		if (null == convertView) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.homerecent_grid_item, null);
			viewHolder.albumName = (TextView) convertView.findViewById(R.id.album_name);
			viewHolder.albumTaskTime = (TextView) convertView.findViewById(R.id.album_palying_time);
			convertView.setTag(viewHolder);
		}
		else
			viewHolder = (ViewHolder) convertView.getTag();
		
		String name = album.getListName();
		
		if (album.getCurrent() != null)
		{
			name = album.getCurrent().getName();
			if (name.lastIndexOf(".") != -1)
				name = name.substring(0, name.lastIndexOf("."));
			String watchTo = mContext.getString(R.string.history_task_time_text);
			viewHolder.albumTaskTime.setText(String.format(watchTo,StringUtil.formatTime(album.getCurrent().getPosition())));
		}
		viewHolder.albumName.setText(name);
		
		return convertView;
	}

	public final class ViewHolder
	{
		public TextView albumName = null;
		public TextView albumTaskTime = null;
	}
	
	private EventListener mEventListener = new EventListener()
	{
		@Override
		public void onEvent(EventId id, EventArgs args)
		{
			albums = ((PlayListManager)((BaseActivity)mContext).getServiceProvider(PlayListManager.class)).getHomeShowAlbums();
			mCount = albums.size();
			notifyDataSetChanged();
		}
	};
	
	
}
