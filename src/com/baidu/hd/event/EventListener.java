package com.baidu.hd.event;

public interface EventListener {
	void onEvent(EventId id, EventArgs args);
}
