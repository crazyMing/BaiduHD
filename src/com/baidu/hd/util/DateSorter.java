package com.baidu.hd.util;

import java.util.Calendar;

import android.content.Context;

import com.baidu.hd.R;

/**
 * Sorts dates into the following groups:
 *   Today
 *   Yesterday
 *   earlier
 */
public class DateSorter {

	/** TAG.*/
    static final String TAG = DateSorter.class.getSimpleName();
    
    /** DEBUG switch.*/
    static final boolean DEBUG = false;

    /** must be >= 3 */
    public static final int DAY_COUNT = 3;

    /** �ָ���.*/
    private long [] mBins = new long[DAY_COUNT - 1];
    
    /** ÿһ�������.*/
    private String [] mLabels = new String[DAY_COUNT];
    
    /**
     * create a data sorter
     * @param context Application context
     */
    public DateSorter(Context context) {

        Calendar c = Calendar.getInstance();
        beginningOfDay(c);
        
        // Create the bins
        mBins[0] = c.getTimeInMillis(); // Today
        
        c.add(Calendar.DAY_OF_YEAR, -1);
        mBins[1] = c.getTimeInMillis();  // Yesterday

        // build labels
        mLabels[0] = context.getString(R.string.today);
        mLabels[1] = context.getString(R.string.yesterday);
        mLabels[2] = context.getString(R.string.earlier);

    }

    /**
     * ����time�õ����������.
     * @param time time since the Epoch in milliseconds, such as that
     * returned by Calendar.getTimeInMillis()
     * @return an index from 0 to (DAY_COUNT - 1) that identifies which
     * date bin this date belongs to
     */
    public int getIndex(long time) {
        int lastDay = DAY_COUNT - 1;
        for (int i = 0; i < lastDay; i++) {
            if (time > mBins[i]) {
            	return i;
            }
        }
        return lastDay;
    }

    /**
     * �����������Ӧ��label.
     * @param index date bin index as returned by getIndex()
     * @return string label suitable for display to user
     */
    public String getLabel(int index) {
        if (index < 0 || index >= DAY_COUNT) {
        	return "";
        }
        return mLabels[index];
    }


    /**
     * ���ݵ�ǰindex���࣬�����ǰһ����ķָ���(time).
     * @param index date bin index as returned by getIndex()
     * @return date boundary at given index
     */
    public long getBoundary(int index) {
        int lastDay = DAY_COUNT - 1;
        // Error case
        if (index < 0 || index > lastDay) {
        	index = 0;
        }
        // Since this provides a lower boundary on dates that will be included
        // in the given bin, provide the smallest value
        if (index == lastDay) {
        	return Long.MIN_VALUE;
        }
        return mBins[index];
    }

    /**
     * Calcuate 12:00am by zeroing out hour, minute, second, millisecond
     * @param c Calcuate
     */
    private void beginningOfDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }
}
