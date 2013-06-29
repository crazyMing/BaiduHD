package com.baidu.browser.explore.share;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.view.KeyEvent;

import com.baidu.browser.core.util.BdLog;
import com.baidu.browser.explore.share.BdTinyUrlGenerator.BdTinyUrlRecievedListener;
import com.baidu.hd.R;

/**
 * 内容分享器
 * 支持文本和图片内容
 */
public class BdSharer {  

	/**
	 * 默认分享请求码
	 */
	public static final int SHARE_RERQUEST_CODE_DEFAULT = 1996;
	/**
	 * 截图分享请求码
	 */
	public static final int SHARE_RERQUEST_CODE_SCREENSHOT = 1997;
	/**
	 * 下载分享请求码
	 */
	public static final int SHARE_RERQUEST_CODE_DOWNLOAD = 1998;
	/**
	 * 页面内分享请求码
	 */
	public static final int SHARE_RERQUEST_CODE_WEB = 1999;

	/**
	 * 文本分享最大字数限制
	 */
	private static final int SHARE_TEXT_SIZE_LIMIT = 140;
	/**
	 * 分享等待提示框
	 */
	private static BdWaitingDialog waitProgressDialog;

	/**
	 * 用于短网址的等待和取消
	 */
	private static BdWaitingDialog waitTinyUrlDialog;
	/**
	 * 是否取消分享
	 */
	private static boolean willCancelShare = false;

	/**
	 * 图片分享大小阀值，Webkit默认优先选择图片分享 如果同时有图片和文字（带背景图片的文字或带默认文字的图片），图片大小小于此阀值的，改为文字分享
	 */
	public static final long IMAGE_MIN_SIZE_TO_SHARE = 1024 * 10;

	/**
	 * 取消锁，防止主线程取消分享，而分享线程并发获取取消状态有误，导致仍然打开第三方分享的问题
	 */
	private static Object cancelLock = new Object();

	/**
	 * 是否发送成功
	 */
	private static boolean sendOk;

	/**
	 * 分享文本
	 * 
	 * @param mContext
	 *            app上下文
	 * @param text1
	 *            文本内容字段一
	 * @param text2
	 *            文本内容字段二
	 * @return 是否发送成功
	 */
	public static boolean sendTextShare(Context mContext, String text1, String text2) {
		return sendTextShare(mContext, R.string.share_content, text1, text2);
	}

	/**
	 * 分享文本
	 * 
	 * @param mContext
	 *            app上下文
	 * @param templateId
	 *            文本内容模版
	 * @param text1
	 *            文本内容字段一
	 * @param text2
	 *            文本内容字段二
	 * @return 是否发送成功
	 */
	public static boolean sendTextShare(final Context mContext, final int templateId, final String text1,
			final String text2) {

		BdLog.d(text1 + ", " + text2);

		final BdSharerOM shareContentMeta = new BdSharerOM();
		shareContentMeta.setContentType(BdSharerOM.TYPE_TEXT);

		waitTinyUrlDialog = new BdWaitingDialog(mContext);
		waitTinyUrlDialog.setMessage(mContext.getString(R.string.share_waiting));
		waitTinyUrlDialog.setCancelable(true);
		waitTinyUrlDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				willCancelShare = true;
			}

		});

		waitTinyUrlDialog.show();

		willCancelShare = false;
		BdTinyUrlGenerator generator = new BdTinyUrlGenerator();
		generator.setEventListener(new BdTinyUrlRecievedListener() {

			@Override
			public void onTinyUrlRecieved(String tinyUrl) {
				String extra1 = text1;
				String extra2 = tinyUrl;
				if (text1 == null) {
					extra1 = "";
				}
				//				if (extra1 != "") {
				//					extra1 = text1 + TextUtil.COMMA;
				//				}
				if (tinyUrl == null) {
					extra2 = "";
				}
				//				if (extra2 != "") {
				//					extra2 = Sharer.SHARE_DETAIL_TIP + tinyUrl;
				//				}
				if (!"".equals(extra1) && "".equals(extra2)) {
					extra1 = text1;
				}
				BdLog.d(extra1 + ", " + extra2);
				String text = mContext.getString(templateId, extra1, extra2);
				shareContentMeta.setContent(text);
				shareContentMeta.setExtra1(text1);
				shareContentMeta.setExtra2(tinyUrl);
				shareContentMeta.setContent(preProcContent(mContext, shareContentMeta));
				if (!willCancelShare) {
					sendOk = BdSharer.sendShare(mContext, shareContentMeta, false,
							SHARE_RERQUEST_CODE_DEFAULT, false);
				}
				if (waitTinyUrlDialog != null) {
					waitTinyUrlDialog.cancel();
					waitTinyUrlDialog = null;
				}
			}
		});

		generator.generate(text2);

		return sendOk;
	}

	/**
	 * 发送分享
	 * 
	 * @param mContext
	 *            app上下文
	 * @param shareContentMeta
	 *            分享内容元数据
	 * @param isMultiple
	 *            是否同时发送多种数据
	 * @param shareEntry
	 *            分享入口，目前有网页文本、网页图片、屏幕截图、书签、历史、下载管理几个入口
	 * @param isCheckPriority
	 *            是否需要检查分享优先级
	 * @return 是否分享成功
	 */
	public static boolean sendShare(Context mContext, BdSharerOM shareContentMeta, boolean isMultiple,
			int shareEntry, boolean isCheckPriority) {

		if (shareContentMeta == null || shareContentMeta.getContent() == null) {
			BdLog.e("shareContentMeta is null.");
			return false;
		}

		String action = "android.intent.action.SEND";
		Intent intent = new Intent(action);
		// 不管有没有附件，都设置文本信息，第三方软件图片分享可能会不支持android.intent.extra.TEXT
		// 如果有文字，则分享文字
		intent.putExtra("android.intent.extra.TEXT", shareContentMeta.getContent());
		BdLog.d(shareContentMeta.getContent());
		int contentType = shareContentMeta.getContentType();
		if (contentType == BdSharerOM.TYPE_TEXT) {
			intent.setType("text/plain");
		}

		Intent intent2 = Intent.createChooser(intent, mContext.getString(R.string.share_channel));
		try {
			if (mContext instanceof Activity) {
				Activity act = (Activity) mContext;
				act.startActivityForResult(intent2, shareEntry);
			}
		} catch (ActivityNotFoundException e) {
			BdLog.e("share app not found.", e);
			return false;
		} catch (Exception e) {
			BdLog.e("share exception.", e);
			return false;
		}
		return true;
	}

	/**
	 * 预处理要分享的文本内容，规则：
	 * <p>
	 * 当短信内容和微博超过标准字数（140字，包括中英文，两条短信）时，[字段一]文字内容以“…”显示剩余部分；
	 * 如整条[字段一]全都以“…”显示却依然超过字数时，删除“点击详情[字段二]”，并尽量显示[字段一]中的内容；
	 * </p>
	 * 
	 * @param mContext
	 *            Context
	 * @param shareContentMeta
	 *            分享元数据
	 * @return 处理过的文本内容
	 */
	private static String preProcContent(Context mContext, BdSharerOM shareContentMeta) {

		String newContent = shareContentMeta.getContent();
		if (newContent.length() > SHARE_TEXT_SIZE_LIMIT) {
			// [字段一]文字内容以“…”显示剩余部分
			newContent = mContext.getString(R.string.share_content, "...", shareContentMeta.getExtra2());
			if (newContent.length() > SHARE_TEXT_SIZE_LIMIT) {
				// 删除“点击详情[字段二]”
				newContent = mContext.getString(R.string.share_content, shareContentMeta.getExtra1(), "");
				if (newContent.length() > SHARE_TEXT_SIZE_LIMIT) {
					// [字段一]文字内容以“前137个字+…”显示剩余部分，并且删除“点击详情[字段二]”
					String ellipsis = "...";
					newContent = newContent.substring(0, SHARE_TEXT_SIZE_LIMIT - ellipsis.length())
							+ ellipsis;
				}
			}
		}

		return newContent;
	}

	/**
	 * 弹出分享等待对话框
	 * 
	 * @param mContext Context
	 */
	public static void showShareWaitDialog(Context mContext) {

		if (waitProgressDialog == null) {
			// waitProgressDialog = ProgressDialog.show(mContext, null, mContext
			// .getString(R.string.share_waiting), false, true);
			waitProgressDialog = new BdWaitingDialog(mContext);
			waitProgressDialog.setMessage(mContext.getString(R.string.share_waiting));
			waitProgressDialog.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						return cancelShare();
					}
					return false;
				}

			});

		}
		waitProgressDialog.show();
	}

	/**
	 * 用户操作，取消分享
	 * 
	 * @return 是否成功取消等待提示框
	 */
	public static boolean cancelShare() {
		if (waitProgressDialog.isShowing()) { // 取消下载
			waitProgressDialog.dismiss();
			waitProgressDialog = null;
			return true;
		}
		return false;
	}

	/**
	 * @return the waitProgressDialog
	 */
	public static BdWaitingDialog getWaitProgressDialog() {
		return waitProgressDialog;
	}

	/**
	 * @return the waitTinyUrlDialog
	 */
	public static BdWaitingDialog getWaitTinyUrlDialog() {
		return waitTinyUrlDialog;
	}

	/**
	 * 取消分享
	 * 
	 * @return 是否成功取消等待提示框
	 */
	public static boolean dismissWatiTinyUrlDialog() {
		if (waitTinyUrlDialog != null) {
			waitTinyUrlDialog.cancel();
			waitTinyUrlDialog.dismiss();
			waitTinyUrlDialog = null;
			return true;
		}
		return false;
	}

	/**
	 * @param waitProgressDialog the waitProgressDialog to set
	 */
	public static void setWaitProgressDialog(BdWaitingDialog waitProgressDialog) { // SUPPRESS CHECKSTYLE
		BdSharer.waitProgressDialog = waitProgressDialog;
	}

	/**
	 * @return the cancelLock
	 */
	public static Object getCancelLock() {
		return cancelLock;
	}

}
