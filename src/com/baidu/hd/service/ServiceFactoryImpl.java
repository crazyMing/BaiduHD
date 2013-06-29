package com.baidu.hd.service;

import java.util.Hashtable;
import java.util.Map;

import android.content.Context;

import com.baidu.browser.visitesite.SearchKeywordManager;
import com.baidu.browser.visitesite.SearchKeywordManagerImpl;
import com.baidu.browser.visitesite.VisiteSiteManager;
import com.baidu.browser.visitesite.VisiteSiteManagerImpl;
import com.baidu.hd.Product;
import com.baidu.hd.conf.Configuration;
import com.baidu.hd.conf.ConfigurationImpl;
import com.baidu.hd.db.DBReader;
import com.baidu.hd.db.DBReaderImpl;
import com.baidu.hd.db.DBWriter;
import com.baidu.hd.db.DBWriterImpl;
import com.baidu.hd.detect.Detect;
import com.baidu.hd.detect.DetectImp;
import com.baidu.hd.event.EventCenter;
import com.baidu.hd.event.EventCenterImpl;
import com.baidu.hd.image.ImageManager;
import com.baidu.hd.image.ImageManagerImp;
import com.baidu.hd.log.Logger;
import com.baidu.hd.net.Uploader;
import com.baidu.hd.net.UploaderImp;
import com.baidu.hd.player.CyberPlayerHolder;
import com.baidu.hd.playlist.PlayListManager;
import com.baidu.hd.playlist.PlayListManagerImpl;
import com.baidu.hd.sniffer.Sniffer;
import com.baidu.hd.sniffer.SnifferImpl;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.stat.StatImp;
import com.baidu.hd.task.TaskManager;
import com.baidu.hd.task.TaskManagerImp;
import com.baidu.hd.upgrade.RemoteUpgrade;
import com.baidu.hd.upgrade.RemoteUpgradeImp;
import com.baidu.hd.upgrade.Upgrade;
import com.baidu.hd.upgrade.UpgradeImp;
import com.baidu.player.download.DownloadServiceAdapter;
import com.baidu.player.download.DownloadServiceAdapterImp;

public class ServiceFactoryImpl implements ServiceFactory {
	
	private Logger logger = new Logger("ServiceFactoryImpl");

	private Map<Class<? extends ServiceProvider>, ServiceProvider> serviceProiders = 
			new Hashtable<Class<? extends ServiceProvider>, ServiceProvider>();
	
	public ServiceFactoryImpl() {
		
		switch(Product.getProcessType()) {
		case Main:
			this.serviceProiders.put(DBReader.class, new DBReaderImpl());
			this.serviceProiders.put(DBWriter.class, new DBWriterImpl());
			this.serviceProiders.put(EventCenter.class, new EventCenterImpl());
			this.serviceProiders.put(Detect.class, new DetectImp());
			this.serviceProiders.put(Configuration.class, new ConfigurationImpl());
			this.serviceProiders.put(Uploader.class, new UploaderImp());
			this.serviceProiders.put(RemoteUpgrade.class, new RemoteUpgradeImp());
			this.serviceProiders.put(ImageManager.class, new ImageManagerImp());
			this.serviceProiders.put(TaskManager.class, new TaskManagerImp());
			this.serviceProiders.put(DownloadServiceAdapter.class, new DownloadServiceAdapterImp());
			this.serviceProiders.put(PlayListManager.class, new PlayListManagerImpl());
			this.serviceProiders.put(Sniffer.class, new SnifferImpl());
			this.serviceProiders.put(Upgrade.class, new UpgradeImp());
			this.serviceProiders.put(Stat.class, new StatImp());
			this.serviceProiders.put(CyberPlayerHolder.class, new CyberPlayerHolder());
			this.serviceProiders.put(VisiteSiteManager.class, new VisiteSiteManagerImpl());
			this.serviceProiders.put(SearchKeywordManager.class, new SearchKeywordManagerImpl());
			break;
		case Task:
			break;
		case Stat:
			this.serviceProiders.put(Configuration.class, new ConfigurationImpl());
			break;
		}

		// 向消费者设置
		for(ServiceProvider provider: this.serviceProiders.values()) {
			if(provider instanceof ServiceConsumer) {
				((ServiceConsumer)provider).setServiceFactory(this);
			}
		}
	}
	
	@Override
	public ServiceProvider getServiceProvider(Class<? extends ServiceProvider> type) {
		return (ServiceProvider)this.serviceProiders.get(type);
	}
	
	@Override
	public void setContext(Context ctx) {		
		for(ServiceProvider sp: this.serviceProiders.values()) {
			sp.setContext(ctx);
		}
	}
	
	@Override
	public void create() {
		for(ServiceProvider sp: this.serviceProiders.values()) {
			sp.onCreate();
		}
	}
	
	@Override
	public void destory() {

		logger.i("destory");
		
		// 释放其他服务组件时可能需要调用数据库接口，
		// 将数据库的释放操作放到最后进行。
		DBReader dbReader = null;
		DBWriter dbWriter = null;
		
		
		for(ServiceProvider sp: this.serviceProiders.values()) {
			if (sp instanceof DBReaderImpl ) {
				dbReader = (DBReader)sp;
			}
			else if (sp instanceof DBWriterImpl ) {
				dbWriter = (DBWriterImpl)sp;
			}
			else {
				sp.onDestory();
			}
		}
		
		// 释放数据库
		if (dbWriter != null) {
			dbWriter.onDestory();
		}
		if (dbReader != null) {
			dbReader.onDestory();
		}
	}

	@Override
	public void save() {
		for(ServiceProvider sp: this.serviceProiders.values()) {
			sp.onSave();
		}
	}
}
