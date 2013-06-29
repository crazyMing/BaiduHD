package com.baidu.hd.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.baidu.hd.BaiduHD;
import com.baidu.hd.BaseActivity;
import com.baidu.hd.event.EventCenter;


/**
 * ListView适配器
 */
public abstract class EditableAdapter<T> extends BaseAdapter
{
	public interface Callback
	{
		public void onEditButton(Integer nIsEditEnable);
		public void onSelectInverseButton(Integer nIsEditEnable);
		public void onDeleteButton(Integer nIsDeleteEnable);
		public void onStartStopAllButton(Integer nIsStartStopEnable);
	}

	private static final int EditButtonMsg = 1;
	private static final int SelectInverseButtonMsg = 2;
	private static final int DeleteButtonMsg = 3;
	private static final int StartStopAllButtonMsg = 4;
	
	private Callback mCallback = null;
	
	protected Context mContext = null;
	protected LayoutInflater mInflater = null;
	protected BaseActivity mBaseActivity = null;
	protected boolean mIsEditComplete = false;
	
	/** 列表中的所有项 */
	protected int mAllItemNum = 0;
	
	/** 列表中被选择的项 */
	protected int mSelectItemNum = 0;
	
	protected List<T> mItems = new ArrayList<T>();
	
	abstract public void removeMarkDeleteItem();
	abstract public void onResume();
	abstract public void onPause();
	abstract public void onServiceCreated();

	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) 
		{
			if (null == mCallback)
			{
				return;
			}
			Integer i = (Integer)msg.obj;
			if(msg.what == EditButtonMsg) 
			{
				mCallback.onEditButton(i);
			} else if(msg.what == SelectInverseButtonMsg) 
			{
				mCallback.onSelectInverseButton(i);
			} else if(msg.what == DeleteButtonMsg) 
			{
				mCallback.onDeleteButton(i);
			} else if(msg.what == StartStopAllButtonMsg) 
			{
				mCallback.onStartStopAllButton(i);
			}
		}
	};
	
	protected EditableAdapter(Context context, Callback callback) 
	{
		this.mContext = context;
		this.mCallback = callback;
		this.mInflater = LayoutInflater.from(context);
		this.mBaseActivity = (BaseActivity)context;
	}
	
	protected void notifyEditButton(Integer nIsEditEnable)
	{
		this.mHandler.sendMessage(this.mHandler.obtainMessage(EditButtonMsg, nIsEditEnable));
	}
	
	protected void notifySelectInverseButton(Integer nIsSelete)
	{
		this.mHandler.sendMessage(this.mHandler.obtainMessage(SelectInverseButtonMsg, nIsSelete));
	}
	
	protected void notifyDeleteButton(Integer nIsDeleteEnable)
	{
		this.mHandler.sendMessage(this.mHandler.obtainMessage(DeleteButtonMsg, nIsDeleteEnable));
	}
	
	protected void notifyStartStopAllButton(Integer nIsStartStopEnable)
	{
		this.mHandler.sendMessage(this.mHandler.obtainMessage(StartStopAllButtonMsg, nIsStartStopEnable));
	}
	
	protected boolean isServiceCreated() 
	{
		if (null != BaiduHD.cast(mContext))
		{
			return BaiduHD.cast(mContext).getServiceContainer().isCreated();
		}
		return false;
	}
	
	public  List<T> getItems()
	{
		return mItems;
	}
	
	public  int getAllItemNum()
	{
		return mAllItemNum;
	}
	
	public  int getSelectItemNum()
	{
		return mSelectItemNum;
	}
	
	/** 一个都未选择  删除钮 disable
	 * 全选/反选--永远有效，无效时，编辑框根本不会弹出
	 * @param selectItemNum
	 */
	public  void setSelectItemNum(int selectItemNum)
	{
		mSelectItemNum = selectItemNum;
		if (0 == mSelectItemNum)
		{
			//0 disabled
			notifyDeleteButton(0);
			//1 no selected
			notifySelectInverseButton(1);
		}else if (mAllItemNum == mSelectItemNum)
		{
			//1 enabled
			notifyDeleteButton(1);
			//2 is all selected
			notifySelectInverseButton(2);
		}else if (mSelectItemNum < mAllItemNum)
		{
			//1 enabled
			notifyDeleteButton(1);
			//1 is some but not all is selected
			notifySelectInverseButton(1);
		}
	}
	
	public  void setIsEditCancel(boolean bIsEditCancel)
	{
		mIsEditComplete = bIsEditCancel;
	}
	
	protected EventCenter getEventCenter() 
	{
		return (EventCenter)mBaseActivity.getServiceProvider(EventCenter.class);
	}
	
	@Override
	public int getCount()
	{
		return mItems.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		return convertView;
	}
}

