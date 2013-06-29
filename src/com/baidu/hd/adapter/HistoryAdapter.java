package com.baidu.hd.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.hd.BrowserSpecActivity;
import com.baidu.hd.db.DBWriter;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.image.ImageManager;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.HistoryItemPackage;
import com.baidu.hd.module.Image;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

/**
 * ListView适配器
 */
public class HistoryAdapter extends EditableAdapter<HistoryItemPackage> {
	Logger logger = new Logger("HistoryAdapter");
	
	/** 筛选状态 */
//	private FilterType mFilterType = FilterType.SHOW_ALL;
	
	private EventListener mEventListener = new EventListener()
	{
		@Override
		public void onEvent(EventId id, EventArgs args)
		{
			logger.d("onEvent");
			
			if (id == EventId.eImageNeedReload)
			{
				notifyDataSetChanged();
			}
			else if (id == EventId.ePlayListUpdate)
			{
				logger.d("ePlayListUpdate");
				fillList();
				notifyDataSetChanged();
			}
		}
	};

	private final class ViewHolder
	{
		public ImageView commonTaskDelete = null;
		public ImageView commonTaskThumbnail = null;
		public TextView commonTaskName = null;
		public TextView commonTaskUpdateInfo = null;
		public TextView commonTaskPlayTime = null;
		public LinearLayout commonTaskRightLayout = null;
		public ImageView commonTaskLeftNew = null;
	}

	public HistoryAdapter(Context context, Callback callback)
	{
		super(context, callback);
	}

	/**
	 * 修改逻辑，加入是否隐藏判断
	 */
	public void fillList()
	{
		logger.d("fillList");
		
		PlayListManager playListManager = (PlayListManager) mBaseActivity.getServiceProvider(PlayListManager.class);
		List<Album> albums = playListManager.getAllPersonalHistoryAlbums();
		logger.d("personalHistory Alubms = " + ((Integer)albums.size()).toString());

		mItems.clear();
		mAllItemNum = 0;
		for (Album album : albums)
		{
//			if (mFilterType == FilterType.SHOW_ALL || // 全部显示
//				mFilterType == FilterType.SHOW_MULTIPLE && !album.isSingleAlbum() || // 只显示剧集
//				mFilterType == FilterType.SHOW_SINGLE && album.isSingleAlbum() ) { // 只显示单集
				
				HistoryItemPackage item = new HistoryItemPackage();
				item.setAlbum(album);
				mItems.add(item);
				mAllItemNum++;
//			}			
		}
		// 0表示disabled；1表示enabled
		// 此处不涉及图标，只涉及可用于不可用
		notifyEditButton(mAllItemNum == 0 ? 0 : 1);
	}

	@Override
	public void onServiceCreated()
	{
	}
	
	@Override
	public void onResume()
	{
		if (!isServiceCreated()) return;
		getEventCenter().addListener(EventId.eImageNeedReload, mEventListener);
		getEventCenter().addListener(EventId.ePlayListUpdate, mEventListener);
	}

	@Override
	public void onPause()
	{
		if (!isServiceCreated()) return;
		getEventCenter().removeListener(mEventListener);
	}

	@Override
	public void removeMarkDeleteItem()
	{
		if (!isServiceCreated()) return;
		PlayListManager playListManager = (PlayListManager) mBaseActivity.getServiceProvider(PlayListManager.class);
		for (HistoryItemPackage item : mItems)
		{
			if (item.isSelectedDel())
			{
				playListManager.removePersonalHistoryAlbum(item.getAlbum());
			}
		}
	}
	
	/**
	 * 设置筛选剧集类型
	 */
//	public void setFilterType(FilterType filterType) {
//		mFilterType = filterType;
//	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder = null;
		final HistoryItemPackage item = mItems.get(position);
		final Album album = item.getAlbum();

		if (null == convertView)
		{
			viewHolder = new ViewHolder();

			convertView = mInflater.inflate(R.layout.baiduplayer_history_item, null);
			viewHolder.commonTaskDelete = (ImageView) convertView.findViewById(R.id.history_item_delete);
			viewHolder.commonTaskThumbnail = (ImageView) convertView.findViewById(R.id.history_item_thumbnail);
			viewHolder.commonTaskName = (TextView) convertView.findViewById(R.id.history_item_title);
			viewHolder.commonTaskPlayTime = (TextView) convertView.findViewById(R.id.history_item_title_info);
			viewHolder.commonTaskUpdateInfo = (TextView) convertView.findViewById(R.id.history_item_submit_left_info);
			viewHolder.commonTaskRightLayout = (LinearLayout) convertView.findViewById(R.id.history_item_rigth_layout);
			viewHolder.commonTaskLeftNew = (ImageView) convertView.findViewById(R.id.history_item_submit_left_info_icon);
			convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.commonTaskRightLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickLayoutRight(position);
			}
		});

//		Drawable drawable = null;
		Drawable drawable =  ((ImageManager) mBaseActivity.getServiceProvider(ImageManager.class)).request(album.getImage(), Image.Type.Album);
		if (drawable == null) {
			drawable = mBaseActivity.getResources().getDrawable(R.drawable.album);
		}
		viewHolder.commonTaskThumbnail.setImageDrawable(drawable);

		// delete icon
		viewHolder.commonTaskDelete.setVisibility(mIsEditComplete ? View.VISIBLE : View.GONE);
		viewHolder.commonTaskDelete.setImageResource(item.isSelectedDel() ? R.drawable.ic_list_check_on : R.drawable.ic_list_check_off);

		/** 将隐藏列表icon，修改为隐藏右边整个layout */
		// status icon
	//	viewHolder.commonTaskStatus.setVisibility(!mIsEditCancel ? View.VISIBLE : View.GONE);
		// right layout
	//	viewHolder.commonTaskRightLayout.setVisibility(!mIsEditCancel ? View.VISIBLE : View.GONE);

		// name
		viewHolder.commonTaskName.setText(album.getListName());

		// update info
		viewHolder.commonTaskLeftNew.setVisibility(View.GONE);
		if (album.isFinished())
		{
			viewHolder.commonTaskUpdateInfo.setText(R.string.history_finished);
		}
		else
		{
			String format = mContext.getString(R.string.history_update_video_count);
			PlayListManager playListManager = (PlayListManager) mBaseActivity.getServiceProvider(PlayListManager.class);
			int count = playListManager.getNetVideos(album.getId(), album.getListId(), album.getRefer()).size();
			viewHolder.commonTaskUpdateInfo.setText(String.format(format, count));
		}

		// play info
		if (album.getCurrent() != null)
		{
			// 显示列表按钮
			viewHolder.commonTaskRightLayout.setVisibility(!mIsEditComplete ? View.VISIBLE : View.GONE);
			
			// 显示更新信息
			viewHolder.commonTaskUpdateInfo.setVisibility(View.VISIBLE);
			
			// 修改缩略图的缩放
			viewHolder.commonTaskThumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
			
			NetVideo video = album.getCurrent();
//			viewHolder.commonTaskPlayTime.setText(String.format(watchTo + " %s", StringUtil.getCurrentVideoIndex(this.mBaseActivity,album), StringUtil.formatTime(video.getPosition())));
			
			String watchTo = "";
			if (album.asBigServerAlbum() != null) {
				watchTo = mContext.getString(R.string.history_watch_to_normal);
				viewHolder.commonTaskPlayTime.setText(String.format(watchTo + " %s", video.getName(), StringUtil.formatTime(video.getPosition())));
			}
			else {
				watchTo = mContext.getString(R.string.history_watch_to_album);
				viewHolder.commonTaskPlayTime.setText(String.format(watchTo + " %s", video.getEpisode(), StringUtil.formatTime(video.getPosition())));
			}
		}
		convertView.findViewById(R.id.history_item_center_layout).setBackgroundDrawable(null);
		convertView.findViewById(R.id.history_item_submit).setBackgroundDrawable(null);
		convertView.findViewById(R.id.history_item_submit_left_info).setBackgroundDrawable(null);
		
		return convertView;
	}
	
	public void onClickLayoutRight(int pos) {
		Album album = mItems.get(pos).getAlbum();
		
		if (!mIsEditComplete)
		{
			DBWriter dbWriter = (DBWriter)mBaseActivity.getServiceProvider(DBWriter.class);
			dbWriter.modifyAlbum(album, DBWriter.Action.Update);
			getStat().incLogCount(StatId.HistorySpec);
//			MainActivity.setOnPauseAnimation(true);
			Intent intent = new Intent(mBaseActivity, BrowserSpecActivity.class);
			intent.putExtra(Const.IntentExtraKey.BrowSpecAlbumId, album.getId());
			intent.putExtra(Const.IntentExtraKey.BrowSpecIndex, album.getCurrent().getEpisode());
			mBaseActivity.startActivity(intent);
		}
	}

	private Stat getStat()
	{
		return (Stat) mBaseActivity.getServiceProvider(Stat.class);
	}
	
}
