package com.baidu.hd.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.module.FolderItemPackage;
import com.baidu.hd.personal.SDCardUtil;
import com.baidu.hd.personal.ScanAsyncTask;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

/**
 * �ļ����б� Adapter
 * @author sunjianshun
 *
 */
public class FolderAdapter extends EditableAdapter<FolderItemPackage> {
	
	/* �÷���������д��ע����FolderAdapter�ж���һ�������һ��Ŀ¼��������������������������
	 * (non-Javadoc)
	 * @see com.baidu.video.adapter.EditableAdapter#setSelectItemNum(int)
	 */
	@Override
	public void setSelectItemNum(int selectItemNum) {
		if (mDirStack.size() > 1) { // �С�������һ��Ŀ¼����
			
			mSelectItemNum = selectItemNum;
			if (0 == mSelectItemNum) {
				notifyDeleteButton(0);
				notifySelectInverseButton(1);
			} else if (mSelectItemNum == mAllItemNum-1) {
				notifyDeleteButton(1);
				notifySelectInverseButton(2);
			} else if (mSelectItemNum < mAllItemNum-1) {
				notifyDeleteButton(1);
				notifySelectInverseButton(1);
			}
		}
		else { // �ޡ�������һ��Ŀ¼����
			super.setSelectItemNum(selectItemNum);
		}
	}

	@Override
	public void notifyDeleteButton(Integer nIsDeleteEnable) {
		super.notifyDeleteButton(nIsDeleteEnable);
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}
	
	private EventListener mEventListener = new EventListener() {
		
		@Override
		public void onEvent(EventId id, EventArgs args) {
		//	fillList();
		//	notifyDataSetChanged();
		}
	};

	/** ��¼·�� */
	private Stack<String> mDirStack = new Stack<String>();
	
	private final class ViewHolder {
		public ImageView folderSelected = null;
		public ImageView folderImage; // icon ͼ
		public TextView folderName; // �ļ�������
		public TextView videoTypeTextView;
		public TextView videoSizeTextView;
	}
	
	public FolderAdapter(Context context, Callback callback) {
		super(context, callback);
	}
	
	public String getCurrentPath() {
		String path = "";
		for (String dir : mDirStack)
		{
			path += dir + "/";
		}
		path = path.substring(0, path.length() - 1);
		return path;
	}
	
	public boolean fillList(FolderItemPackage item) {
		if (item == null) {
			mDirStack.push(SDCardUtil.getInstance().getSDCardRootDir()); // ��Ŀ¼
		}
		else if (item.isToParent()) {
			mDirStack.pop();  // ������һ��
		}
		else if (item.getFolderName() != null){
			mDirStack.push(item.getFolderName()); // ����ļ���
		}
		else {
			// ���������Ŀǰֻ���ļ�
			return false;
		}
		
		return fillList();
	}
	
	private boolean fillList() {
		
		List<FolderItemPackage> itemsDir = new ArrayList<FolderItemPackage>();
		List<FolderItemPackage> itemsFile = new ArrayList<FolderItemPackage>();
		String currentPath = getCurrentPath();
		File rootFile = new File(currentPath);
		if (!rootFile.canRead()) return false;
		
		File[] files = new File(currentPath).listFiles();
		// δ����SD��ʱ�����ص���null
		// ��SD��û���ļ�ʱ������File[0]
		if (files == null) return false;
		for (File file : files) {
			// û�пɶ�Ȩ�ޣ������г�����������
			if (!file.canRead()) {
				continue;
			}
			
			FolderItemPackage i = new FolderItemPackage(this.mContext);
			i.setFile(file);
			
			if (file.isDirectory()) {
				itemsDir.add(i);
			}
			else if (SDCardUtil.getInstance().isSupported(file.getName())) {
					itemsFile.add(i);
			}
		}
		
		// ����,�ļ������ϣ��ļ�����
		Comparator<FolderItemPackage> compatator = new Comparator<FolderItemPackage>(){

			@Override
			public int compare(FolderItemPackage lhs, FolderItemPackage rhs) {
				FolderItemPackage itemLhs = (FolderItemPackage)lhs;
				FolderItemPackage itemRhs = (FolderItemPackage)rhs;
				return itemLhs.getFolderName().compareToIgnoreCase(itemRhs.getFolderName());
			}
			
		};
		synchronized(compatator) {
			Collections.sort(itemsDir, compatator);
	    }		
		synchronized(compatator) {
			Collections.sort(itemsDir, compatator);
	    }
		
		mItems.clear();
		mAllItemNum = 0;
		if (mDirStack.size() > 1) {
			FolderItemPackage i = new FolderItemPackage(this.mContext);
			i.setFile(null);
			mItems.add(i);
			mAllItemNum += 1;
		}
		mItems.addAll(itemsDir);
		mItems.addAll(itemsFile);
		mAllItemNum += itemsDir.size();
		mAllItemNum += itemsFile.size();
		
		notifyEditButton(mAllItemNum == 0 ? 0 : 1);
		this.notifyDataSetChanged();
		return true;
	}
	
	public boolean toParent() {
		if (mDirStack.size() < 2) return false;
		mDirStack.pop();
		fillList();
		return true;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		
		if (convertView == null) { 
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.folder_item, null);
			viewHolder.folderSelected = (ImageView) convertView.findViewById(R.id.folder_item_selected);
			viewHolder.folderImage = (ImageView) convertView.findViewById(R.id.folder_icon);
			viewHolder.folderName = (TextView) convertView.findViewById(R.id.folder_name);
			viewHolder.videoTypeTextView = (TextView)convertView.findViewById(R.id.folder_item_right_layout_type);
			viewHolder.videoSizeTextView = (TextView)convertView.findViewById(R.id.folder_item_right_layout_size);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.folderImage.setImageResource(mItems.get(position).getIconResId());
		viewHolder.folderName.setText(mItems.get(position).getFileName());
		FolderItemPackage item = mItems.get(position);
		viewHolder.folderSelected.setImageResource(item.isSelectedDel() ? R.drawable.ic_list_check_on : R.drawable.ic_list_check_off);
		
		// ������һ��Ŀ¼��
		if (mItems.get(position).isToParent()) {
			viewHolder.folderSelected.setVisibility(View.GONE);
			viewHolder.folderImage.setVisibility(View.GONE);
			viewHolder.videoTypeTextView.setVisibility(View.GONE);
			viewHolder.videoSizeTextView.setVisibility(View.GONE);
		}
		// �ļ���
		else if (mItems.get(position).getFolderName() == null) {
			viewHolder.folderSelected.setVisibility(View.VISIBLE);
			viewHolder.folderImage.setVisibility(View.GONE);
			viewHolder.videoTypeTextView.setVisibility(View.VISIBLE);
			viewHolder.videoSizeTextView.setVisibility(View.VISIBLE);
			viewHolder.videoTypeTextView.setText(SDCardUtil.getInstance().getFileNameExtra(item.getFile()));			
			viewHolder.videoSizeTextView.setText(StringUtil.formatSize(new File(item.getFile().getPath()).length()));
		}
		// �ļ�����
		else {
			viewHolder.folderSelected.setVisibility(View.VISIBLE);
			viewHolder.folderImage.setVisibility(View.VISIBLE);
			viewHolder.videoTypeTextView.setVisibility(View.GONE);
			viewHolder.videoSizeTextView.setVisibility(View.GONE);
		}
				
		viewHolder.folderSelected.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ImageView commonTaskDelete = (ImageView) v;					
				FolderItemPackage item = mItems.get(position);
				item.setSelectedDel(!item.isSelectedDel());
				setSelectItemNum(getSelectItemNum() + (item.isSelectedDel() ? 1 : -1));
				commonTaskDelete.setImageResource(item.isSelectedDel() ? R.drawable.ic_list_check_on : R.drawable.ic_list_check_off);
				
				if (getSelectItemNum() == 0) {
					// "��ӵ��б�"��ť��ɫ
					notifyDeleteButton(0);
				}
				else {
				//	((FileBrowserActivity)mContext).setImortButtonEnable(true);
					notifyDeleteButton(1);
				}
			}
		});
		convertView.findViewById(R.id.folder_item_center_layout).setBackgroundDrawable(null);
		convertView.findViewById(R.id.folder_item_selected).setBackgroundDrawable(null);
		return convertView;
	}

	/** ��ɾ�������޸�Ϊ������� */
	@Override
	public void removeMarkDeleteItem() {
		List<String> pathList = new ArrayList<String>();
		for (FolderItemPackage item : mItems) {
			if (item.isSelectedDel()) {
				if (!item.isToParent()) {
					pathList.add(item.getFile().toString());
					mSelectItemNum--;
				}
			}
		}
		
		new ScanAsyncTask(mContext, mContext, new ScanAsyncTask.Callback(){

			@Override
			public void onComplete(int nResult) {
				// TODO Auto-generated method stub
				//Toast �ȷŵ�ScanAsyncTask�У����Ժ��б䶯�������������޸�
				// ��ת��LocalActivity
		//		MainTabActivity.setOnResumeAnimation(true);
				((Activity)mContext).finish();
			}

			@Override
			public boolean isNeedRefresh() {
				return false;
			}
		}).execute(pathList);
	}
	
	@Override
	public void onResume() {
		if (!isServiceCreated()) return;
		getEventCenter().addListener(EventId.ePlayListUpdate, mEventListener);
	}

	@Override
	public void onPause() {
		if (!isServiceCreated()) return;
		getEventCenter().removeListener(mEventListener);
	}

	@Override
	public void onServiceCreated() {
		getEventCenter().addListener(EventId.ePlayListUpdate, mEventListener);
	}
	
	public void onItemClick(View paramView) {
		ImageView commonTaskDelete = (ImageView) paramView.findViewById(R.id.folder_item_selected);
		commonTaskDelete.performClick();
	}
}
