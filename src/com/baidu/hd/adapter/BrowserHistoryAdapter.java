package com.baidu.hd.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.browser.SearchManager;
import com.baidu.browser.ui.IconTextView;
import com.baidu.browser.visitesite.VisiteSite;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.util.DateSorter;
import com.baidu.hd.util.Utility;
import com.baidu.hd.R;

/**
 * @ClassName: BrowserHistoryAdapter 
 * @Description: 历史
 * @author LEIKANG 
 * @date 2012-12-25 下午8:49:01
 */
public class BrowserHistoryAdapter extends BaseExpandableListAdapter{

    private DateSorter mDateSorter;
	
    private Context mContext;
    
   private int[] mItemMap = null;
   
   private int[] mItemMapExtraOffset = null;
   
   private int mNumberOfBins;
   
   private List<VisiteSite> mHistorys = new ArrayList<VisiteSite>();
    
    public BrowserHistoryAdapter(Context context) {
    	mContext = context;
        mDateSorter = new DateSorter(context);
    }
    
    public void setHistorys(List<VisiteSite> historys) {
    	mHistorys = historys;
    	buildMap();
    	notifyDataSetChanged();
    }
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
    	if (childPosition < getGroupExtraOffSet(groupPosition)) {
            LayoutInflater factory = LayoutInflater.from(mContext);
            convertView = factory.inflate(R.layout.history_group_emptyview, parent, false);
            convertView.setClickable(false);
            convertView.setLongClickable(false);
            
            return convertView;
            
    	} else {
            IconTextView item;
            if (null == convertView || !(convertView instanceof IconTextView)) {
                item = new IconTextView(mContext);
            } else {
                item = (IconTextView) convertView;
            }
            
            item.setClickable(true);
            item.setLongClickable(true);
            
            item.setTopText(mHistorys.get(childPosition).getSiteTitle());
            item.setLeftView(R.drawable.history_favicon);
            item.setBottomText(mHistorys.get(childPosition).getSiteUrl());
            item.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					final String url = ((IconTextView)v).getBottomText().toString();
					if (Utility.isUrl(url))
						SearchManager.launchURL(mContext,url);
					else if (Utility.isBDHD(url))
						PlayerLauncher.startup(mContext, url);
				}
			});
            
            
            return item;
    	}
	}

	@Override
	public int getChildrenCount(int groupPosition) {
	    return mItemMap[groupPositionToBin(groupPosition)] + mItemMapExtraOffset[groupPosition];
	}

	@Override
	public Object getGroup(int groupPosition) {
		return null;
	}

	@Override
	public int getGroupCount() {
		return mNumberOfBins;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
        View item;
        ViewHolder holder = null;
        if (null == convertView) {
            LayoutInflater factory = LayoutInflater.from(mContext);
            item = factory.inflate(R.layout.history_group_view, parent, false);
            holder = new ViewHolder();
            holder.groupName = (TextView) item.findViewById(R.id.groupName);
            holder.indicator = (ImageView) item.findViewById(R.id.indicator);
            item.setTag(holder);
        } else {
            item = convertView;
            holder = (ViewHolder) item.getTag();
        }
        String label = mDateSorter.getLabel(groupPosition);
        holder.groupName.setText(label);
        if (isExpanded) {
        	holder.indicator.setBackgroundResource(R.drawable.indicator_unfolded_status);
        } else {
        	holder.indicator.setBackgroundResource(R.drawable.indicator_folded_status);
        }
        return item;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
    /**
     * view holder. 
     */
    static class ViewHolder {
    	/** group name.*/
        TextView groupName;
        /** group indicator.*/
        ImageView indicator;
    }
    
    private void buildMap() {
        int[] array = new int[DateSorter.DAY_COUNT];
        
        mItemMapExtraOffset = new int[DateSorter.DAY_COUNT];
        
        for (int j = 0; j < DateSorter.DAY_COUNT; j++) {
            array[j] = 0;
            mItemMapExtraOffset[j] = 0;
        }
 
        mNumberOfBins = DateSorter.DAY_COUNT;
        
        int dateIndex = -1;
        	
        	for (int i = 0; i< mHistorys.size(); i++) {
        		final VisiteSite visiteSite = mHistorys.get(i);
        		
                long date = visiteSite.getVisitedTime();
                int index = mDateSorter.getIndex(date);
                if (index > dateIndex) {
 
                    if (index == DateSorter.DAY_COUNT - 1) {
                        array[index] = mHistorys.size()- i;
                        break;
                    }
                    dateIndex = index;
                }
                array[dateIndex]++;
            }
        	
        mItemMap = array;
        
        for (int j = 0; j < DateSorter.DAY_COUNT; j++) {
        	if (mItemMap[j] == 0) {
        		mItemMapExtraOffset[j] = 1;
        	}
        }
    }
    
    /**
     * 获得每个分类对应的头数目
     * @param groupPosition 组id
     * @return 分类对应的头数目
     */
    public int getGroupExtraOffSet(int groupPosition) {
    	return mItemMapExtraOffset[groupPosition];
    }
    
    
    private int groupPositionToBin(int groupPosition) {
        if (groupPosition < 0 || groupPosition >= DateSorter.DAY_COUNT) {
            throw new AssertionError("group position out of range");
        }
        if (DateSorter.DAY_COUNT == mNumberOfBins || 0 == mNumberOfBins) {
            return groupPosition;
        }
        int arrayPosition = -1;
        while (groupPosition > -1) {
            arrayPosition++;
            if (mItemMap[arrayPosition] != 0) {
                groupPosition--;
            }
        }
        return arrayPosition;
    }

}
