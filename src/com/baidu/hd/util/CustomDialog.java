package com.baidu.hd.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.hd.settings.DocShowActivity;
import com.baidu.hd.R;

public class CustomDialog extends Dialog {
	public CustomDialog(Context paramContext) {
		super(paramContext);
	}

	public CustomDialog(Context paramContext, int paramInt) {
		super(paramContext, paramInt);
	}

	public static class Builder {
		private CustomDialog localCustomDialog;
		private boolean cancelable = true;
		private View contentView;
		private Context context;
		private DialogInterface.OnClickListener expandButtonClickListener;
		private String expandButtonText;
		private String message;
		private String scrollMessage;
		private DialogInterface.OnClickListener negativeButtonClickListener;
		private String negativeButtonText;
		private DialogInterface.OnClickListener positiveButtonClickListener;
		private String positiveButtonText;
		private String title;

		public Builder(Context paramContext) {
			this.context = paramContext;
		}

		public CustomDialog create() {
			Object localObject = (LayoutInflater) this.context
					.getSystemService("layout_inflater");
			localCustomDialog = new CustomDialog(this.context, R.style.Dialog);
			localObject = ((LayoutInflater) localObject).inflate(
					R.layout.custom_dialog, null);

			((TextView) ((View) localObject).findViewById(R.id.title))
					.setText(this.title);

			if (this.contentView == null) {
				((LinearLayout) ((View) localObject).findViewById(R.id.content))
						.setVisibility(View.GONE);
			} else {
				((LinearLayout) ((View) localObject).findViewById(R.id.content))
						.removeAllViews();
				((LinearLayout) ((View) localObject).findViewById(R.id.content))
						.addView(this.contentView, new ViewGroup.LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT));
			}

			if (StringUtil.isEmpty(this.positiveButtonText)) {
				((View) localObject).findViewById(R.id.positiveButton)
						.setVisibility(View.GONE);
			} else {
				((Button) ((View) localObject)
						.findViewById(R.id.positiveButton))
						.setText(this.positiveButtonText);
				if (this.positiveButtonClickListener != null) {
					((Button) ((View) localObject)
							.findViewById(R.id.positiveButton))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View paramView) {
									positiveButtonClickListener.onClick(
											localCustomDialog,
											R.id.positiveButton);
								}
							});
				}
			}

			if (this.negativeButtonText == null) {
				((View) localObject).findViewById(R.id.negativeButton)
						.setVisibility(8);
			} else {
				((Button) ((View) localObject)
						.findViewById(R.id.negativeButton))
						.setText(this.negativeButtonText);
				if (this.negativeButtonClickListener != null)
					((Button) ((View) localObject)
							.findViewById(R.id.negativeButton))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View paramView) {
									negativeButtonClickListener.onClick(
											localCustomDialog,
											R.id.negativeButton);
								}
							});
			}

			if (StringUtil.isEmpty(this.expandButtonText)) {
				((View) localObject).findViewById(R.id.expandButton)
						.setVisibility(View.GONE);
			} else {
				((Button) ((View) localObject).findViewById(R.id.expandButton))
						.setText(this.expandButtonText);
				if (this.negativeButtonClickListener != null) {
					((Button) ((View) localObject)
							.findViewById(R.id.expandButton))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View paramView) {
									expandButtonClickListener.onClick(
											localCustomDialog,
											R.id.expandButton);
								}
							});
				}
			}

			if (StringUtil.isEmpty(this.scrollMessage)) {
				((View) localObject).findViewById(R.id.scrollview)
						.setVisibility(View.GONE);
			} else {
				TextView message = (TextView) ((View) localObject)
						.findViewById(R.id.scroll_message);
				final String xkxy=context.getResources().getString(R.string.xkxx);
				final String yssm=context.getResources().getString(R.string.yssm);
				
				if (scrollMessage.contains("<<"+xkxy+">>")
						|| scrollMessage.contains("<<"+yssm+">>")) {
					SpannableString msp = new SpannableString(scrollMessage);
					int start1 = scrollMessage.indexOf("<<"+xkxy+">>");
					int end1 = ("<<"+xkxy+">>").length() + start1;
					int start2 = scrollMessage.indexOf("<<"+yssm+">>");
					int end2 = ("<<"+yssm+">>").length() + start2;
					//Log.e("qq", ""+start1+","+end1+","+start2+","+end2);
					msp.setSpan(new URLSpan(""){
						@Override
						public void onClick(View widget) {
							Intent it=new Intent();
							it.setClass(context,DocShowActivity.class);
							it.putExtra("str", context.getResources().getString(R.string.dialog_disclaimer_xksyxx));
							it.putExtra("title", xkxy);
							((Activity)context).startActivity(it);
						}
					}, start1, end1,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					msp.setSpan(new URLSpan(""){
						@Override
						public void onClick(View widget) {
							Intent it=new Intent();
							it.setClass(context, DocShowActivity.class);
							it.putExtra("str", context.getResources().getString(R.string.dialog_disclaimer_ysbhsm));
							it.putExtra("title", yssm);
							((Activity)context).startActivity(it);
						}
					}, start2, end2,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					message.setText(msp);						
					message.setMovementMethod(LinkMovementMethod.getInstance());
	
				} else {
					message.setText(this.scrollMessage);
				}
			}

			if (StringUtil.isEmpty(this.message)) {
				((View) localObject).findViewById(R.id.message).setVisibility(
						View.GONE);
			} else {
				TextView message = (TextView) ((View) localObject)
						.findViewById(R.id.message);
				// message.setMovementMethod(ScrollingMovementMethod.getInstance());
				message.setText(this.message);
			}
			((View) localObject).findViewById(R.id.title_layout)
					.setBackgroundDrawable(null);
			((View) localObject).findViewById(R.id.custom_dialog_main_layout)
					.setBackgroundColor(
							this.context.getResources().getColor(
									android.R.color.transparent));
			((View) localObject).findViewById(R.id.custom_dialog_main_layout)
					.setBackgroundResource(R.drawable.dialog_full_holo_light);

			localCustomDialog.setContentView((View) localObject);
			localCustomDialog.setCancelable(this.cancelable);
			// 由于custom_dialog里面设置宽度不起效,所以这里单独控制
			WindowManager windowManager = ((Activity) this.context)
					.getWindowManager();
			Display display = windowManager.getDefaultDisplay(); // 获取屏幕宽、高用
			WindowManager.LayoutParams layoutParams = localCustomDialog.getWindow().getAttributes();
			int width = display.getWidth();
			int height = display.getHeight();
			layoutParams.width = (int)((Math.min(width, height)) * 6 /7.0);
			localCustomDialog.getWindow().setAttributes(layoutParams);
			return (CustomDialog) localCustomDialog;
		}

		public Builder setCancelable(boolean paramBoolean) {
			this.cancelable = paramBoolean;
			return this;
		}

		public Builder setContentView(View paramView) {
			this.contentView = paramView;
			return this;
		}

		public Builder setExpandButton(int paramInt,
				DialogInterface.OnClickListener paramOnClickListener) {
			this.expandButtonText = ((String) this.context.getText(paramInt));
			this.expandButtonClickListener = paramOnClickListener;
			return this;
		}

		public Builder setExpandButton(String paramString,
				DialogInterface.OnClickListener paramOnClickListener) {
			this.expandButtonText = paramString;
			this.expandButtonClickListener = paramOnClickListener;
			return this;
		}

		public Builder setMessage(int paramInt) {
			this.message = ((String) this.context.getText(paramInt));
			return this;
		}

		public Builder setMessage(String paramString) {
			this.message = paramString;
			return this;
		}

		public Builder setScrollMessage(int paramInt) {
			this.scrollMessage = ((String) this.context.getText(paramInt));
			return this;
		}

		public Builder setScrollMessage(String paramString) {
			this.scrollMessage = paramString;
			return this;
		}

		public Builder setNegativeButton(int paramInt,
				DialogInterface.OnClickListener paramOnClickListener) {
			this.negativeButtonText = ((String) this.context.getText(paramInt));
			this.negativeButtonClickListener = paramOnClickListener;
			return this;
		}

		public Builder setNegativeButton(String paramString,
				DialogInterface.OnClickListener paramOnClickListener) {
			this.negativeButtonText = paramString;
			this.negativeButtonClickListener = paramOnClickListener;
			return this;
		}

		public Builder setPositiveButton(int paramInt,
				DialogInterface.OnClickListener paramOnClickListener) {
			this.positiveButtonText = ((String) this.context.getText(paramInt));
			this.positiveButtonClickListener = paramOnClickListener;
			return this;
		}

		public Builder setPositiveButton(String paramString,
				DialogInterface.OnClickListener paramOnClickListener) {
			this.positiveButtonText = paramString;
			this.positiveButtonClickListener = paramOnClickListener;
			return this;
		}

		public Builder setTitle(int paramInt) {
			this.title = ((String) this.context.getText(paramInt));
			return this;
		}

		public Builder setTitle(String paramString) {
			this.title = paramString;
			return this;
		}
	}
}