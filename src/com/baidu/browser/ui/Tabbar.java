package com.baidu.browser.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.baidu.browser.framework.BPFrameView;
import com.baidu.hd.R;

public class Tabbar extends RelativeLayout implements OnClickListener{

	
	private TabScrollView mTabScrollView ;
	private ImageView mAddTabButton;
	
	private BPFrameView mBPFrameView;
	
	public Tabbar(Context context, BPFrameView frame) {
		super(context);
        LayoutInflater factory = LayoutInflater.from(context);
        factory.inflate(R.layout.tab_bar, this);
        
        mTabScrollView = (TabScrollView) findViewById(R.id.tab_scroolview);
        mAddTabButton = (ImageView) findViewById(R.id.addtab_btn);
        mAddTabButton.setOnClickListener(this);
        
        mBPFrameView = frame;
	}

	public void addTab(TabView mTabView) {
		mTabScrollView.addTab(mTabView);
	}

	public void removeTab(TabView mTabView) {
		mTabScrollView.removeTab(mTabView);
	}

	@Override
	public void onClick(View v) {
		mBPFrameView.createWindowFromMultiWindow();
	}

	public void ensureChildVisible(TabView mTabView) {
		mTabScrollView.ensureChildVisible(mTabView);
	}

}
