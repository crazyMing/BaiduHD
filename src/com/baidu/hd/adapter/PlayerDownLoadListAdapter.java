package com.baidu.hd.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.TaskFactory;
import com.baidu.hd.module.Task.State;
import com.baidu.hd.module.Task.Type;
import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.personal.SDCardUtil;
import com.baidu.hd.player.PlayerAccessor;
import com.baidu.hd.player.PlayerActivity;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.player.Scheduler;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatId;
import com.baidu.hd.task.TaskManager;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.ToastUtil;
import com.baidu.hd.R;

/**
 * 播放器播放列表
 */
public class PlayerDownLoadListAdapter extends BaseAdapter {

	private LayoutInflater mInflater = null;
	private List<NetVideo> mVideos = new ArrayList<NetVideo>();
	private BaseActivity mContext;
	private String referString;
	TaskManager taskManager;
	private Album mAlbum;
	PlayerAccessor mpPlayerAccessor;

	private final class ViewHolder {
		TextView txtName;
		ImageView imageView;
		boolean isFinished=false;
	}

	public PlayerDownLoadListAdapter(Context context, String referString,
			PlayerAccessor accessor) {
		this.mInflater = LayoutInflater.from(context);
		this.mContext = (BaseActivity) context;
		this.referString = referString;
		taskManager = (TaskManager) mContext
				.getServiceProvider(TaskManager.class);
		this.mpPlayerAccessor = accessor;
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

		if (null == convertView) {
			viewHolder = new ViewHolder();

			convertView = mInflater.inflate(R.layout.player_dwonloadlist_item,
					null);
			viewHolder.txtName = (TextView) convertView
					.findViewById(R.id.video_name);
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.player_playlist_play_image);
			// viewHolder.txtName.setGravity(Gravity.CENTER);
			// viewHolder.imageView.setImageResource(R.drawable.ic_downloading);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final NetVideo netVideo = this.mVideos.get(position).toNet();
		String name = StringUtil.getVideoName(netVideo, mAlbum);
		if (name.contains(".")) {
			name = (position + 1) + "-"
					+ name.substring(0, name.lastIndexOf("."));
		} else {
			name = (position + 1) + "-" + name;
		}
		viewHolder.txtName.setText(name);
		final Task task = createTask(netVideo);
		Task find = taskManager.find(task.getKey());
		if (find != null) {

			viewHolder.imageView.setVisibility(View.VISIBLE);
			if (find.getState() == State.Complete) {
				viewHolder.imageView.setImageResource(R.drawable.ic_downloaded);
				viewHolder.isFinished=true;
			} else {
				viewHolder.imageView
						.setImageResource(R.drawable.ic_downloading);
			}
			// 去掉正在播放但是不在下载的任务
			// 下载中已有当前播放任务,播放下载列表不显示.这个问题是Scheduler.getTask在初始化时没有判断是否downloading引起的
			// 已修复2013-0109
			Scheduler sc = ((PlayerActivity) mContext).getScheduler();
			try {
				if (sc.getTaskKey() == find.getKey()) {
					if (!sc.isDownLoading()) {
						viewHolder.imageView.setVisibility(View.GONE);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (getInWaitArr(task) != null) {
			viewHolder.imageView.setImageResource(R.drawable.ic_downloading);
			viewHolder.imageView.setVisibility(View.VISIBLE);
		} else {
			viewHolder.imageView.setVisibility(View.GONE);
			viewHolder.txtName.setTextColor(0xff929292);
		}
		final ViewHolder vh = viewHolder;
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mpPlayerAccessor.startHideRightBar();
				if(vh.isFinished){
					return;
				}
				Task find = taskManager.find(task.getKey());
				// 无此任务
				if (find == null) {
					//无此任务，wait队列里也无此任务
					if (getInWaitArr(task) == null) {
						if (isSdcardReady()) {
							downloadTask(task);
							ToastUtil.showMessage(
									mContext,
									mContext.getResources().getString(
											R.string.download_tip),
									Toast.LENGTH_SHORT);
							vh.imageView
									.setImageResource(R.drawable.ic_downloading);
							vh.imageView.setVisibility(View.VISIBLE);
						}
					}
					//无此任务，wait队列里有此任务，除去队列里的任务和图标
					else{
						Scheduler sc = ((PlayerActivity) mContext).getScheduler();
						sc.waitArr.remove(getInWaitArr(task));
						vh.imageView.setVisibility(View.GONE);
					}
				}
				// 有此任务
				else {
					Scheduler sc = ((PlayerActivity) mContext).getScheduler();
					if (vh.imageView.getVisibility() == View.VISIBLE) {
						// 有此任务，且图标可见，不为正在播放，则除去此任务
						if (sc.getTaskKey() != find.getKey()) {
							vh.imageView.setVisibility(View.GONE);
							cacnle(find);
						}
						// 有任务，图标可见，为正在播放，则将download状态改为false
						else {
							vh.imageView.setVisibility(View.GONE);
							cacnle(find);
							sc.cancleDownload();
						}
						Toast.makeText(
								mContext,
								mContext.getResources().getString(
										R.string.cancel_download), 0).show();
						;
					} else {
						// 有此任务，图标不可见,为正在播放的视频,开始下载
						if (sc.getTaskKey() == find.getKey()) {
							if (!sc.isDownLoading()) {
								if (sc.download()) {
									vh.imageView
											.setImageResource(R.drawable.ic_downloading);
									vh.imageView.setVisibility(View.VISIBLE);
								}
							}
						}
						// 有此任务，图标不可见,不为正在播放 提示，则图标明明应该显示的啊是吧擦。显示图标
						else {
							ToastUtil
									.showMessage(
											mContext,
											mContext.getResources()
													.getString(
															R.string.brow_spec_select_task_exist),
											Toast.LENGTH_SHORT);
							vh.imageView
									.setImageResource(R.drawable.ic_downloading);
							vh.imageView.setVisibility(View.VISIBLE);
						}
					}
				}

			}
		});
		return convertView;

	}

	public void fillList(Album album) {
		this.mAlbum = album;
		this.mVideos = album.getVideos();
	}

	private Task createTask(NetVideo v) {
		String url = v.getUrl();
		String refer = v.getRefer();
		PlayListManager playListManager = ((PlayListManager) ((BaseActivity) mContext)
				.getServiceProvider(PlayListManager.class));
		Task task = TaskFactory.create(StringUtil.isSmallUrl(url)? Task.Type.Small
				: Type.Big);
		task.setName(StringUtil.getNameForUrl(url));
		Album album = playListManager.findAlbumByRefer(referString);
		playListManager.setDownload(album, true);
		task.setAlbumId(album.getId());
		task.setUrl(url);
		task.setRefer(refer);
		task.setVideoType(StringUtil.isSmallUrl(url) ? NetVideo.NetVideoType.P2P_STREAM
				: NetVideo.NetVideoType.BIGSITE);
		task.setName(StringUtil.getVideoName(v, mAlbum));
		return task;
	}

	private void downloadTask(Task task) {
		/*if (taskManager.find(task.getKey()) == null) {
			Stat stat = ((Stat) mContext.getServiceProvider(Stat.class));
			stat.incLogCount(StatId.BaseDownloadCount + task.getVideoType());
			stat.incEventCount(StatId.Download.Name, StatId.Download.Count);
			stat.incEventCount(StatId.Download.Name, StatId.Download.Count
					+ task.getFormatVideoType());
		}
		taskManager.start(task);*/
		Scheduler sc = ((PlayerActivity) mContext).getScheduler();
		sc.waitArr.add(task);
	}

	private void cacnle(Task task) {
		taskManager.remove(task);
	}

	private Task getInWaitArr(Task t) {
		Task res = null;
		Scheduler sc = ((PlayerActivity) mContext).getScheduler();

		for (Task temp : sc.waitArr) {
			if (temp.isSame(t)) {
				res = temp;
				break;
			}
		}
		return res;
	}

	private boolean isSdcardReady() {
		double availableSize = SDCardUtil.getInstance().getAvailableSize();
		double totoalSize = SDCardUtil.getInstance().getAllSize();
		if (availableSize != -1 && totoalSize != -1) {
			return true;
		} else {
			ToastUtil.showMessage(
					mContext,
					mContext.getResources().getString(
							R.string.brow_spec_select_sdcard_no_tip),
					Toast.LENGTH_SHORT);
		}
		return false;
	}

}
