package com.baidu.hd.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;


public class ProgressDialogAsyncTask<T, P, R> extends AsyncTask<T, P, R>
{
	private ProgressDialogTask<T, P, R> task;
	private ProgressDialog dialog = null;
	private Context context;

	public interface ProgressDialogTask<T, P, R>
	{
		void onPreExecute(ProgressDialogAsyncTask<T, P, R> asysncTask, ProgressDialog dialog);
		R doInBackground(T... param);
		void onProgressUpdate(ProgressDialog dialog, P... values);
		void onPostExecute(R result);
	}
	
	public ProgressDialogAsyncTask(Context context, ProgressDialogTask<T, P, R> todo)
	{
		this.context = context;
		this.task = todo;
	}

	@Override
	protected R doInBackground(T... params)
	{
		if (this.task != null)
		{
			return this.task.doInBackground(params);
		}
		else
		{
			return null;
		}
	}
	
	@Override
	protected void onPreExecute()
	{
		this.dialog = new ProgressDialog(this.context);
		if (this.task != null)
		{
			this.task.onPreExecute(this, this.dialog);
		}
	}

	@Override
	protected void onPostExecute(R result)
	{
		this.dialog.dismiss();
		this.dialog = null;
		if (this.task != null)
		{
			this.task.onPostExecute(result);
		}
	}

	@Override
	protected void onProgressUpdate(P... values)
	{
		// TODO 找到多少个
		if (this.task != null)
		{
			this.task.onProgressUpdate(this.dialog, values);
		}
	}
	
	public void upDate(P... progress)
	{
		publishProgress(progress);
	}
}
