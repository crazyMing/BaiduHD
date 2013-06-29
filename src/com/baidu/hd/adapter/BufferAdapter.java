package com.baidu.hd.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.hd.ctrl.TextViewMultilineEllipse;
import com.baidu.hd.detect.Detect;
import com.baidu.hd.detect.FilterCallback;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.BufferItemPackage;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.Video;
import com.baidu.hd.module.album.VideoFactory;
import com.baidu.hd.module.album.NetVideo.NetVideoType;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId.Personal;
import com.baidu.hd.task.TaskManager;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.PlayerTools;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

public class BufferAdapter extends EditableAdapter<BufferItemPackage>
{
	Logger logger = new Logger("BufferAdapter");
	
	private final class ViewHolder
	{
		ImageView commonTaskDelete; // 删除
		RelativeLayout commonTaskCenterLayout; // 删除
		TextViewMultilineEllipse commonTaskName; // 文件名
		ProgressBar commonTaskProgressBar; // 下载bar
		TextView commonTaskSize; // 下载大小/总大小
		TextView commonTaskStatusInfo; // 下载状态信息
		ImageView commonTaskStatus; // 下载控制
		LinearLayout commonTaskRightLayout;
	}

	public BufferAdapter(Context context, Callback callback)
	{
		super(context, callback);
		if (isServiceCreated())
		{
			onServiceCreated();
		}
	}
	
	/**
	 * 事件监听器
	 */
	private EventListener mEventListener = new EventListener()
	{
		@Override
		public void onEvent(EventId id, EventArgs args)
		{
			switch (id)
			{
				case eTaskCreate:
				case eTaskRemove:
					fillList();
//					updateRemoteTask();
					break;
				case eTaskStart:
				case eTaskStop:
				case eTaskComplete:
				case eTaskQueue:
				case eTaskError:
					// ==0 表示全部处于暂停状态，返回1；否则返回2
					notifyStartStopAllButton(getStartQueueTaskCount() == 0 ? 1 : 2);
					notifyDataSetChanged();
//					updateRemoteTask();
					break;
			}
		}
	};

	private Handler mRefreshHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (getStartQueueTaskCount() > 0)
			{
				notifyDataSetChanged();
				if (mSDCardCallback != null ) {
					mSDCardCallback.notifySDCardInfo();
				}
			}
			this.sendEmptyMessageDelayed(0, Const.Elapse.TaskRefresh);
		}
	};

	public void fillList()
	{
		TaskManager taskManager = (TaskManager) this.mBaseActivity.getServiceProvider(TaskManager.class);
		List<Task> tasks = taskManager.getAllVisible();

		mItems.clear();
		mAllItemNum = 0;
		for (Task task : tasks)
		{
			BufferItemPackage item = new BufferItemPackage();
			item.setTask(task);
			mItems.add(item);
			mAllItemNum++;
		}
		notifyEditButton(mAllItemNum == 0 ? 0 : 1);
		if (mAllItemNum == 0)
		{
			notifyStartStopAllButton(0);
		}
		else
		{
			notifyStartStopAllButton(getStartQueueTaskCount() == 0 ? 1 : 2);
		}
	}
	
	public void launcherTask(int paramInt)
	{
		Task task = getItems().get(paramInt).getTask();
		logger.d("task url=" + task.getUrl());
		
		PlayListManager playListManager = (PlayListManager)mBaseActivity.getServiceProvider(PlayListManager.class);
		Album album = playListManager.findAlbumById(task.getAlbumId());
		NetVideo video = playListManager.findNetVideo(task.getRefer(), task.getUrl());
		
		// <<<<<补丁：解决BUG MEDIA-4810 BEGIN
		//【崩溃】【个人中心】【下载】在SD不加载情况下下载的视频，恢复SD卡下载完成后，播放崩溃（非必现） 
		if (video == null) {
			logger.e("launcherTask video = null");
			video = VideoFactory.create(false).toNet();
		}
		video.setAlbum(album);
		video.setAlbumId(task.getAlbumId());
		video.setUrl(task.getUrl());
		// END >>>>>
		if (album != null) {
			album.setCurrent(video);
			playListManager.setDownload(album, true);
			video.setAlbumRefer(album.getRefer());
			video.setType(album.getType());
			PlayerLauncher.startup(mContext, album, video);
		}
		else {
			if (!StringUtil.isEmpty(video.getUrl())) {
				 if (task.getType()==Task.Type.Small) {
					 video.setType(NetVideoType.P2P_STREAM);
				 }
				 else {
					 video.setType(NetVideoType.BIGSITE);
				 }
			}
			PlayerLauncher.startup(mContext, video);
		}
		
		// 统计
		Stat stat = (Stat) mBaseActivity.getServiceProvider(Stat.class);
		if (stat != null) {
			stat.incEventCount(Personal.Name, Personal.BufferPlay);
		}
	}
	
	public void changeTaskState(int paramInt) {
		Task task = getItems().get(paramInt).getTask();
		TaskManager taskManager = (TaskManager) mBaseActivity.getServiceProvider(TaskManager.class);
		if (task.getState() == Task.State.Start) {
			taskManager.stop(task);
		}
		else if (task.getState() == Task.State.Stop) {
			taskManager.start(task);
		}
		else if (task.getState() == Task.State.Queue) {
			taskManager.stop(task);
		}
	}
	
	public int getTaskState(int paramInt) {
		Task task = getItems().get(paramInt).getTask();
		return task.getState();
	}

	public void stopAll()
	{
		if (!isServiceCreated()) return;
		TaskManager taskManager = (TaskManager) this.mBaseActivity.getServiceProvider(TaskManager.class);
		taskManager.stopAllVisible();
	}
	
	public void toStartTask(Task task)
	{
		if (!isServiceCreated()) return;
		// SD卡提示
		Detect detect = (Detect) this.mBaseActivity.getServiceProvider(Detect.class);
		if (!detect.isSDCardAvailable()) {
			detect.SDCardPrompt();
			return;
		}
		
		// 3G网络提示
		final Task fTask = task;
		final TaskManager taskManager = (TaskManager) mBaseActivity.getServiceProvider(TaskManager.class);
		detect.filterByNet(new FilterCallback()
		{
			@Override
			public void onDetectPromptReturn(DetectPromptReturn detectPromptReturn)
			{
				if (DetectPromptReturn.eTrue  == detectPromptReturn)
				{
					if (null == fTask)
					{
						taskManager.startAllVisible();
					}
					else
					{
						taskManager.start(fTask);
					}
				}
				else if (DetectPromptReturn.eNoNetAvailable  == detectPromptReturn)
				{
					Toast.makeText(mContext, R.string.network_not_available, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	@Override
	public void onServiceCreated()
	{
		getEventCenter().addListener(mEventListener);
	}
	
	@Override
	public void onResume()
	{
		this.fillList();
		this.mRefreshHandler.sendEmptyMessageDelayed(0, Const.Elapse.TaskRefresh);
	}

	@Override
	public void onPause()
	{
		this.mRefreshHandler.removeMessages(0);
	}

	@Override
	public void removeMarkDeleteItem()
	{
		if (!isServiceCreated()) return;
		//<<先将所有剧集都设为非download
		PlayListManager playListManager = (PlayListManager)mBaseActivity.getServiceProvider(PlayListManager.class);
		playListManager.setDownload(false);
		
		List<String> keys = new ArrayList<String>();
		for (BufferItemPackage item : mItems)
		{
			if (item.isSelectedDel())
			{
				keys.add(item.getTask().getKey());
			}
			else {
				playListManager.setDownload(item.getTask().getAlbumId(), true);
			}
		}
		TaskManager taskManager = (TaskManager) this.mBaseActivity.getServiceProvider(TaskManager.class);
		taskManager.multiRemove(keys);
		fillList();
		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder = null;
		final int itemPosition = position;
		final Task fTask = mItems.get(position).getTask();
		final BufferItemPackage item = mItems.get(position);
		if (null == convertView)
		{
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.common_task_item, null);
			viewHolder.commonTaskDelete = (ImageView) convertView.findViewById(R.id.common_task_delete);
			viewHolder.commonTaskCenterLayout = (RelativeLayout) convertView.findViewById(R.id.common_task_center_layout);
			viewHolder.commonTaskName = new TextViewMultilineEllipse(mContext);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			viewHolder.commonTaskName.setPadding(0, 
					PlayerTools.formatDipToPx(mContext, 10), 
					PlayerTools.formatDipToPx(mContext, 30), PlayerTools.formatDipToPx(mContext, 30));
			viewHolder.commonTaskName.setLayoutParams(layoutParams);
			viewHolder.commonTaskName.setEllipsis("...");
			viewHolder.commonTaskName.setMaxLines(1);
			viewHolder.commonTaskName.setLineSpacing(PlayerTools.formatDipToPx(mContext, 6));
			viewHolder.commonTaskName.setTextSize(PlayerTools.formatDipToPx(mContext, 16));
			viewHolder.commonTaskName.setTextColor(0xff333333);
	        viewHolder.commonTaskCenterLayout.addView(viewHolder.commonTaskName);
			viewHolder.commonTaskProgressBar = (ProgressBar) convertView.findViewById(R.id.common_task_progressbar);
			
			viewHolder.commonTaskSize = (TextView) convertView.findViewById(R.id.common_task_submit_left_info);
			viewHolder.commonTaskStatusInfo = (TextView) convertView.findViewById(R.id.common_task_submit_right_info);
			viewHolder.commonTaskStatus = (ImageView) convertView.findViewById(R.id.common_task_status);
			viewHolder.commonTaskRightLayout = (LinearLayout) convertView.findViewById(R.id.common_task_rigth_layout);
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
				TaskManager taskManager = (TaskManager) mBaseActivity.getServiceProvider(TaskManager.class);
				switch (fTask.getState())
				{
					case Task.State.Start:
					case Task.State.Queue:
						taskManager.stop(fTask);
						break;
					case Task.State.Error:
					case Task.State.Stop:
						toStartTask(fTask);
						break;
					case Task.State.Complete:
						launcherTask(itemPosition);
						break;
				}
			}
		});

		long totalSize = fTask.getTotalSize();
		long downloadSize = fTask.getDownloadSize();

		// delete
		viewHolder.commonTaskDelete.setVisibility(mIsEditComplete ? View.VISIBLE : View.GONE);
		viewHolder.commonTaskDelete.setImageResource(item.isSelectedDel() ? R.drawable.ic_list_check_on : R.drawable.ic_list_check_off);

		// name
		String name = fTask.getName();
		if (StringUtil.isEmpty(name))
		{
			name = fTask.getFileName();
		}
		viewHolder.commonTaskName.setMaxLines(1);
		viewHolder.commonTaskName.setText(name);

		// size
		if (fTask.getState() == Task.State.Complete)
		{
			viewHolder.commonTaskSize.setText(StringUtil.formatSize(totalSize));
		}
		else
		{
			viewHolder.commonTaskSize.setText(String.format("%s/%s", StringUtil.formatSize(downloadSize), StringUtil.formatSize(totalSize)));
		}

		// percent
		int percent = fTask.getPercent();
		viewHolder.commonTaskProgressBar.setProgress(percent);

		// 播放控制
		this.updatePlayControlImage(fTask, viewHolder);

		convertView.findViewById(R.id.common_task_center_layout).setBackgroundDrawable(null);
		return convertView;
	}

	private void updatePlayControlImage(Task task, ViewHolder viewHolder)
	{
		int imageId = R.drawable.ic_list_stop;
		viewHolder.commonTaskRightLayout.setVisibility(!mIsEditComplete ? View.VISIBLE : View.GONE);
		ColorStateList csl1 = mContext.getResources().getColorStateList(R.color.normal_textcolor_gray);
		ColorStateList csl2 = mContext.getResources().getColorStateList(R.color.normal_textcolor_red);
		viewHolder.commonTaskStatusInfo.setTextColor(csl1);
		viewHolder.commonTaskProgressBar.setVisibility(View.VISIBLE);
		
		switch (task.getState())
		{
			case Task.State.Start:
				viewHolder.commonTaskStatusInfo.setText(StringUtil.formatSpeed(task.getSpeed()));
				break;
			case Task.State.Queue:
				viewHolder.commonTaskStatusInfo.setText(R.string.task_queue);
				break;
			case Task.State.Error:
				viewHolder.commonTaskStatusInfo.setTextColor(csl2);
				if(task.getErrorCode() == Task.ErrorCode.SnifferFail) {
					viewHolder.commonTaskStatusInfo.setText(R.string.task_sniffer_error);
				} else if(task.getErrorCode() == Task.ErrorCode.DiskSpace || task.getErrorCode() == Task.ErrorCode.FileError) {
					logger.e("task.getErrorCode()  : file name=" + task.getFileName() + "; ErrorCode=" + task.getErrorCode());
					viewHolder.commonTaskStatusInfo.setText(R.string.task_sdcard_full_error);
				} else {
					viewHolder.commonTaskStatusInfo.setText(R.string.task_error);
				}
				imageId = R.drawable.ic_list_start;
				break;
			case Task.State.Stop:
				viewHolder.commonTaskStatusInfo.setText(R.string.task_stop);
				imageId = R.drawable.ic_list_start;
				break;
			case Task.State.Complete:
				viewHolder.commonTaskStatusInfo.setText(R.string.task_complete);
				viewHolder.commonTaskProgressBar.setVisibility(View.GONE);
				viewHolder.commonTaskName.setMaxLines(2);
//				viewHolder.commonTaskName.setSingleLine(false);
				imageId = R.drawable.ic_list_play;
				break;
			default:
				break;
		}
		viewHolder.commonTaskStatus.setImageResource(imageId);
	}

	/**
	 * 任务在下载或者在排队的总数
	 * 若一个都木有，则说明木有需要下载，可以认为全部都处于停止状态，此时应该提示“全部开始”；否则，应该提示“全部暂停”
	 * 即返回值为0时，提示“全部开始”；否则，提示“全部暂停”，
	 * 亦即，返回值为0时表示全部暂停；否则，某些在下载。
	 * @return
	 */
	private int getStartQueueTaskCount()
	{
		int count = 0;
		for (BufferItemPackage pack : mItems)
		{
			Task t = pack.getTask();
			if (t.getState() == Task.State.Start ||
				t.getState() == Task.State.Queue)
			{
				++count;
			}
		}
		return count;
	}
	
	/**
	 *  通知缓存面板BufferPage
	 */
	public interface SDCardCallback {
		void notifySDCardInfo() ;
	}
	private SDCardCallback mSDCardCallback = null;
	public void setSDCardCallback(SDCardCallback callback) {
		mSDCardCallback = callback;
	}
}
