package com.baidu.hd.personal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.R;

public class PersonalDeleteDialog extends BaseActivity implements OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personal_delete_dialog);
		findViewById(R.id.per_del_dlg_positiveButton).setOnClickListener(this);
		findViewById(R.id.per_del_dlg_negativeButton).setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(PersonalDeleteDialog.this, PersonalActivity.class);
		setResult(RESULT_CANCELED, intent);
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(PersonalDeleteDialog.this, PersonalActivity.class);
		if (getIntent().getExtras() != null) {
			intent.putExtra("LongPressed", getIntent().getExtras().getInt("LongPressed"));
		}
		
		switch (v.getId()) {
		case R.id.per_del_dlg_positiveButton:
			setResult(RESULT_OK, intent);
			finish();
			break;
		case R.id.per_del_dlg_negativeButton:
			setResult(RESULT_CANCELED, intent);
			finish();
			break;
		
		default:
			break;
		}
	}
}
