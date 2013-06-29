package com.baidu.hd.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import com.baidu.hd.adapter.EditableAdapter.Callback;
import com.baidu.hd.event.EventArgs;
import com.baidu.hd.event.EventId;
import com.baidu.hd.event.EventListener;
import com.baidu.hd.module.FolderItemPackage;
import com.baidu.hd.personal.SDCardUtil;
import com.baidu.hd.personal.ScanAsyncTask;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * ���û���·��Adapter
 * @author sunjianshun
 *
 */
public class SettingsBufferPathAdapter extends BaseAdapter {

	private List<FolderItemPackage> mItems = new ArrayList<FolderItemPackage>();
	private LayoutInflater mInflater = null;
	private Context mContext = null;
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder = null;
		
		if (convertView == null) { 
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.folder_item, null);
			viewHolder.folderImage = (ImageView) convertView.findViewById(R.id.folder_icon);
			viewHolder.folderName = (TextView) convertView.findViewById(R.id.folder_name);
			viewHolder.videoTypeTextView = (TextView)convertView.findViewById(R.id.folder_item_right_layout_type);
			viewHolder.videoSizeTextView = (TextView)convertView.findViewById(R.id.folder_item_right_layout_size);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		// ����ѡ��ť
		convertView.findViewById(R.id.folder_item_selected).setVisibility(View.GONE);
		
		viewHolder.folderImage.setImageResource(mItems.get(position).getIconResId());
		viewHolder.folderName.setText(mItems.get(position).getFileName());
		FolderItemPackage item = mItems.get(position);
		
		// ������һ��Ŀ¼��
		if (mItems.get(position).isToParent()) {
			viewHolder.folderImage.setVisibility(View.GONE);
			viewHolder.videoTypeTextView.setVisibility(View.GONE);
			viewHolder.videoSizeTextView.setVisibility(View.GONE);
		}
		// �ļ���
		else if (mItems.get(position).getFolderName() == null) {
			viewHolder.folderImage.setVisibility(View.GONE);
			viewHolder.videoTypeTextView.setVisibility(View.VISIBLE);
			viewHolder.videoSizeTextView.setVisibility(View.VISIBLE);
			viewHolder.videoTypeTextView.setText(SDCardUtil.getInstance().getFileNameExtra(item.getFile()));			
			viewHolder.videoSizeTextView.setText(StringUtil.formatSize(new File(item.getFile().getPath()).length()));
		}
		// �ļ�����
		else {
			viewHolder.folderImage.setVisibility(View.VISIBLE);
			viewHolder.videoTypeTextView.setVisibility(View.GONE);
			viewHolder.videoSizeTextView.setVisibility(View.GONE);
		}
				
		convertView.findViewById(R.id.folder_item_center_layout).setBackgroundDrawable(null);
		convertView.findViewById(R.id.folder_item_selected).setBackgroundDrawable(null);
		return convertView;
	}

	/** ��¼·�� */
	private Stack<String> mDirStack = new Stack<String>();
	
	private final class ViewHolder {
		public ImageView folderImage; // icon ͼ
		public TextView folderName; // �ļ�������
		public TextView videoTypeTextView;
		public TextView videoSizeTextView;
	}
	
	private String mInitPath = "";
	
	public SettingsBufferPathAdapter(Context context, String path) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mInitPath = path;
	}
	
	public String getCurrentPath() {
		String path = "";
		for (String dir : mDirStack) {
			path += dir + "/";
		}
//		path = path.substring(0, path.length() - 1);
		return path;
	}
	
	public boolean fillList(FolderItemPackage item) {
		if (item == null) {
			mDirStack.push(SDCardUtil.getInstance().getSDCardRootDir()); // ��Ŀ¼
			List<String> pathList = getPathListToSDCard(mInitPath);
			for (int i = 0; i < pathList.size(); i++) {
				mDirStack.push(pathList.get(i));
			}
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
		String currentPath = getCurrentPath();
		File rootFile = new File(currentPath);
		if (!rootFile.canRead()) return false;
		
		File[] files = rootFile.listFiles();
		// δ����SD��ʱ�����ص���null
		// ��SD��û���ļ�ʱ������File[0]
		if (files == null) return false;
		for (File file : files) {
			// û�пɶ�Ȩ�ޣ������г�����������
			if (!file.canRead()) {
				continue;
			}
			
			if (file.isDirectory()) {
				FolderItemPackage i = new FolderItemPackage(this.mContext);
				i.setFile(file);
				itemsDir.add(i);
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
		if (rootFile.getParent() != null && !rootFile.getAbsolutePath().equalsIgnoreCase(SDCardUtil.getInstance().getSDCardRootDir())) {
			FolderItemPackage i = new FolderItemPackage(this.mContext);
			i.setFile(null);
			mItems.add(i);
		}
		mItems.addAll(itemsDir);
		
		this.notifyDataSetChanged();
		return true;
	}
	
	public boolean toParent() {
		if (mDirStack.size() < 2) return false;
		mDirStack.pop();
		fillList();
		return true;
	}
	
	public int getIndex(String fileName) {
		
		for (int i = 0; i < mItems.size(); i++) {
			FolderItemPackage item = mItems.get(i);
			if (item.getFolderName().equals(fileName)) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * ��һ��·�������õ�ֱ��SDCard��Ϊֹ��·������
	 * TODO ��ע�⣺��������Ż�����֪���Ƿ����ͨ���ַ���SDCard�������������������������ķ���
	 * @param value
	 * @return
	 */
	public static List<String> getPathListToSDCard(String value) {
		List<String> list = new ArrayList<String>();
		
		if (StringUtil.isEmpty(value)) {
			return list;
		}
		
		File file = new File(value);
		
		while(file!= null && file.canRead() && !file.getAbsolutePath().equalsIgnoreCase(SDCardUtil.getInstance().getSDCardRootDir())) {
			list.add(0, file.getName());
			file = file.getParentFile();
		}
		
		return list;
	}
}
