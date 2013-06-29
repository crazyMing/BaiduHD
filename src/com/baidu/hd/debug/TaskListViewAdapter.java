package com.baidu.hd.debug;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.hd.module.Task;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

class TaskListViewAdapter extends BaseAdapter {

	private LayoutInflater mInflater = null;
	private List<Task> mTasks = new ArrayList<Task>();

	private final class ViewHolder {
		TextView txtName = null;
		TextView txtSpeed = null;
		TextView txtState = null;
	}

	public TaskListViewAdapter(Context context) {
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mTasks.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder = null;

		if (null == convertView) {
			viewHolder = new ViewHolder();

			convertView = mInflater
					.inflate(R.layout.debug_task_list_item, null);
			viewHolder.txtName = (TextView) convertView.findViewById(R.id.name);
			viewHolder.txtSpeed = (TextView) convertView
					.findViewById(R.id.speed);
			viewHolder.txtState = (TextView) convertView
					.findViewById(R.id.state);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Task task = mTasks.get(position);
		viewHolder.txtState.setText(task.getFormatState());
		viewHolder.txtSpeed.setText(StringUtil.formatSpeed(task.getSpeed()));
		viewHolder.txtName.setText(task.getName());

		return convertView;

	}

	public void fillList(List<Task> list) {
		mTasks = list;
	}
}
