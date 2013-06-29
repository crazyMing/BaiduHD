package com.baidu.hd.event;

import com.baidu.hd.service.ServiceProvider;

public interface EventCenter extends ServiceProvider {

	void addListener(EventListener value);
	void addListener(EventId id, EventListener listener);
	void removeListener(EventListener value);
	void fireEvent(EventId id, EventArgs args);
}
