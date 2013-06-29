package com.baidu.hd.task;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.baidu.hd.conf.Configuration;
import com.baidu.hd.db.DBReader;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.P2PBlock;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.Task.State;
import com.baidu.hd.service.ServiceConsumer;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.upgrade.RemoteUpgrade;
import com.baidu.hd.upgrade.RemoteUpgrade.Type;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;
import com.baidu.player.download.JNIP2P;

/**
 * 任务管理器实现
 */
public class TaskManagerImp implements TaskManager, ServiceConsumer,
		TaskManagerAccessor {
	private static final int RemoteUpgradeMsgDelay = 500;
	private static final int RemoteUpgradeMsg = 1;

	private Logger logger = new Logger("TaskManager");

	private Context mContext = null;
	private ServiceFactory mServiceFactory = null;

	/** 该对象是否已经销毁 */
	private boolean isDestroyed = false;

	/** 事件通知 */
	private EventNotifier mEventNotifier = new EventNotifier();

	/** 任务列表 */
	private List<Task> mTasks = new ArrayList<Task>();

	/** 底层JNI的适配器 */
	private ExecAdapter mExeAdapter = new ExecAdapter();
	private QueryAdapter mQueryAdpater = new QueryAdapter();

	/** 列表状态 */
	private ListStateManager mListStateManager = new ListStateManager();

	/** 任务处理器 */
	private Map<Integer, TaskHandler> mTaskHandlers = new HashMap<Integer, TaskHandler>();

	/** 更新远程通知栏 */
	private RemoteUpgrade mRemoteUpgrade = null;
	private Handler mHandlerRemoteUpgrade = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == RemoteUpgradeMsg) {
				int num = getStartQueueTaskCount();
				if (num > 0) {
//					 mRemoteUpgrade.startRemoteUpgrade(Type.DowndLoadTaskUpgrade, mContext.getText(R.string.app_name).toString());
					String str = String.format(
							mContext.getText(R.string.remote_download_task)
									.toString(), num);
					mRemoteUpgrade.updateRemoteUpgrade(
							Type.DowndLoadTaskUpgrade, str);
				} else {
					mRemoteUpgrade.stopRemoteUpgrade(Type.DowndLoadTaskUpgrade);
				}
				logger.d("updateRemoteTask = " + num);
			}
		}
	};

	private EventListener mDieListener = new EventListener() {

		@Override
		public void onEvent(EventId id, EventArgs args) {
			restart();
		}
	};

	/**
	 * 刷新任务定时器
	 */
	private Handler mTimer = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			for (Task task : mTasks) {
				getTaskHandler(task).query(task);
			}
			sendEmptyMessageDelayed(0, Const.Elapse.TaskRefresh);
		}
	};

	public TaskManagerImp() {
		mTaskHandlers.put(Task.Type.Big, new BigSiteTaskHandler());
		mTaskHandlers.put(Task.Type.Small, new SmallSiteTaskHandler());
	}

	@Override
	public void setContext(Context ctx) {
		mContext = ctx;
	}

	public void onCreate() {

		// initialize notifier
		EventCenter eventCenter = (EventCenter) mServiceFactory
				.getServiceProvider(EventCenter.class);
		mEventNotifier.create(eventCenter);

		// make download path
		Configuration conf = (Configuration) mServiceFactory
				.getServiceProvider(Configuration.class);
		File dir = new File(conf.getTaskSavePath());
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				logger.e("create dir fail " + conf.getTaskSavePath());
			}
		}

		// initialize adapter
		mQueryAdpater.create(mServiceFactory);
		mExeAdapter.create(this);

		// initialize state
		mListStateManager.create(this);

		// initialize handler
		TaskHandler bigTaskHandler = mTaskHandlers.get(Task.Type.Big);
		TaskHandler smallTaskHandler = mTaskHandlers.get(Task.Type.Small);
		bigTaskHandler.create(this);
		smallTaskHandler.create(this);

		// initialize tasks
		DBReader reader = (DBReader) mServiceFactory
				.getServiceProvider(DBReader.class);
		mTasks = reader.getAllTask();

		// initialize refresh
		mTimer.sendEmptyMessageDelayed(0, Const.Elapse.TaskRefresh);

		bigTaskHandler.setPostEventEnable(false);
		smallTaskHandler.setPostEventEnable(false);
		for (Task t : mTasks) {
			getTaskHandler(t).startup(t);
		}
		bigTaskHandler.setPostEventEnable(true);
		smallTaskHandler.setPostEventEnable(true);

		// 必须使JNI模块在主进程load一遍，否则播放内核模块会加载JNI模块失败
		JNIP2P.getInstance();

		eventCenter.addListener(EventId.eDownloadServiceDie, mDieListener);
		mRemoteUpgrade = (RemoteUpgrade) mServiceFactory
				.getServiceProvider(RemoteUpgrade.class);
	}

	public void onDestory() {

		EventCenter eventCenter = (EventCenter) mServiceFactory
				.getServiceProvider(EventCenter.class);
		eventCenter.removeListener(mDieListener);

		mListStateManager.destory();
		mTimer.removeMessages(0);
		for (Task t : mTasks) {
			getTaskHandler(t).shutdown(t);
		}
		mTaskHandlers.get(Task.Type.Big).destroy();
		mTaskHandlers.get(Task.Type.Small).destroy();
		isDestroyed = true;
	}

	@Override
	public void onSave() {
		for (Task t : mTasks) {
			getTaskHandler(t).shutdown(t);
		}
	}

	@Override
	public void setServiceFactory(ServiceFactory factory) {
		mServiceFactory = factory;
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// TaskManager
	@Override
	public void remove(Task value) {
		if (checkParam(value, "remove")) {
			Task task = find(value.getKey());
			if (task == null) {
				logger.e("remove null task " + value.getKey());
			} else {
				mTasks.remove(task);
				mListStateManager.getCurrentState().remove(task);
			}
		}
	}

	@Override
	public void start(Task value) {
		if (checkParam(value, "start")) {
			Task task = create(value);
			mListStateManager.getCurrentState().start(task);
		}
	}

	@Override
	public void stop(Task value) {
		if (checkParam(value, "stop")) {
			Task task = find(value.getKey());
			if (task == null) {
				logger.e("stop null task " + value.getKey());
			} else {
				mListStateManager.getCurrentState().stop(task);
			}
		}
	}

	@Override
	public void error(Task value) {
		if (checkParam(value, "error")) {
			Task task = find(value.getKey());
			if (task == null) {
				logger.e("error null task " + value.getKey());
			} else {
				TaskHandler taskHandler = getTaskHandler(task);
				taskHandler.setProperty(task, value);
				mListStateManager.getCurrentState().error(task);
			}
		}
	}

	@Override
	public List<Task> multiQuery(List<String> keys) {

		List<Task> tasks = new ArrayList<Task>();
		for (String key : keys) {
			Task task = find(key);
			if (task != null) {
				tasks.add(task);
			}
		}
		return tasks;
	}

	@Override
	public void startAllVisible() {
		ListState listState = mListStateManager.getCurrentState();
		listState.setBatching(ListState.BatchOperate.eStart);
		for (Task t : mTasks) {
			if (t.isVisible()) {
				listState.start(t);
			}
		}
		listState.setBatching(ListState.BatchOperate.eNone);
	}

	@Override
	public void stopAllVisible() {
		ListState listState = mListStateManager.getCurrentState();
		listState.setBatching(ListState.BatchOperate.eStop);
		for (Task t : mTasks) {
			if (t.isVisible()) {
				listState.stop(t);
			}
		}
		listState.setBatching(ListState.BatchOperate.eNone);
	}

	@Override
	public void multiRemove(List<String> keys) {
		ListState listState = mListStateManager.getCurrentState();
		listState.setBatching(ListState.BatchOperate.eRemove);
		for (String key : keys) {
			Task task = find(key);
			if (task == null) {
				logger.e("remove null task " + key);
			} else {
				mTasks.remove(task);
				listState.remove(task);
			}
		}
		listState.setBatching(ListState.BatchOperate.eNone);
	}

	@Override
	public Task find(String value) {
		if (StringUtil.isEmpty(value)) {
			return null;
		}
		for (Task t : mTasks) {
			if (value.equalsIgnoreCase(t.getKey())) {
				return t;
			}
		}
		return null;
	}

	@Override
	public List<Task> getAllVisible() {
		List<Task> result = new ArrayList<Task>();
		for (Task t : mTasks) {
			if (t.isVisible()) {
				result.add(t);
			}
		}
		return result;
	}

	@Override
	public List<Task> getAll() {
		List<Task> result = new ArrayList<Task>();
		for (Task t : mTasks) {
			result.add(t);
		}
		return result;
	}

	@Override
	public boolean isFileExist(Task value) {
		if (checkParam(value, "isFileExist")) {

			// 必须取容器内的元素，只有容器内的元素才有firstTask
			Task task = find(value.getKey());
			if (task == null) {
				return false;
			} else if (task.getState() != Task.State.Complete) {
				return false;
			} else {
				return getTaskHandler(value).isFileExist(task);
			}
		}
		return false;
	}

	@Override
	public void setMediaTime(Task task, int value) {
		mQueryAdpater.setMediaTime(task, value);
	}

	@Override
	public P2PBlock getBlock(Task task) {
		// 不需要找容器中的任务，因为只是查询操作
		return getTaskHandler(task).getBlock(task);
	}

	@Override
	public void clearGarbage(clearGarbageEvent event) {
		try {
			Configuration conf = (Configuration) mServiceFactory
					.getServiceProvider(Configuration.class);
			String taskPath = conf.getTaskSavePath();
			File folder = new File(taskPath);
			for (String subFolder : folder.list()) {
				if (null != event && event.isCancel()) {
					return;
				}
				try {
					File file = new File(subFolder);
					if (file.isFile()) {
						if (null != event) {
							event.clearSize(file.length());
						}
						file.delete();
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				boolean found = false;
				for (Task t : mTasks) {
					if (null != event && event.isCancel()) {
						return;
					}

					if (t.getFolderName().equalsIgnoreCase(subFolder)) {
						found = true;
						break;
					}
				}
				if (!found) {
					try {
						File path = new File(taskPath + subFolder);
						if (!path.exists()) {
							return;
						}
						for (File file : path.listFiles()) {
							if (null != event) {
								if (event.isCancel()) {
									return;
								}
								event.clearSize(file.length());
							}
							file.delete();
						}
						path.delete();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// TaskManagerAccessor
	@Override
	public ServiceFactory getServiceFactory() {
		return mServiceFactory;
	}

	@Override
	public TaskHandler getTaskHandler(int type) {
		if (isDestroyed) {
			return new EmptyTaskHandler();
		}
		return mTaskHandlers.get(type);
	}

	@Override
	public List<Task> getAllTask() {
		return mTasks;
	}

	@Override
	public ExecAdapter getExeAdpater() {
		return mExeAdapter;
	}

	@Override
	public QueryAdapter getQueryAdapter() {
		return mQueryAdpater;
	}

	@Override
	public void postEvent(int eventId, Task task) {
		mListStateManager.getCurrentState().onPrePostEvent(eventId, task);
		if (eventId == CallbackEventId.OnQuery) {
			return;
		}
		mEventNotifier.postEvent(convertEventId(eventId), task);
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// helper
	private TaskHandler getTaskHandler(Task task) {
		return getTaskHandler(task.getType());
	}

	private Task create(Task value) {

		Task task = find(value.getKey());
		if (task == null) {
			mTasks.add(value);
			getTaskHandler(value).create(value);
			task = value;
		} else {
			getTaskHandler(value).setProperty(task, value);
		}
		return task;
	}

	private void restart() {
		mListStateManager.restart();
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// utility
	private boolean checkParam(Task task, String action) {
		return checkParam(task, action, false);
	}

	private boolean checkParam(Task task, String action, boolean isVerbose) {

		if (task == null) {
			logger.e(action + " null");
			return false;
		}
		if (isVerbose) {
			// logger.v(action + task.getKey());
		} else {
			logger.d(action + " " + task.getKey());
		}
		return true;
	}

	private EventId convertEventId(int eventId) {
		switch (eventId) {
		case CallbackEventId.OnCreate:
			return EventId.eTaskCreate;
		case CallbackEventId.OnRemove:
			updateRemoteTask();
			return EventId.eTaskRemove;
		case CallbackEventId.OnStart:
			updateRemoteTask();
			return EventId.eTaskStart;
		case CallbackEventId.OnStop:
			updateRemoteTask();
			return EventId.eTaskStop;
		case CallbackEventId.OnComplete:
			updateRemoteTask();
			return EventId.eTaskComplete;
		case CallbackEventId.OnError:
			updateRemoteTask();
			return EventId.eTaskError;
		case CallbackEventId.OnPlay:
			return EventId.eTaskPlay;
		case CallbackEventId.OnQueue:
			updateRemoteTask();
			return EventId.eTaskQueue;
		default:
			return EventId.eNone;
		}
	}

	private void updateRemoteTask() {
		if (null == mRemoteUpgrade) {
			mRemoteUpgrade = (RemoteUpgrade) mServiceFactory
					.getServiceProvider(RemoteUpgrade.class);
		}
		mHandlerRemoteUpgrade.removeMessages(RemoteUpgradeMsg);
		mHandlerRemoteUpgrade.sendMessageDelayed(
				mHandlerRemoteUpgrade.obtainMessage(RemoteUpgradeMsg),
				RemoteUpgradeMsgDelay);
	}

	private int getStartQueueTaskCount() {
		int count = 0;
		for (Task t : mTasks) {
			if (t.isVisible()
					&& (t.getState() == State.Start || t.getState() == State.Queue)) {
				count++;
			}
		}
		return count;
	}
}
