package com.baidu.browser.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.android.ext.widget.SwipeAdapter;
import com.baidu.browser.BPBrowser;
import com.baidu.browser.framework.BPWindow;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.util.UrlUtil;
import com.baidu.hd.R;

public class MultiWindowListAdapter extends SwipeAdapter{
	
	private LayoutInflater mInflater;
	private Context mContext;
	private List<BPWindow> mWindowList;
	private int mCurrentPosition = 0;
	private int currentTitleColor;
	private int currentUrlColor;
	
	private MultiWindowItemCloseListener mMultiWindowItemCloseListener = null;
	
    private final ItemClickListener mItemClickListener = new ItemClickListener();
	
	public MultiWindowListAdapter(Context context,LayoutInflater inflater) {
		super();
		mWindowList = new ArrayList<BPWindow>();
		mContext = context;
		mInflater = inflater;
	}
	
	public void setMultiWindows(List<BPWindow> windows, int position) {
		mWindowList = windows;
		mCurrentPosition = position;
		notifyDataSetChanged();
	}
	
	public void setMultiWindowItemCloseListener(MultiWindowItemCloseListener multiWindowItemCloseListener) {
		mMultiWindowItemCloseListener = multiWindowItemCloseListener;
	}
	
    private class ItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
        	mMultiWindowItemCloseListener.onMultiWindowItemCloseClicked((Integer)v.getTag());
        }
    }

	@Override
	public int getCount() {
		return mWindowList.size();
	}

	@Override
	public Object getItem(int position) {
		return mWindowList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private final class ViewHolder
	{
		ImageView multiwindow_item_icon; 
		ImageView multiwindow_item_delete;
		TextView multiwindow_item_description;
		TextView multiwindow_item_title;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder = null;
		
		currentTitleColor = R.color.multiwindow_item_current_color;
		currentUrlColor = R.color.multiwindow_item_current_color;
		
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.multiwindow_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.multiwindow_item_icon = (ImageView) convertView.findViewById(R.id.multiwindow_item_icon);
            viewHolder.multiwindow_item_delete = (ImageView) convertView.findViewById(R.id.multiwindow_item_delete);
            viewHolder.multiwindow_item_description = (TextView) convertView.findViewById(R.id.multiwindow_item_description);
            viewHolder.multiwindow_item_title = (TextView) convertView.findViewById(R.id.multiwindow_item_title);
           
            viewHolder.multiwindow_item_delete.setOnClickListener(mItemClickListener);
            convertView.setTag(viewHolder);
            
        } else 
        	viewHolder = (ViewHolder)convertView.getTag();

        viewHolder.multiwindow_item_delete.setTag(position);
        
        viewHolder.multiwindow_item_title.setText((position+1) + "." + mWindowList.get(position).getTitle());
        
        String url = mWindowList.get(position).getUrl();
        if (StringUtil.isEmpty(url))
        	url = mWindowList.get(position).getCurrentUrl();
        
        if (url.equals(BPBrowser.HOME_PAGE)) {
        	viewHolder.multiwindow_item_description.setVisibility(View.GONE);
        	viewHolder.multiwindow_item_icon.setImageResource(R.drawable.multi_window_item_icon);	
        }
        else {
        	viewHolder.multiwindow_item_description.setVisibility(View.VISIBLE);
        	
        	viewHolder.multiwindow_item_icon.setImageResource(R.drawable.multi_window_item_icon);	
        	
        	final Drawable drawable = getDrawable(url);
        	
        	final Bitmap bitmap = mWindowList.get(position).getIcon();
        	if (drawable!=null)
        		viewHolder.multiwindow_item_icon.setImageDrawable(drawable);
        	if (bitmap!=null && !url.contains("youku.com")) {
        		viewHolder.multiwindow_item_icon.setImageBitmap(bitmap);
        	}
        }
        
        viewHolder.multiwindow_item_description.setText(url);
        
        if (mCurrentPosition == position) {
        	 viewHolder.multiwindow_item_title.setTextColor(mContext.getResources().getColor(currentTitleColor));
        	 viewHolder.multiwindow_item_description.setTextColor(mContext.getResources().getColor(currentUrlColor));
        } else {
       	 	viewHolder.multiwindow_item_title.setTextColor(mContext.getResources().getColor(R.color.multiwindow_item_title_color));
       	 	viewHolder.multiwindow_item_description.setTextColor(mContext.getResources().getColor(R.color.multiwindow_item_description_color));
        }
        
		return convertView;
	}
	
	private Drawable getDrawable(String site) {
		Drawable drawable = null;
			try {
				String name = UrlUtil.getHost(site).replace(".", "");
				if (name != null && !name.equals("")) {
					name=Environment.getExternalStorageDirectory() + "/baidu/baiduplayer/image/"+name+".png";
					drawable=Drawable.createFromPath(name);
					return drawable;
				}
			} catch (Exception e) {
			}
			
		return drawable;
	}

	@Override
	protected int removeAndInsert(int removePos) {
		return 0;
	}

	@Override
	public int getSwipeAction(int position) {
		return 0;
	}

}
