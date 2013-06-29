package com.baidu.hd.ctrl;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.log.Logger;
import com.baidu.hd.util.CustomDialog;
import com.baidu.hd.R;

public class PopupDialog
{
	Logger logger = new Logger("PopupDialog");
	
	public enum ReturnType
	{
		OK, Cancel,
	}

	public interface Callback
	{
		void onReturn(ReturnType type, boolean checked);
	}

	public class Text
	{
		private Context mContext = null;
		private String mText = null;
		private int mTextId = -1;

		public Text(String text, Context context)
		{
			this.mText = text;
			this.mContext = context;
		}

		public Text(int textId, Context context)
		{
			this.mTextId = textId;
			this.mContext = context;
		}

		public String getText()
		{
			return (this.mText == null ? this.mContext.getString(this.mTextId).toString() : this.mText);
		}
	}

	private Activity mParent = null;
	private boolean mChecked = false;
	private Callback mCallback = null;

	private CustomDialog.Builder mBuilder = null;
	private CustomDialog mDialog = null;

	public PopupDialog(Activity activity, Callback callback)
	{
		Activity act = activity;
		while (act.getParent() != null) {
			act = act.getParent();
		}
		this.mParent = act;
		
	//	this.mParent = activity;
		this.mCallback = callback;
		this.mBuilder = new CustomDialog.Builder(this.mParent);
	}
	
	public Text createText(String text)
	{
		return new Text(text, this.mParent);
	}

	public Text createText(int textId)
	{
		return new Text(textId, this.mParent);
	}

	public PopupDialog setTitle(Text value)
	{
		if (value == null)
		{
			return this;
		}
		this.mBuilder.setTitle(value.getText());
		return this;
	}

	public PopupDialog setMessage(Text value)
	{
		if (value == null)
		{
			return this;
		}
		this.mBuilder.setMessage(value.getText());
		return this;
	}

	public PopupDialog setScrollMessage(Text value)
	{
		if (value == null)
		{
			return this;
		}
		this.mBuilder.setScrollMessage(value.getText());
		return this;
	}
	
	public PopupDialog setCheckBox(Text value)
	{
		return setCheckBox(value, false);
	}
	
	public PopupDialog setCheckBox(Text value, boolean isCheck)
	{
		mChecked = isCheck;
		if (value == null)
		{
			return this;
		}
		View localView = this.mParent.getLayoutInflater().inflate(R.layout.dialog_view, null);
		CheckBox checkbox = (CheckBox) localView.findViewById(R.id.id_normal_checkbox);
		checkbox.setVisibility(View.VISIBLE);
		checkbox.setText(value.getText());
		checkbox.setChecked(mChecked);
		checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			public void onCheckedChanged(CompoundButton paramCompoundButton, boolean paramBoolean)
			{
				mChecked = paramBoolean;
			}
		});
		this.mBuilder.setContentView(localView);
		return this;
	}

	public PopupDialog setPositiveButton(Text value)
	{
		if (value == null)
		{
			return this;
		}
		this.mBuilder.setPositiveButton(value.getText(), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface paramDialogInterface, int paramInt)
			{
				if (mParent instanceof BaseActivity) {
					BaseActivity baseActivity = (BaseActivity) mParent;
					baseActivity.removePopupDialog(PopupDialog.this);
				}
				
				paramDialogInterface.dismiss();
				if (mCallback != null) {
					mCallback.onReturn(ReturnType.OK, mChecked);
				}
			}
		});
		return this;
	}

	public PopupDialog setNegativeButton(Text value)
	{
		if (value == null)
		{
			return this;
		}
		this.mBuilder.setNegativeButton(value.getText(), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface paramDialogInterface, int paramInt)
			{
				if (mParent instanceof BaseActivity) {
					BaseActivity baseActivity = (BaseActivity) mParent;
					baseActivity.removePopupDialog(PopupDialog.this);
				}
				
				paramDialogInterface.dismiss();
				if (mCallback != null)
				{
					mCallback.onReturn(ReturnType.Cancel, mChecked);
				}
			}
		});
		return this;
	}

	public PopupDialog setCancelable(boolean value)
	{
		this.mBuilder.setCancelable(value);
		return this;
	}

	public PopupDialog show()
	{
		logger.i("show");
		
		this.mDialog = this.mBuilder.create();
		this.mDialog.show();
		if (mParent instanceof BaseActivity) {
			BaseActivity baseActivity = (BaseActivity) mParent;
			baseActivity.addPopupDialog(this);
		}
		
		this.mDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				if (mCallback != null)
				{
					mCallback.onReturn(ReturnType.Cancel, false);
				}
			}
		});
		return this;
	}

	public void dismiss()
	{
		if (this.mDialog != null)
		{
			this.mDialog.dismiss();
			this.mDialog = null;
		}
		this.mBuilder = null;
	}
}
