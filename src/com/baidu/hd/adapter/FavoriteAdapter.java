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
import com.baidu.hd.module.HistoryItemPackage;
import com.baidu.hd.module.Image;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.Video;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

public class FavoriteAdapter extends EditableAdapter<HistoryItemPackage> {

	private EventListener mEventListener = new EventListener() {
		@Override
		public void onEvent(EventId id, EventArgs args) {
			if (id == EventId.eImageNeedReload) {
				notifyDataSetChanged();
			}
			else if (id == EventId.ePlayListUpdate) {				
				fillList();
				notifyDataSetChanged();
			}
		}
	};

	private final class ViewHolder {
		public ImageView imageSelected = null;
		public ImageView imageThumbnail = null;
		public TextView textViewName = null;
		public TextView textViewUpdateInfo = null;
		public TextView textViewPlayTime = null;
		public LinearLayout layoutRight = null;
		public ImageView imageNew = null;
	}
	
	public FavoriteAdapter(Context context, Callback callback) {
		super(context, callback);
	}

	@Override
	public void removeMarkDeleteItem() {
		if (!isServiceCreated()) return;
		PlayListManager playListManager = (PlayListManager) mBaseActivity.getServiceProvider(PlayListManager.class);
		for (HistoryItemPackage item : mItems) {
			if (item.isSelectedDel()) {
				playListManager.removeFavoriteAlbum(item.getAlbum());
			}
		}
		
		fillList();
	}

	@Override
	public void onResume() {
		if (!isServiceCreated()) return;
		getEventCenter().addListener(EventId.eImageNeedReload, mEventListener);
		getEventCenter().addListener(EventId.ePlayListUpdate, mEventListener);
		fillList();
		notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		if (!isServiceCreated()) return;
		getEventCenter().removeListener(mEventListener);
	}

	@Override
	public void onServiceCreated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.baiduplayer_history_item, null);
			viewHolder = new ViewHolder();
			viewHolder.imageSelected = (ImageView)convertView.findViewById(R.id.history_item_delete);
			viewHolder.imageThumbnail = (ImageView)convertView.findViewById(R.id.history_item_thumbnail);
			viewHolder.layoutRight = (LinearLayout)convertView.findViewById(R.id.history_item_rigth_layout);
			viewHolder.imageNew = (ImageView)convertView.findViewById(R.id.history_item_submit_left_info_icon);
			viewHolder.textViewName = (TextView)convertView.findViewById(R.id.history_item_title);
			viewHolder.textViewPlayTime = (TextView)convertView.findViewById(R.id.history_item_title_info);
			viewHolder.textViewUpdateInfo = (TextView)convertView.findViewById(R.id.history_item_submit_left_info);
			
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		// 设置相关信息
		initViewHolder(viewHolder, position);
		return convertView;
	}
	
	private void initViewHolder(ViewHolder viewHolder, int position) {
		initSelectedIcon(viewHolder, position);
		initThumbnail(viewHolder, position);
		initRightLayout(viewHolder, position);
		initNewIcon(viewHolder, position);
		initAlbumName(viewHolder, position);
		initAlbumPlayTime(viewHolder, position);
		initAlbumUpdateInfo(viewHolder, position);
	}
	
	private void initSelectedIcon(ViewHolder viewHolder, int position) {
		viewHolder.imageSelected.setVisibility(mIsEditComplete ? View.VISIBLE : View.GONE);
		viewHolder.imageSelected.setImageResource(mItems.get(position).isSelectedDel() ? R.drawable.ic_list_check_on : R.drawable.ic_list_check_off);
	}
	
	private void initThumbnail(ViewHolder viewHolder, int position) {
		Album album = mItems.get(position).getAlbum();
		
		Drawable drawable =  ((ImageManager) mBaseActivity.getServiceProvider(ImageManager.class)).request(album.getImage(), Image.Type.Album);
		if (drawable == null) {
			mBaseActivity.getResources().getDrawable(R.drawable.album);
		}
		viewHolder.imageThumbnail.setImageDrawable(drawable);
		viewHolder.imageThumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
	}
	
	private void initRightLayout(ViewHolder viewHolder, final int position) {
		final Album album = mItems.get(position).getAlbum();
		if (mIsEditComplete) {
			viewHolder.layoutRight.setVisibility(View.GONE);
			return;
		}
		
		viewHolder.layoutRight.setVisibility(View.VISIBLE);
		viewHolder.layoutRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickLayoutRight(position);
			}
		});
	}
	
	private void initNewIcon(ViewHolder viewHolder, int position) {
		Album album = mItems.get(position).getAlbum();
		if (album.isFinished()) {
			viewHolder.imageNew.setVisibility(View.GONE);
		}
		else if (album.isHaveNew()) {
			viewHolder.imageNew.setVisibility(View.VISIBLE);
		} else {
			viewHolder.imageNew.setVisibility(View.GONE);
		}
	}
	
	private void initAlbumName(ViewHolder viewHolder, int position) {
		Album album = mItems.get(position).getAlbum();
		viewHolder.textViewName.setText(album.getListName());
	}
	
	private void initAlbumPlayTime(ViewHolder viewHolder, int position) {
		Album album = mItems.get(position).getAlbum();
		
		NetVideo video = album.getCurrent();
		if (video == null) {
			viewHolder.textViewPlayTime.setText(mContext.getString(R.string.personal_non_watch_info));
		}
		else {
			String watchTo = "";
			
			if (album.asBigServerAlbum() != null) {
				watchTo = mContext.getString(R.string.history_watch_to_normal);
				viewHolder.textViewPlayTime.setText(String.format(watchTo + " %s", video.getName(), StringUtil.formatTime(video.getPosition())));
			}
			else {
				watchTo = mContext.getString(R.string.history_watch_to_album);
				viewHolder.textViewPlayTime.setText(String.format(watchTo + " %s", video.getEpisode(), StringUtil.formatTime(video.getPosition())));
			}
		}
	}
	
	private void initAlbumUpdateInfo(ViewHolder viewHolder, int position) {
		Album album = mItems.get(position).getAlbum();
		viewHolder.textViewUpdateInfo.setVisibility(View.VISIBLE);

		if (album.isFinished()) {
			viewHolder.textViewUpdateInfo.setText(R.string.history_finished);
		} else {
			
			String format = "";
			
			if (!album.isHaveNew()) {
				format = mContext.getString(R.string.personal_non_update_info);
			}
			else {
				format = mContext.getString(R.string.history_update_to_normal);
				format = mContext.getString(R.string.history_update_newest_count);
			}
			
			viewHolder.textViewUpdateInfo.setText(String.format(format, album.getNewestCount()));
		}
		Drawable icon = mContext.getResources().getDrawable(R.drawable.ic_favorite_item);
		icon.setBounds(0, 0, icon.getMinimumWidth(), icon.getMinimumHeight()); 
		viewHolder.textViewUpdateInfo.setCompoundDrawables(icon, null, null, null);
	}
	
	public void fillList() {
		PlayListManager playListManager = (PlayListManager) mBaseActivity.getServiceProvider(PlayListManager.class);
		List<Album> albums = playListManager.getAllFavoriteAlbums();
		
		mItems.clear();
		mAllItemNum = 0;
		for (Album album : albums) {
			HistoryItemPackage item = new HistoryItemPackage();
			item.setAlbum(album);
			mItems.add(item);
			mAllItemNum++;
		}
		notifyEditButton(mAllItemNum == 0 ? 0 : 1);
	}

	public void onClickLayoutRight(int pos) {
		Album album = mItems.get(pos).getAlbum();
		if (!mIsEditComplete) {
			album.setHaveNew(false);
			album.setNewestCount(0);
			DBWriter dbWriter = (DBWriter)mBaseActivity.getServiceProvider(DBWriter.class);
			dbWriter.modifyAlbum(album, DBWriter.Action.Update);
//			MainActivity.setOnPauseAnimation(true);
			Intent intent = new Intent(mBaseActivity, BrowserSpecActivity.class);
			intent.putExtra(Const.IntentExtraKey.BrowSpecAlbumId, album.getId());
			intent.putExtra(Const.IntentExtraKey.BrowSpecIndex, album.getCurrent().getEpisode());
			mBaseActivity.startActivity(intent);
		}
	}
	
	private Stat getStat() {
		return (Stat) mBaseActivity.getServiceProvider(Stat.class);
	}
}
