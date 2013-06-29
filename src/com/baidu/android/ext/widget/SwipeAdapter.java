package com.baidu.android.ext.widget;

import android.widget.BaseAdapter;

/**
 * ��������item�϶���ɾ���������߼���adapter���̳���BaseAdapter��
 */
public abstract class SwipeAdapter extends BaseAdapter {
    
    /**ֻ��ָ��λ��ִ��ɾ����*/
    public static final int ONLY_REMOVE = -2;
    
    /**
     * ɾ������롣
     * @param removePos �ڸ�λ�ô�ִ��ɾ����
     * @return �����Ϊ{@link #ONLY_REMOVE}�����ڸ�λ�ô�ִ�в��롣
     */
    protected abstract int removeAndInsert(int removePos);
    
    /**
     * �ж�ָ��λ�ô��ܷ�ִ���϶���ɾ����
     * @param position λ�á�
     * @return 64bit(int). {@link #SWIPE_DRAG}��־λΪ1����ʾ�����϶���{@link #SWIPE_REMOVE}��־λΪ1����ʾ����ɾ����
     */
    public abstract int getSwipeAction(int position);
    
    /**�����϶�������ɾ��*/
    public static final int SWIPE_FIXED = 0;
    
    /**�����϶���*/
    public static final int SWIPE_DRAG = 0x01;
    
    /**����ɾ����*/
    public static final int SWIPE_REMOVE = 0x02;
}