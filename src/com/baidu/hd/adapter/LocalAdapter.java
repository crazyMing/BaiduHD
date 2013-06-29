package com.baidu.hd.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.baidu.hd.ctrl.TextViewMultilineEllipse;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.module.LocalItemPackage;
import com.baidu.hd.module.album.LocalVideo;
import com.baidu.hd.personal.SDCardUtil;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.util.PlayerTools;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;


/**
 * ListView适配器
 */
public class LocalAdapter extends EditableAdapter<LocalItemPackage>
{
	private EventListener mEventListener = new EventListener() {
		
		@Override
		public void onEvent(EventId id, EventArgs args) {
			// 不需要fillList
			//fillList();
			notifyDataSetChanged();
		}
	};
	
	private final class ViewHolder
	{
		ImageView commonTaskDelete; // 删除
		RelativeLayout commonTaskCenterLayout; // 
		TextViewMultilineEllipse commonTaskName; // 文件名
		TextView commonTaskPlayTime; // 已播放时长/总播放时长
		TextView commonTaskSize; // 下载大小/总大小
	}
	
	public LocalAdapter(Context context, Callback callback)
	{
		super(context, callback);
		if (isServiceCreated())
		{
			onServiceCreated();
		}
	}
	
	public void fillList()
	{
		PlayListManager playListManager = (PlayListManager) mBaseActivity.getServiceProvider(PlayListManager.class);
		List<LocalVideo> videos = playListManager.getAllLocal();

		mItems.clear();
		mAllItemNum = 0;
		for (LocalVideo v : videos)
		{
			LocalItemPackage item = new LocalItemPackage();
			item.setVideo(v);
			mItems.add(item);
			mAllItemNum++;
		}
		notifyEditButton(mAllItemNum == 0 ? 0 : 1);
	}
	
	@Override
	public void onServiceCreated()
	{
		getEventCenter().addListener(EventId.ePlayListUpdate, mEventListener);
	}
	
	@Override
	public void onResume()
	{
		fillList();
		getEventCenter().addListener(EventId.ePlayListUpdate, mEventListener);
	}

	@Override
	public void onPause()
	{
		getEventCenter().removeListener(mEventListener);
	}

	@Override
	public void removeMarkDeleteItem()
	{
		PlayListManager playListManager = (PlayListManager) mBaseActivity.getServiceProvider(PlayListManager.class);
		for (LocalItemPackage item : mItems)
		{
			if (item.isSelectedDel())
			{
				playListManager.removeLocal(item.getVideo());
				SDCardUtil.getInstance().delFiles(item.getVideo().getFullName());
				mSelectItemNum--;
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder = null;
		final LocalItemPackage item = mItems.get(position);
		final LocalVideo video = item.getVideo();

		if (null == convertView)
		{
			viewHolder = new ViewHolder();

			convertView = mInflater.inflate(R.layout.common_task_item, null);
			viewHolder.commonTaskDelete = (ImageView) convertView.findViewById(R.id.common_task_delete);
			viewHolder.commonTaskCenterLayout = (RelativeLayout) convertView.findViewById(R.id.common_task_center_layout);
			viewHolder.commonTaskName = new TextViewMultilineEllipse(mContext);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
//			layoutParams.setMargins(0, PlayerTools.formatDipToPx(mContext, 10), PlayerTools.formatDipToPx(mContext, 30), 0);
			viewHolder.commonTaskName.setPadding(0, 
					PlayerTools.formatDipToPx(mContext, 10), 
					PlayerTools.formatDipToPx(mContext, 30), PlayerTools.formatDipToPx(mContext, 30));
			viewHolder.commonTaskName.setLayoutParams(layoutParams);
			viewHolder.commonTaskName.setEllipsis("...");
//			viewHolder.commonTaskName.setEllipsisMore("More");
			viewHolder.commonTaskName.setMaxLines(2);
			viewHolder.commonTaskName.setLineSpacing(PlayerTools.formatDipToPx(mContext, 6));
			viewHolder.commonTaskName.setTextSize(PlayerTools.formatDipToPx(mContext, 16));
			viewHolder.commonTaskName.setTextColor(0xff333333);
	        viewHolder.commonTaskCenterLayout.addView(viewHolder.commonTaskName);
			
			viewHolder.commonTaskPlayTime = (TextView) convertView.findViewById(R.id.common_task_submit_left_info);
			viewHolder.commonTaskSize= (TextView) convertView.findViewById(R.id.common_task_submit_right_info);
			convertView.findViewById(R.id.common_task_rigth_layout).setVisibility(View.GONE);
			convertView.findViewById(R.id.common_task_progressbar).setVisibility(View.GONE);
			convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// delete icon
		viewHolder.commonTaskDelete.setVisibility(mIsEditComplete ? View.VISIBLE : View.GONE);
		viewHolder.commonTaskDelete.setImageResource(item.isSelectedDel() ? R.drawable.ic_list_check_on : R.drawable.ic_list_check_off);

		// name
		viewHolder.commonTaskName.setText(video.getName());

		// play info
		String playInfo = String.format("%s/%s", StringUtil.formatTime(video.getPosition()), StringUtil.formatTime(video.getDuration()));
		viewHolder.commonTaskPlayTime.setText(playInfo);
		
		// size
		viewHolder.commonTaskSize.setText(StringUtil.formatSize(video.getTotalSize()));

		convertView.findViewById(R.id.common_task_center_layout).setBackgroundDrawable(null);
		return convertView;
	}
	
}

