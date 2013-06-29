package com.baidu.hd.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;

import com.baidu.hd.util.ListUtil;

public class EventCenterImpl implements EventCenter {

	private class EventListenerPackage
	{
		public EventListenerPackage(EventId id, EventListener listener)	{
			this.id = id;
			this.listener = listener;
		}
		private EventId id;
		public EventId getId() {
			return id;
		}
		private EventListener listener;
		public EventListener getListener() {
			return listener;
		}
	}

	private List<EventListenerPackage> mListeners = new ArrayList<EventListenerPackage>();

	@Override
	public void setContext(Context ctx) {
	}

	@Override
	public void onCreate() {		
	}

	@Override
	public void onDestory() {		
	}

	@Override
	public void onSave() {
	}

	@Override
	public void addListener(EventListener value)	{
		synchronized (this.mListeners) {
			this.mListeners.add(new EventListenerPackage(EventId.eAll, value));
		}
	}
	
	@Override
	public void addListener(EventId id, EventListener listener)	{
		synchronized (this.mListeners) {
			this.mListeners.add(new EventListenerPackage(id, listener));
		}
	}

	@Override
	public void removeListener(EventListener value)	{

		List<EventListenerPackage> delList = new ArrayList<EventListenerPackage>();
		
		synchronized (this.mListeners) {
			Iterator<EventListenerPackage> iter = this.mListeners.iterator();
			while(iter.hasNext()) {
				EventListenerPackage pack = iter.next();
				if(pack.getListener() == value) {
					delList.add(pack);
				}
			}
			this.mListeners.removeAll(delList);
		}
	}
	
	@Override
	public void fireEvent(EventId id, EventArgs args)	{
		
		List<EventListenerPackage> listeners = null;
		synchronized (mListeners) {
			listeners = ListUtil.clone(mListeners);
		}
		for(EventListenerPackage pack: listeners) {
			if(pack.getId() == EventId.eAll) {
				pack.getListener().onEvent(id, args);
			}
			if(pack.getId() == id) {
				pack.getListener().onEvent(id, args);
			}
		}
	}
}




