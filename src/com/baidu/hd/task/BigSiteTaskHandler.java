package com.baidu.hd.task;

import java.io.File;

import com.baidu.hd.conf.Configuration;
import com.baidu.hd.db.DBWriter;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.BigSiteTask;
import com.baidu.hd.module.P2PBlock;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.TaskFactory;
import com.baidu.hd.sniffer.M3U8SnifferEntityImpl;
import com.baidu.hd.sniffer.SnifferHandler;
import com.baidu.hd.util.Const;
import com.baidu.hd.util.FileUtil;
import com.baidu.hd.util.StringUtil;

/**
 * 大站任务处理器
 */
public class BigSiteTaskHandler extends BaseTaskHandler {

	private Logger logger = new Logger("TaskHandler");
	
	private SecondTaskHandler mSecondTaskHandler = new SecondTaskHandler();

	@Override
	public void create(TaskManagerAccessor accessor){
		super.create(accessor);
		mSecondTaskHandler.create(accessor.getServiceFactory());
	}
	
	@Override
	public void destroy() {
		mSecondTaskHandler.cancel();
	}

	@Override
	public void clearHandle(Task task) {
		BigSiteTask wholeTask = task.toBig();
		if(wholeTask.getFirstTask() != null) {
			wholeTask.getFirstTask().setHandle(0);
		}
		for(BigSiteTask t: wholeTask.getSecondTasks()) {
			t.setHandle(0);
		}
	}

	@Override
	public void forceStart(Task aTask) {
		BigSiteTask wholeTask = aTask.toBig();
		
		if(wholeTask.getFirstTask() == null) {
			if(StringUtil.isEmpty(wholeTask.getUrl())) {
				debugLog("first null and have not url, sniffer", wholeTask);
				wholeTask.setFileName(wholeTask.getName() + getSaveFileNameExt());
				final BigSiteTask fWholeTask = wholeTask;
				new TaskSniffer(wholeTask, mAccessor.getServiceFactory()).run(new TaskSniffer.Callback() {
					
					@Override
					public void onCallback(boolean success, String url) {
						if(success) {
							logger.d("sniffer complete, success");
							fWholeTask.setUrl(url);
							BigSiteTask firstTask = createFirstTask(fWholeTask);
							getDBWriter().addTask(firstTask);
							mAccessor.getExeAdpater().start(firstTask);
						} else {
							logger.e("sniffer error " + fWholeTask.getRefer());
							fWholeTask.setErrorCode(Task.ErrorCode.SnifferFail);
							setError(fWholeTask);
						}
					}
				});
			} else {
				// TOOD 会进来么？
				debugLog("first null and have url, create first", wholeTask);
				BigSiteTask firstTask = createFirstTask(wholeTask);
				getDBWriter().addTask(firstTask);
				mAccessor.getExeAdpater().start(firstTask);
			}
		} else {
			BigSiteTask firstTask = wholeTask.getFirstTask();
			if(firstTask.getState() == Task.State.Complete) {
				if(wholeTask.isParseComplete()) {
					debugLog("first not null and state complete and parse complete, start first and second", wholeTask);
					startTask(wholeTask.getFirstTask());
					fetchOneSecondStart(wholeTask);
				} else {
					debugLog("first not null and state complete and parse no complete, parse", wholeTask);
					mSecondTaskHandler.fill(wholeTask);
					if(mSecondTaskHandler.isError()) {
						setError(wholeTask);
					} else {
						if(mSecondTaskHandler.isSplit()) {
							setComplete(wholeTask);	
						} else {
							fetchOneSecondStart(wholeTask);
						}
					}
				}
			} else {
				debugLog("first not null and state not complete", wholeTask);
				startTask(wholeTask.getFirstTask());
				fetchOneSecondStart(wholeTask);
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// operate
	@Override
	public void remove(Task aTask) {
		
		// 对数据库的删除交给基类，会删除同refer的所有记录
		BigSiteTask wholeTask = aTask.toBig();
		BigSiteTask firstTask = wholeTask.getFirstTask();
		if(firstTask != null) {
			mAccessor.getExeAdpater().remove(firstTask);
		}
		for(BigSiteTask t: wholeTask.getSecondTasks()) {
			mAccessor.getExeAdpater().remove(t);
		}
		wholeTask.setFirstTask(null);
		wholeTask.getSecondTasks().clear();
		logger.d("removed first and seconds");
		super.remove(wholeTask);
	}

	/**
	 * 在这里向外发事件，设置总任务状态
	 * 不等到回调,因为还有嗅探之类的
	 * 子任务状态还是要等到回答在再设置
	 */
	@Override
	public void start(Task aTask) {
		BigSiteTask wholeTask = aTask.toBig();
		
		switch(wholeTask.getState()) {
		
		case Task.State.Start:
			debugLog("started, need not start", wholeTask);
			mAccessor.postEvent(CallbackEventId.OnStart, wholeTask);
			if(wholeTask.getFirstTask() != null) {
				if(wholeTask.getFirstTask().getHandle() != 0) {
					mAccessor.postEvent(CallbackEventId.OnPlay, wholeTask);
				}
			}
			break;

		case Task.State.Queue:
		case Task.State.Stop:
		case Task.State.Error:
			{
				forceStart(wholeTask);
				setStart(wholeTask);
			}
			break;
			
		case Task.State.Complete:
			{
				if(!isFileExist(wholeTask)) {
					debugLog("complete, but file not exist", wholeTask);
					remove(wholeTask);
					wholeTask.clearState(); // 只清空了whole任务的成员，sub任务在remove中被置空了
					create(wholeTask);
					start(wholeTask);
					
				} else {
					debugLog("complete, need not start", wholeTask);
					mAccessor.postEvent(CallbackEventId.OnStart, wholeTask);
				}
			}
			break;
		}
	}

	/**
	 * 在这里向外发事件，设置总任务状态
	 */
	@Override
	public void stop(Task aTask) {
		BigSiteTask wholeTask = aTask.toBig();
		
		switch(wholeTask.getState()) {
		case Task.State.Start:
			debugLog("need stop", aTask);
			stopAllSub(wholeTask);
			setStop(wholeTask);
			break;
			
		case Task.State.Queue:
			debugLog("need only set stop", aTask);
			setStop(wholeTask);
			break;
			
		case Task.State.Stop:
		case Task.State.Complete:
		case Task.State.Error:
			debugLog("not started, need not stop", aTask);
			mAccessor.postEvent(CallbackEventId.OnStop, aTask);
			break;
		}
	}

	@Override
	public void query(Task aTask) {
		
		BigSiteTask wholeTask = aTask.toBig();
		if(wholeTask.getState() == Task.State.Start) {
			BigSiteTask firstTask = wholeTask.getFirstTask();
			if(firstTask != null && firstTask.getState() == Task.State.Start) {
				mAccessor.getExeAdpater().query(firstTask);
			}
			for(BigSiteTask t: wholeTask.getSecondTasks()) {
				if(t.getState() == Task.State.Start) {
					mAccessor.getExeAdpater().query(t);
				}
			}
		}
	}

	@Override
	public void error(Task aTask) {
		BigSiteTask wholeTask = aTask.toBig();
		stopAllSub(wholeTask);
		super.error(wholeTask);
	}

	@Override
	public void queue(Task aTask) {
		
		BigSiteTask wholeTask = aTask.toBig();
		stopAllSub(wholeTask);
		super.setQueue(wholeTask);
	}

	@Override
	public void startPlay(Task task) {
		Task firstTask = task.toBig().getFirstTask();
		if(firstTask != null) {
			firstTask.setPlaying(true);
			if(firstTask.getHandle() != 0) {
				mAccessor.getQueryAdapter().setPlaying(firstTask);
			}
		}
	}

	@Override
	public void endPlay(Task task) {
		BigSiteTask firstTask = task.toBig().getFirstTask();
		if(firstTask != null) {
			firstTask.setPlaying(false);
			mAccessor.getQueryAdapter().setPlaying(firstTask);
		}
	}

	@Override
	public boolean isFileExist(Task aTask) {
		
		BigSiteTask wholeTask = aTask.toBig();
		BigSiteTask firstTask = wholeTask.getFirstTask();
		
		if(firstTask == null) {
			return false;
		}
		
		if(wholeTask.getPlayType() == BigSiteTask.PlayType.P2P) {
			return mAccessor.getQueryAdapter().isFileExist(firstTask);
		}
		
		String path = getSavePath() + aTask.getFolderName() + "/";
		if(!new File(path + firstTask.getFileName()).exists()) {
			return false;
		}
		for(Task t: wholeTask.getSecondTasks()) {
			if(!new File(path + t.getFileName()).exists()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public P2PBlock getBlock(Task task) {
		return null;
	}

	@Override
	public void startup(Task aTask) {

		BigSiteTask wholeTask = aTask.toBig();
		switch(wholeTask.getState()) {
		case Task.State.Start:
		case Task.State.Queue:
			{
				debugLog("set stop whole", wholeTask);
				setStop(wholeTask);
				setAllSubStop(wholeTask);
			}
			break;
		case Task.State.Stop:
		case Task.State.Complete:
		case Task.State.Error:
			break;
		}
	}

	@Override
	public void shutdown(Task aTask) {
		saveAll(aTask.toBig());
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// callback
	@Override
	public void onSuccess(int eventId, Task task) {

		// 在集合中已不到此任务了，不再做任何处理
		if(eventId == CallbackEventId.OnRemove) {
			return;
		}

		BigSiteTask inTask = findTaskByCallback(task);
		if(inTask == null) {
			logger.w("have not callback task in onSuccess " + (task == null ? "null task" : task.getRefer()));
			return;
		}
		switch(eventId) {
		
		case CallbackEventId.OnCreate:
			onCreate(inTask, task);
			break;

		case CallbackEventId.OnQuery:
			onQuery(inTask, task);
			break;
			
		case CallbackEventId.OnStart:
			onStart(inTask, task);
			break;
			
		case CallbackEventId.OnStop:
			onStop(inTask, task);
			break;
		}
	}

	@Override
	public void onError(int eventId, Task task, int errorCode) {
		if(eventId == CallbackEventId.OnCreate) {
			BigSiteTask inTask = findTaskByCallback(task);
			if(inTask == null) {
				logger.w("have not callback task in onError " + (task == null ? "null task" : task.getRefer()));
				return;
			}
			onError(inTask);
		}
	}

	private void onCreate(BigSiteTask iTask, Task oTask) {
		debugLog("onCreate", oTask);
		iTask.setHandle(oTask.getHandle());
		if(oTask.toBig().getSubType() == BigSiteTask.SubType.First) {
			iTask.getParent().setHandle(iTask.getHandle());
		}
	}

	private void onQuery(BigSiteTask iTask, Task oTask) {
		logger.v("onQuery " + oTask.getInfo());
		int oldState = iTask.getState();
		int newState = oTask.getState();
		iTask.copyFrom(oTask);
		
		if(oldState != Task.State.Error && newState == Task.State.Error) {
			onError(iTask);
			return;
		}
		
		onSubQuery(iTask);
		
		if(oldState != Task.State.Complete && newState == Task.State.Complete) {
			onSubComplete(iTask);
		}
	}
	
	private void onStart(BigSiteTask iTask, Task oTask) {
		debugLog("onStart", oTask);
		// 由于异步的原因，可能已经完成后收到开始或停止的消息
		if(iTask.getState() == Task.State.Complete) {
			return;
		}
		setStart(iTask);
		BigSiteTask wholeTask = iTask.toBig().getParent();
		BigSiteTask firstTask = wholeTask.getFirstTask();
		if(firstTask.isPlaying()) {
			mAccessor.getQueryAdapter().setPlaying(firstTask);
			mAccessor.postEvent(CallbackEventId.OnPlay, wholeTask);
		}
	}
	
	private void onStop(BigSiteTask iTask, Task oTask) {
		debugLog("onStop", iTask);
		// 同上
		if(iTask.getState() == Task.State.Complete) {
			return;
		}
		setStop(iTask);
	}

	private void onError(BigSiteTask iTask) {
		logger.e("onError " + iTask.getKey() + " " + iTask.getUrl() + " " + iTask.getErrorCode());
		setError(iTask);
		BigSiteTask wholeTask = iTask.getParent();
		if(wholeTask.getState() != Task.State.Error) {
			logger.e("any error, whole error " + wholeTask.getRefer());
			wholeTask.setErrorCode(iTask.getErrorCode());
			setError(wholeTask);
		}
		setAllSubStop(wholeTask);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// helper
	@Override
	protected boolean needPostEvent(Task aTask) {
		return aTask.toBig().getSubType() == BigSiteTask.SubType.Whole;
	}
	
	@Override
	protected boolean needRemoveOnComplete(Task aTask) {
		if(aTask.toBig().getSubType() == BigSiteTask.SubType.Whole) {
			return false;
		}
		// 可能正在播放p2p需要使用
		if(aTask.toBig().getDiskFile() > 1) {
			return false;
		}
		return true;
	}

	/**
	 * 开始任务
	 */
	private void startTask(BigSiteTask task) {
		
		if(task.getState() == Task.State.Start || task.getState() == Task.State.Complete) {
			debugLog("need not startTask", task);
		} else {
			debugLog("startTask", task);
			mAccessor.getExeAdpater().start(task);
		}
	}

	/**
	 * 子任务完成
	 */
	private void onSubComplete(BigSiteTask task) {

		debugLog("onSubComplete ", task);
		if(task.getSubType() == BigSiteTask.SubType.First) {
			BigSiteTask firstTask = task.getParent().getFirstTask();
			String url = mAccessor.getQueryAdapter().getRedirectUrl(firstTask);
			if(!"".equalsIgnoreCase(url)) {
				firstTask.setUrl(url);
				getDBWriter().updateTask(firstTask);
			}
			setComplete(task);
			onFirstComplete(task);
		}
		if(task.getSubType() == BigSiteTask.SubType.Second) {
			setComplete(task);
			onSecondComplete(task);
		}
	}

	/**
	 * 一级任务下载完成
	 */
	private void onFirstComplete(BigSiteTask aTask) {
		debugLog("onFirstComplete", aTask);
		BigSiteTask wholeTask = aTask.getParent();
		mSecondTaskHandler.fill(wholeTask);
		if(mSecondTaskHandler.isError()) {
			setError(wholeTask);
		} else {
			if(mSecondTaskHandler.isSplit()) {
				setComplete(wholeTask);
			} else {
				// 可能上层stop，但恰好底层的oncomplete后开始了一个新的，开始的回调还没有回来，所以子任务没有start的，就没有stop，但是逐个下载的流程却走下去
				if(wholeTask.getState() == Task.State.Start) {
					fetchOneSecondStart(wholeTask);
				}
			}
		}
	}

	/**
	 * 二级任务下载完成
	 */
	private void onSecondComplete(BigSiteTask aTask) {
		debugLog("onSecondComplete", aTask);
		BigSiteTask wholeTask = aTask.getParent();
		
		if(!wholeTask.isComputeTotalSize()) {
			if(aTask.getTotalSize() != 0) {
				if(aTask.getDuration() > Const.M3U8ValidBlockDuration) {
					wholeTask.setTotalSize((int)((double)aTask.getTotalSize() / (double)aTask.getDuration() * (double)wholeTask.getDuration()));
					wholeTask.setComputeTotalSize(true);
				}
			}
		}
		
		// 同上
		if(wholeTask.getState() == Task.State.Start) {
			if(fetchOneSecondStart(wholeTask)) {
				return;
			}
		}
		
		if(isAllComplete(wholeTask)) {
			debugLog("all complete, whole complete", wholeTask);
			// 针对m3u8的处理
			wholeTask.setTotalSize(wholeTask.getDownloadSize());
			if(mSecondTaskHandler.replaceUrls(wholeTask)) {
				setComplete(wholeTask);
			} else {
				setError(wholeTask, Task.ErrorCode.ReplaceFail);
			}
		}
	}
	
	/**
	 * 任务查询处理
	 */
	private void onSubQuery(BigSiteTask task) {
		
		BigSiteTask wholeTask = task.getParent();
		BigSiteTask firstTask = wholeTask.getFirstTask();
		
		int completeDuration = 0;
		long downloadSize = 0;
		long totalSize = 0;
		int speed = 0;
		
		if(firstTask != null) {
			downloadSize += firstTask.getDownloadSize();
			totalSize += firstTask.getTotalSize();
			speed += firstTask.getSpeed();
		}
		
		for(BigSiteTask t: wholeTask.getSecondTasks()) {
			if(t.getState() == Task.State.Complete) {
				double percent = t.getTotalSize() == 0 ? 0 : (double)t.getDownloadSize() / (double)t.getTotalSize();
				completeDuration += (int)((double)t.getDuration() * percent);
			}
			downloadSize += t.getDownloadSize();
			totalSize += t.getTotalSize();
			speed += t.getSpeed();
		}
		
		wholeTask.setDownloadSize(downloadSize);
		if(speed == 0 && wholeTask.getZeroTime() < 3) {
			wholeTask.incZeroTime();
		} else {
			wholeTask.setSpeed(speed);
		}
		
		if(!wholeTask.isComputeTotalSize()) {
			wholeTask.setTotalSize(totalSize);
		}
		if(wholeTask.getSecondTasks().isEmpty()) {
			wholeTask.setPercent(
					totalSize == 0 ? 0 : (int)(100 * downloadSize / totalSize));
		} else {
			wholeTask.setPercent(
					wholeTask.getDuration() == 0 ? 0 : 100 * completeDuration / wholeTask.getDuration());
		}
		// 异常处理
		if(downloadSize > wholeTask.getTotalSize()) {
			wholeTask.setTotalSize((long)((double)downloadSize / (double)wholeTask.getPercent() * (double)100));
			wholeTask.setComputeTotalSize(true);
		}
		if(wholeTask.getDiskFile() == 0) {
			wholeTask.setDiskFile(firstTask.getDiskFile());
		}
		
		boolean isDirty = false;
		if(wholeTask.getPlayType() == BigSiteTask.PlayType.Unknown) {
			if(firstTask.getDiskFile() > 1) {
				wholeTask.setPlayType(BigSiteTask.PlayType.P2P);
			} else if(firstTask.getDiskFile() == 1) {
				wholeTask.setPlayType(BigSiteTask.PlayType.File);
			} else {
				logger.d("0 diskfile " + wholeTask.getRefer());
			}
			isDirty = true;
		}
		
		if(!wholeTask.getFileName().equals(firstTask.getFileName())) {
			wholeTask.setFileName(firstTask.getFileName());
			isDirty = true;
		}
		if(isDirty) {
			getDBWriter().updateTask(wholeTask);
		}
	}
	
	/**
	 * 取得一个二级任务开始下载
	 * return 是否开始了一个下载
	 */
	private boolean fetchOneSecondStart(BigSiteTask wholeTask) {
		
		for(BigSiteTask t: wholeTask.getSecondTasks()) {
			boolean canStart = (t.getState() == Task.State.Stop || t.getState() == Task.State.Error);
			if(canStart) {
				debugLog("fetch one second start", t);
				mAccessor.getExeAdpater().start(t);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 停止所有子任务
	 */
	private void stopAllSub(BigSiteTask wholeTask) {
		BigSiteTask firstTask = wholeTask.getFirstTask();
		if(firstTask != null && firstTask.getState() == Task.State.Start) {
			debugLog("stop first", firstTask);
			mAccessor.getExeAdpater().stop(firstTask);
		}
		for(BigSiteTask t: wholeTask.getSecondTasks()) {
			if(t.getState() == Task.State.Start) {
				debugLog("stop second", t);
				mAccessor.getExeAdpater().stop(t);
			}
		}
	}
	
	/**
	 * 对所有子任务设置状态为停止
	 */
	private void setAllSubStop(BigSiteTask wholeTask) {
		BigSiteTask firstTask = wholeTask.getFirstTask();
		if(firstTask != null && firstTask.getState() == Task.State.Start) {
			debugLog("set first stop", firstTask);
			setStop(firstTask);
		}
		
		for(BigSiteTask t: wholeTask.getSecondTasks()) {
			if(t.getState() == Task.State.Start) {
				debugLog("set second stop", t);
				setStop(t);
			}
		}
	}

	private void saveAll(BigSiteTask wholeTask) {
		DBWriter writer = getDBWriter();
		if(wholeTask.getState() == Task.State.Start) {
			writer.updateTask(wholeTask);
		}
		BigSiteTask firstTask = wholeTask.getFirstTask();
		if(firstTask != null && firstTask.getState() == Task.State.Start) {
			writer.updateTask(firstTask);
		}
		for(BigSiteTask t: wholeTask.getSecondTasks()) {
			if(t.getState() == Task.State.Start) {
				writer.updateTask(t);
			}
		}
	}

	/**
	 * 通过回调参数获得对应的任务对象
	 */
	private BigSiteTask findTaskByCallback(Task value) {
		if(value == null) {
			logger.e("findTaskByCallback null param");
			return null;
		}
		if(value.getType() != Task.Type.Big) {
			logger.e("findTaskByCallback param is not big " + value.getRefer());
			return null;
		}
		if(value.toBig().getSubType() == BigSiteTask.SubType.Whole) {
			logger.e("findTaskByCallback but whole task " + value.getRefer());
			return null;
		}
		Task task = findTask(value.toBig().getParent());
		if(task == null) {
			return null;
		}
		if(task.getType() != Task.Type.Big) {
			logger.e("findTaskByCallback task is not big " + task.getRefer());
			return null;
		}
		BigSiteTask wholeTask = task.toBig();
		BigSiteTask firstTask = wholeTask.getFirstTask();
		if(firstTask != null) {
			if(firstTask.getUrl().equalsIgnoreCase(value.getUrl())) {
				return firstTask;
			}
		}
		return findSecondTask(wholeTask, value.getUrl());
	}

	private boolean isAllComplete(BigSiteTask wholeTask) {
		int state = wholeTask.getFirstTask().getState(); 
		if(state != Task.State.Complete) {
			return false;
		} else {
			for(BigSiteTask t: wholeTask.getSecondTasks()) {
				if(t.getState() != Task.State.Complete) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 根据url查找二级任务
	 */
	private BigSiteTask findSecondTask(BigSiteTask wholeTask, String url) {
		
		for(BigSiteTask t: wholeTask.getSecondTasks()) {
			if(url.equalsIgnoreCase(t.getUrl())) {
				return t;
			}
		}
		return null;
	}
	
	private BigSiteTask createFirstTask(BigSiteTask wholeTask) {
		BigSiteTask firstTask = TaskFactory.create(Task.Type.Big).toBig();
		firstTask.setSubType(BigSiteTask.SubType.First);
		firstTask.setUrl(wholeTask.getUrl());
		firstTask.setParent(wholeTask);
		firstTask.setFileName(FileUtil.filterName(wholeTask.getName() + getSaveFileNameExt()));
		firstTask.setFolderName(wholeTask.getFolderName());
		wholeTask.setFirstTask(firstTask);
		return firstTask;
	}

	private String getSaveFileNameExt() {
		Configuration conf = (Configuration)mAccessor.getServiceFactory().getServiceProvider(Configuration.class);
		return conf.getTaskFileNameExt();
	}
}
