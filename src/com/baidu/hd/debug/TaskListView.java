package com.baidu.hd.debug;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.task.TaskManager;
import com.baidu.hd.R;

public class TaskListView extends LinearLayout {

	private ServiceFactory mServiceFactory = null;

    public TaskListView(final Context ctxt, AttributeSet attrs) {
        super(ctxt,attrs);
    }
    
    public void setServiceFactory(ServiceFactory serviceFactory) {
    	mServiceFactory = serviceFactory;
    }

	protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.debug_task_list, this);
	}
	
	
	@Override
	public void setVisibility(int visibility) {
		if(visibility == View.VISIBLE) {
			show();
		}
		super.setVisibility(visibility);
	}

	public void show() {
		TaskManager taskManager = (TaskManager)mServiceFactory.getServiceProvider(TaskManager.class);
		TaskListViewAdapter adapter = new TaskListViewAdapter(getContext());
		adapter.fillList(taskManager.getAll());
		
		ListView listview = (ListView) findViewById(R.id.playlist);
		listview.setAdapter(adapter);
	}
}
