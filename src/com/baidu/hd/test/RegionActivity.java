package com.baidu.hd.test;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.db.DBWriter;
import com.baidu.hd.log.DebugLogger;
import com.baidu.hd.net.HttpWeb;
import com.baidu.hd.net.HttpWebCallback;
import com.baidu.hd.service.ServiceFactory;
import com.baidu.hd.sniffer.SmallSiteUrl;
import com.baidu.hd.sniffer.Sniffer;
import com.baidu.hd.stat.Stat;
import com.baidu.hd.util.PlayerTools;
import com.baidu.hd.util.TimeUtil;
import com.baidu.hd.util.Turple;
import com.baidu.mobstat.StatService;
import com.baidu.player.download.DownloadServiceAdapter;
import com.baidu.hd.R;

public class RegionActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);

		super.setBackExitFlag(false);

		// 测试网络
		Button btnNetworkTest = (Button) findViewById(R.id.btnNetwork);
		btnNetworkTest.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (PlayerTools.isNetworkConnected(RegionActivity.this,
						PlayerTools.NetworkType.NETWORK_TYPE_WIFI)) {
					Toast.makeText(RegionActivity.this,
							"WIFI network connected", Toast.LENGTH_SHORT)
							.show();
				} else if (PlayerTools.isNetworkConnected(RegionActivity.this,
						PlayerTools.NetworkType.NETWORK_TYPE_3G)) {
					Toast.makeText(RegionActivity.this, "3G network connected",
							Toast.LENGTH_SHORT).show();
				} else if (PlayerTools.isNetworkConnected(RegionActivity.this,
						PlayerTools.NetworkType.NETWORK_TYPE_GPRS)) {
					Toast.makeText(RegionActivity.this,
							"GPRS network connected", Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(RegionActivity.this,
							"None network connected", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

		Button btnUploadLog = (Button) findViewById(R.id.btn_upload_log);
		btnUploadLog.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Stat stat = (Stat) getServiceProvider(Stat.class);
				stat.postLog();
			}
		});

		Button btnCrashMain = (Button) findViewById(R.id.btn_crash_main);
		btnCrashMain.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// int a = 0;
				// int b = 5 / a;
				// System.out.print(b);
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});

		Button btnCrashDownload = (Button) findViewById(R.id.btn_crash_download);
		btnCrashDownload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Log.d("Region", "stopService");
				DownloadServiceAdapter adapter = (DownloadServiceAdapter) getServiceProvider(DownloadServiceAdapter.class);
				adapter.destroy();
			}
		});

		Button btnSetLogLevel = (Button) findViewById(R.id.btn_task_set_log_level);
		btnSetLogLevel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					StatService.onEvent(RegionActivity.this, "a", "1", 2);
					StatService.onEvent(RegionActivity.this, "a", "2", 3);
					StatService.onEvent(RegionActivity.this, "b", "1", 4);
					StatService.onEvent(RegionActivity.this, "b", "2", 5);

					StatService.onEvent(RegionActivity.this, "c", "1");
					StatService.onEvent(RegionActivity.this, "c", "2");
					StatService.onEvent(RegionActivity.this, "d", "1");
					StatService.onEvent(RegionActivity.this, "d", "2");

					String info = ((EditText) findViewById(R.id.txt_task_info))
							.getText().toString();
					DownloadServiceAdapter adapter = (DownloadServiceAdapter) getServiceProvider(DownloadServiceAdapter.class);
					adapter.setLogLevel(Integer.parseInt(info));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		});

		Button btnLimitSpeed = (Button) findViewById(R.id.btn_task_limit_speed);
		btnLimitSpeed.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					String info = ((EditText) findViewById(R.id.txt_task_info))
							.getText().toString();
					DownloadServiceAdapter adapter = (DownloadServiceAdapter) getServiceProvider(DownloadServiceAdapter.class);
					adapter.setSpeedLimit(Integer.parseInt(info));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		});

		EditText txtSnifferUrl = (EditText) findViewById(R.id.txt_sniffer_url);
		txtSnifferUrl.setText("http://www.yiyi.cc/film9/muguiyingguashuai/");

		Button btnSniffer = (Button) findViewById(R.id.btn_sniffer);
		btnSniffer.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String url = ((EditText) findViewById(R.id.txt_sniffer_url))
						.getText().toString();

				mSnifferState = 3;
//				Sniffer sniffer = (Sniffer) getServiceProvider(Sniffer.class);
//				sniffer.createBig(new Sniffer.BigSiteCallback() {
//					
//					@Override
//					public void onComplete(String refer, String url) {
//						mSnifferState = ("".equals(url) ? 2 : 1);
//					}
//					
//					@Override
//					public void onCancel(String refer) {
//					}
//				}).request(url);
			}
		});

//		Button btnSmallSniffer = (Button) findViewById(R.id.btn_small_sniffer);
//		btnSmallSniffer.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				String url = ((EditText) findViewById(R.id.txt_sniffer_url))
//						.getText().toString();
//				Sniffer sniffer = (Sniffer) getServiceProvider(Sniffer.class);
//				sniffer.createSmall(new Sniffer.SmallSiteCallback() {
//					
//					@Override
//					public void onComplete(String refer, SmallSiteUrl url) {
//						String text = "refer:" + url.getRefer() + " link:"
//								+ url.getLink() + "count:"
//								+ url.getPlayUrls().size() + "\r\n";
//						for (Turple<String, String> entry : url.getPlayUrls()) {
//							text += "url:" + entry.getX() + " bdhd:"
//									+ entry.getY() + "\r\n";
//						}
//						Toast.makeText(RegionActivity.this, text,
//								Toast.LENGTH_LONG).show();
//						DebugLogger.write(text);
//					}
//					
//					@Override
//					public void onCancel(String refer) {
//					}
//				}).request(url);
//			}
//		});

		EditText txtWebUrl = (EditText) findViewById(R.id.txt_web_url);
		txtWebUrl.setText("http://www.youku.com");

		Button btnWeb = (Button) findViewById(R.id.btn_web);
		btnWeb.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String url = ((EditText) findViewById(R.id.txt_web_url))
						.getText().toString();
				new HttpWeb().fetch(url, getApplicationContext(),
						new HttpWebCallback() {

							@Override
							public void onTitle(String url, String title) {
								Toast.makeText(RegionActivity.this,
										url + " " + title, Toast.LENGTH_LONG)
										.show();
							}

							@Override
							public void onStop(String url) {
								Toast.makeText(RegionActivity.this,
										url + " stop", Toast.LENGTH_LONG)
										.show();
							}

							@Override
							public void onImage(String url, Bitmap image) {
								Toast.makeText(
										RegionActivity.this,
										url
												+ " "
												+ (image == null ? "false"
														: "true"),
										Toast.LENGTH_LONG).show();
							}
						});
			}
		});

		// 浏览器模式测试
		Button btnBrowser = (Button) findViewById(R.id.btn_browser);
		btnBrowser.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				BrowserHomeTest.setOnResumeAnimation(true);
				Intent intent = new Intent(RegionActivity.this, BrowserHomeTest.class);
				startActivity(intent);
			}
		});

		// 测试书签、历史
//		testBookmarkAndHistory();
		
		// add by sunyimin 优化调起app测试
		
	}

	// for sniffer unit test
	private int mSnifferState = 0;

	/*
	 * 1 success 2 fail 3 ing 4 none
	 */
	public int getSnifferState() {
		return this.mSnifferState;
	}

//	BrowserHistoryRecord current = null;
//	String recorddate = TimeUtil.getInstance().getCurrentTime();
//
//	// mark history demo by sunjianshun
//	private void testBookmarkAndHistory() {
//		ServiceFactory factory = getPlayerApp().getServiceFactory();
//		final BrowserHistoryRecordManager bhrManager = (BrowserHistoryRecordManager) factory
//				.getServiceProvider(BrowserHistoryRecordManager.class);
//		final DBWriter dbWrite = (DBWriter) factory
//				.getServiceProvider(DBWriter.class);
//		Button buttonLauncher = (Button) findViewById(R.id.test_region_launch_browserHistoryActivity);
//		final EditText searchUrl = (EditText) findViewById(R.id.test_region_history_url);
//		final Button buttonBookmark = (Button) findViewById(R.id.test_region_bookmark);
//		Button buttonSearch = (Button) findViewById(R.id.test_region_search);
//		// Button buttonDate = (Button)findViewById(R.id.test_region_date);
//		searchUrl.addTextChangedListener(new TextWatcher() {
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before,
//					int count) {
//
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count,
//					int after) {
//				buttonBookmark.setText("forbidden");
//				buttonBookmark.setEnabled(false);
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//				// TODO Auto-generated method stub
//
//			}
//		});
//		buttonBookmark.setText("forbidden");
//		buttonBookmark.setEnabled(false);
//		buttonBookmark.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// 只有执行完插入动作之后，才知道该记录是否已经收藏了
//				if (current == null)
//					return;
//
//				current.setBookmarked(!current.isBookmarked());
//				buttonBookmark.setText(current.isBookmarked() ? "已存在" : "请添加");
//			}
//		});
//		buttonSearch.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//
//				String address = searchUrl.getText().toString();
//				String today = TimeUtil.getInstance().getCurrentTime();
//				/*
//				 * JSONArray parma = new JSONArray(); parma.put(address);
//				 * parma.put(address); parma.put(today); parma.put(today);
//				 * parma.put(true); parma.put(true); current =
//				 * bhrManager.create(parma);
//				 */
//
//				if (address.equals("")) {
//					Toast.makeText(RegionActivity.this, "address is null",
//							Toast.LENGTH_LONG).show();
//					return;
//				}
//
//				ContentValues values = new ContentValues();
//				values.put(BrowserHistoryRecord.B_SITENAME, address);
//				values.put(BrowserHistoryRecord.B_SITEADDR, address);
//				values.put(BrowserHistoryRecord.B_FIRSTTIME, today);
//				values.put(BrowserHistoryRecord.B_LASTTIME, today);
//				values.put(BrowserHistoryRecord.B_BOOKMARKED, false);
//				values.put(BrowserHistoryRecord.B_INHISTORYLIST, true);
//				current = bhrManager.create(values);
//				if (current == null)
//					return;
//
//				if (current.getId() == -1) {
//					bhrManager.add(current);
//					current = bhrManager.findBySiteAddress(current
//							.getSiteAddress());
//					Toast.makeText(RegionActivity.this, "新纪录插入成功",
//							Toast.LENGTH_LONG).show();
//				} else {
//					Toast.makeText(RegionActivity.this, "该记录已经存在",
//							Toast.LENGTH_LONG).show();
//				}
//
//				buttonBookmark.setText(current.isBookmarked() ? "已存在" : "请添加");
//				buttonBookmark.setEnabled(true);
//			}
//		});
//		buttonLauncher.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//			}
//		});
		/*
		 * final Calendar calendar = Calendar.getInstance(Locale.CHINA);
		 * buttonDate.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * DatePickerDialog.OnDateSetListener listenre = new
		 * DatePickerDialog.OnDateSetListener() {
		 * 
		 * @Override public void onDateSet(DatePicker view, int year, int
		 * monthOfYear, int dayOfMonth) { String formate = "%d-%d-%d 00:00:01";
		 * recorddate = String.format(formate, year, monthOfYear, dayOfMonth);
		 * Toast.makeText(RegionActivity.this, recorddate, Toast.LENGTH_LONG); }
		 * };
		 * 
		 * // DatePickerDialog dlg = new DatePickerDialog(RegionActivity.this,
		 * listenre, 0, 0, 0); dlg.show(); }
		 * 
		 * 
		 * });
		 */
//	}
	
}
