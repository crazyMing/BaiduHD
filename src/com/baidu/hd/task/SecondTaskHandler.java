package com.baidu.hd.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.chilicat.m3u8.Element;
import net.chilicat.m3u8.ParseException;
import net.chilicat.m3u8.PlayList;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.baidu.hd.conf.Configuration;
import com.baidu.hd.db.DBWriter;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.BigSiteTask;
import com.baidu.hd.module.Task;
import com.baidu.hd.module.TaskFactory;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.util.FileUtil;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.UrlUtil;

/**
 * 大站m3u8任务的子任务的处理器的帮助类，不是taskhandler的实例
 */
class SecondTaskHandler {

	private Logger logger = new Logger("M3u8Optimize");

	private ServiceFactory mServiceFactory = null;

	private boolean mCancel = false;
	private Handler mDBHandler = null;
	
	/** 是否出错 */
	private boolean isError = false;
	
	/** 是否分块 */
	private boolean isSplit = false;

	public void create(ServiceFactory serviceFactory) {
		mServiceFactory = serviceFactory;

		HandlerThread thread = new HandlerThread("m3u8DB");
		thread.start();
		mDBHandler = new Handler(thread.getLooper()) {

			@Override
			public void handleMessage(Message msg) {
				_addToDB((BigSiteTask) msg.obj);
			}
		};
	}

	public void cancel() {
		mCancel = true;
	}

	public boolean isError() {
		return isError;
	}

	public boolean isSplit() {
		return isSplit;
	}

	/**
	 * 填充二级任务
	 * 
	 * @return 是否为分块文件
	 */
	public void fill(BigSiteTask wholeTask) {
		if(wholeTask.getDiskFile() == 0) {
			isError = true;
			return;
		}
		if (wholeTask.getDiskFile() > 1) {
			logger.d("block greater 1, p2p task");
			wholeTask.setParseComplete(true);
			getDBWriter().updateTask(wholeTask);
			isSplit = true;
			return;
		}

		BigSiteTask firstTask = wholeTask.getFirstTask();
		String fileFullName = this.getSavePath()
				+ firstTask.getFolderName() + "/"
				+ firstTask.getFileName();
		if(!new File(fileFullName).exists()) {
			isError = true;
			return;
		}
		Map<String, Integer> datas = this.parseListFile(fileFullName);
		if (datas == null) {
			logger.d("parse m3u8 file error, is mp4 file "
					+ wholeTask.getRefer());
			wholeTask.setParseComplete(true);
			getDBWriter().updateTask(wholeTask);
			isSplit = true;
			return;
		}
		
		String host = UrlUtil.getHost(firstTask.getUrl());

		String fileNameExt = getSaveFileNameExt();
		int totalDuration = 0;
		for (Entry<String, Integer> entry : datas.entrySet()) {
			
			String url = entry.getKey();
			if(url.length() > 7 && !url.substring(0, 7).equalsIgnoreCase("http://")) {
				url = "http://" + host + url;
			}

			BigSiteTask t = TaskFactory.create(Task.Type.Big).toBig();
			t.setUrl(url);
			t.setDuration(entry.getValue());
			t.setRefer(wholeTask.getRefer());
			t.setFileName(getFileName(wholeTask) + fileNameExt);
			t.setSubType(BigSiteTask.SubType.Second);
			t.setFolderName(wholeTask.getFolderName());
			t.setParent(wholeTask);
			wholeTask.getSecondTasks().add(t);
			totalDuration += entry.getValue();
		}
		wholeTask.setParseComplete(true);
		wholeTask.setDuration(totalDuration);
		getDBWriter().updateTask(wholeTask);

		mDBHandler.sendMessage(mDBHandler.obtainMessage(0, wholeTask));
		logger.d("m3u8 file, create seconds end");
		isSplit = false;
	}

	/**
	 * 替换m3u8链接为本地链接
	 */
	public boolean replaceUrls(BigSiteTask aTask) {

		logger.d("replaceUrls " + aTask.getRefer());

		BigSiteTask firstTask = aTask.getFirstTask();
		String savePath = getSavePath();
		String fileName = savePath + aTask.getFolderName() + "/" + firstTask.getFileName();
		try {
			StringBuilder sb = new StringBuilder();

			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line = "";
			while ((line = br.readLine()) != null) {

				BigSiteTask secondTask = _findSecondTask(aTask, line);
				if (secondTask != null) {
					sb.append("file://" + savePath + aTask.getFolderName() + "/" + secondTask.getFileName()
							+ "\r\n");
				} else {
					sb.append(line + "\r\n");
				}
			}
			br.close();

			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			bw.write(sb.toString());
			bw.close();
			return true;

		} catch (FileNotFoundException e) {
			this.logger.e("file not found " + fileName);
		} catch (IOException e) {
			this.logger.e("ioexception " + fileName);
		}
		return false;
	}

	/**
	 * 尝试解析m3u8文件 失败返回null
	 */
	private Map<String, Integer> parseListFile(String fileName) {

		try {
			Map<String, Integer> result = new Hashtable<String, Integer>();

			InputStream is = new FileInputStream(fileName);
			PlayList playList = PlayList.parse(is);

			for (Element element : playList.getElements()) {
				if (element.isMedia()) {
					result.put(element.getURI().toString(),
							element.getDuration());
				}
			}
			return result;

		} catch (ParseException e) {
			this.logger.d("parse fail " + fileName);
		} catch (FileNotFoundException e) {
			this.logger.e("file not found " + fileName);
		}
		return null;
	}

	private void _addToDB(BigSiteTask wholeTask) {
		DBWriter writer = getDBWriter();
		// 只能暂时的解决问题，同样是异步，如果真的很快，还是会有问题
		List<Task> senconds = convert(wholeTask.getSecondTasks());
		if(getDBWriter().batchAddSubTask(senconds)) {
			if(mCancel) {
				return;
			}
			wholeTask.setParseComplete(true);
			writer.updateTask(wholeTask);
		}
	}

	/**
	 * 根据url查找二级任务
	 */
	private BigSiteTask _findSecondTask(BigSiteTask wholeTask, String url) {
		for (BigSiteTask t : wholeTask.getSecondTasks()) {
			if (t.getUrl().endsWith(url)) {
				return t;
			}
		}
		return null;
	}
	
	private List<Task> convert(List<BigSiteTask> value) {
		List<Task> result = new ArrayList<Task>();
		for(BigSiteTask t: value) {
			result.add(t);
		}
		return result;
	}
	
	private String getFileName(BigSiteTask wholeTask) {
		return FileUtil.filterName(wholeTask.getName() + "_" + StringUtil.createUUID());
	}

	private String getSaveFileNameExt() {
		Configuration conf = (Configuration) mServiceFactory
				.getServiceProvider(Configuration.class);
		return conf.getTaskFileNameExt();
	}

	private String getSavePath() {
		Configuration conf = (Configuration) mServiceFactory
				.getServiceProvider(Configuration.class);
		return conf.getTaskSavePath();
	}

	private DBWriter getDBWriter() {
		return (DBWriter) mServiceFactory.getServiceProvider(DBWriter.class);
	}
}
