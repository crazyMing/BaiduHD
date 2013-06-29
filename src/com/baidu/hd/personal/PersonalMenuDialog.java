package com.baidu.hd.personal;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.module.Task;
import com.baidu.hd.R;

public class PersonalMenuDialog extends BaseActivity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initWidgets(getIntent().getExtras());
	}
	
	private void initWidgets(Bundle b) {
		int pageType = b.getInt("PageType");
		
		setContentView(R.layout.personal_menu_dialog);
		
		switch (pageType) {
		case PersonalConst.PERSONAL_PAGE_BUFFER:
			findViewById(R.id.per_buf_menu).setVisibility(View.VISIBLE);
			findViewById(R.id.per_buf_menu_play).setOnClickListener(this);
			findViewById(R.id.per_buf_menu_pause).setOnClickListener(this);
			findViewById(R.id.per_buf_menu_all_pause).setOnClickListener(this);
			findViewById(R.id.per_buf_menu_delete).setOnClickListener(this);
			
			final int state = b.getInt("TaskState");
			final boolean allStopped = b.getBoolean("AllStopped");
			switch (state) {
			case Task.State.Complete:
				// 隐藏“暂停任务”
				findViewById(R.id.per_buf_menu_pause).setVisibility(View.GONE);
				break;
			case Task.State.Start:
			case Task.State.Queue:
				// 显示“暂停任务”
				((TextView)findViewById(R.id.per_buf_menu_pause)).setText(R.string.personal_task_pause);				
				break;
			default:
				// 显示"开始任务"
				((TextView)findViewById(R.id.per_buf_menu_pause)).setText(R.string.personal_task_start);
				break;
			}
			
			if (allStopped) {
				// 显示“全部开始”
				((TextView)findViewById(R.id.per_buf_menu_all_pause)).setText(R.string.personal_all_start);
			}
			else {
				// 显示“全部暂停”
				((TextView)findViewById(R.id.per_buf_menu_all_pause)).setText(R.string.personal_all_pause);
			}
			
			break;
			
		case PersonalConst.PERSONAL_PAGE_LOCAL:
			findViewById(R.id.per_loc_menu).setVisibility(View.VISIBLE);
			findViewById(R.id.per_loc_menu_play).setOnClickListener(this);
			findViewById(R.id.per_loc_menu_delete).setOnClickListener(this);
			break;
			
		case PersonalConst.PERSONAL_PAGE_FAVORITE:
			findViewById(R.id.per_fav_menu).setVisibility(View.VISIBLE);
			findViewById(R.id.per_fav_menu_play).setOnClickListener(this);
			findViewById(R.id.per_fav_menu_detail).setOnClickListener(this);
			findViewById(R.id.per_fav_menu_delete).setOnClickListener(this);
			break;
			
		case PersonalConst.PERSONAL_PAGE_HISTORY:
			findViewById(R.id.per_his_menu).setVisibility(View.VISIBLE);
			findViewById(R.id.per_his_menu_play).setOnClickListener(this);
			findViewById(R.id.per_his_menu_detail).setOnClickListener(this);
			findViewById(R.id.per_his_menu_delete).setOnClickListener(this);
			break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// buffer
		case R.id.per_buf_menu_play:
			setResult(PersonalConst.PERSONAL_BUFFER_PLAY, new Intent(PersonalMenuDialog.this, PersonalActivity.class));
			break;
		case R.id.per_buf_menu_pause:
			setResult(PersonalConst.PERSONAL_BUFFER_PASUE, new Intent(PersonalMenuDialog.this, PersonalActivity.class));
			break;
		case R.id.per_buf_menu_all_pause:
			setResult(PersonalConst.PERSONAL_BUFFER_ALL_PAUSE, new Intent(PersonalMenuDialog.this, PersonalActivity.class));
			break;
		case R.id.per_buf_menu_delete:
			setResult(PersonalConst.PERSONAL_BUFFER_DELETE, new Intent(PersonalMenuDialog.this, PersonalActivity.class));
			break;
			
		// local
		case R.id.per_loc_menu_play:
			setResult(PersonalConst.PERSONAL_LOCAL_PLAY, new Intent(PersonalMenuDialog.this, PersonalActivity.class));
			break;
		case R.id.per_loc_menu_delete:
			setResult(PersonalConst.PERSONAL_LOCAL_DELETE, new Intent(PersonalMenuDialog.this, PersonalActivity.class));
			break;
			
		// favorite
		case R.id.per_fav_menu_play:
			setResult(PersonalConst.PERSONAL_FAVORITE_PLAY, new Intent(PersonalMenuDialog.this, PersonalActivity.class));
			break;
		case R.id.per_fav_menu_detail:
			setResult(PersonalConst.PERSONAL_FAVORITE_DETAIL, new Intent(PersonalMenuDialog.this, PersonalActivity.class));
			break;
		case R.id.per_fav_menu_delete:
			setResult(PersonalConst.PERSONAL_FAVORITE_DELETE, new Intent(PersonalMenuDialog.this, PersonalActivity.class));
			break;
			
		// history
		case R.id.per_his_menu_play:
			setResult(PersonalConst.PERSONAL_HISTORY_PLAY, new Intent(PersonalMenuDialog.this, PersonalActivity.class));
			break;
		case R.id.per_his_menu_detail:
			setResult(PersonalConst.PERSONAL_HISTORY_DETAIL, new Intent(PersonalMenuDialog.this, PersonalActivity.class));
			break;
		case R.id.per_his_menu_delete:
			setResult(PersonalConst.PERSONAL_HISTORY_DELETE, new Intent(PersonalMenuDialog.this, PersonalActivity.class));
			break;
			
			
		
		default:
			break;
		}
		finish();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		onBackPressed();
		return super.onTouchEvent(event);
	}

	@Override
	public void onBackPressed() {
		setResult(PersonalConst.PERSONAL_NULL, new Intent(PersonalMenuDialog.this, PersonalActivity.class));
		finish();
		super.onBackPressed();
	}
}
