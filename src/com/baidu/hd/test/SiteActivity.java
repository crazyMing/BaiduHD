package com.baidu.hd.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.hd.BaseActivity;
import com.baidu.hd.log.Logger;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.VideoFactory;
import com.baidu.hd.player.PlayerLauncher;
import com.baidu.hd.sniffer.Sniffer;
import com.baidu.hd.sniffer.SnifferEntity;
import com.baidu.hd.sniffer.BigSiteSnifferResult.BigSiteAlbumResult;
import com.baidu.hd.R;

@TargetApi(8)
public class SiteActivity extends BaseActivity {
	
	private Logger logger = new Logger("TestWebActivity");
	
	private String mUrl = "";
	private String mTitle = "";
	private String mIEUA = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)";
	private String mDefaultUA = null;
	private int mUAMode = 0; // IE
	private String mPath = Environment.getExternalStorageDirectory() + "/baidu/test_url/";
	
	private PopupWindow mWindow = null;
	private WebView mSnifferWebView = null;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			if(msg.what == BaiduPlayerJavaScript.TestWebSniffer) {
//				List<String> result = ((BaiduPlayerJavaScript.ResultArgs)msg.obj).getResult();
//				snifferComplete(result);
			}
			
			super.handleMessage(msg);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.test_site);
		mSnifferWebView = (WebView)this.findViewById(R.id.sniffer_web_view);
		mSnifferWebView.getSettings().setUserAgentString("Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
		mSnifferWebView.getSettings().setJavaScriptEnabled(true);
		mSnifferWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		mSnifferWebView.getSettings().setPluginsEnabled(true);
		mSnifferWebView.getSettings().setPluginState(WebSettings.PluginState.OFF);
		
//		super.setBackExitFlag(false);
		
		final WebView webView = (WebView)this.findViewById(R.id.web_view);
		final EditText txtUrl = (EditText)this.findViewById(R.id.txt_url);
		mDefaultUA = webView.getSettings().getUserAgentString();
		// 修改UA
//		webView.getSettings().setUserAgentString("Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
//		webView.getSettings().setUserAgentString("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.requestFocusFromTouch();
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setLoadsImagesAutomatically(true);
		webView.getSettings().setDatabaseEnabled(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webView.getSettings().setPluginsEnabled(true);
		webView.getSettings().setPluginState(WebSettings.PluginState.ON);
//		webView.addJavascriptInterface(new BaiduPlayerJavaScript(this.mHandler),
//				"BAIDUPLAYERURL");
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				logger.d("shouldOverrideUrlLoading url = " + url);
				mUrl = url;
				webView.loadUrl(mUrl);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				logger.d("onPageStarted url = " + url);
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				logger.d("onPageFinished url = " + url);
				txtUrl.setText(url);
				super.onPageFinished(view, url);
			}
			
		});
		webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onReceivedTitle(WebView view, String title) {
				
				mTitle = title;
				super.onReceivedTitle(view, title);
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
			}
		});
		
		final Button btnGo = (Button)this.findViewById(R.id.btn_go);
		btnGo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mUrl = txtUrl.getText().toString();
				if (mUrl.startsWith("http://")) {
					webView.loadUrl(mUrl);
				}
				else {
					webView.loadUrl("http://" + mUrl);
				}
			}
		});
		
		final Button btnSave = (Button)this.findViewById(R.id.btn_save);
		btnSave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				TestUrlManager manager = (TestUrlManager)getServiceProvider(TestUrlManager.class);
//				Toast.makeText(SiteActivity.this, webView.getUrl(), Toast.LENGTH_LONG).show();
//				manager.Add(webView.getUrl(), mUAMode);
			}
		});
		
		webView.loadUrl("file:///android_asset/test/test_site.html");
	}

	@Override
	protected void onPause() {

		if(this.mWindow != null) {
			this.mWindow.dismiss();
			this.mWindow = null;
		}
		this.callHiddenWebViewMethod("onPause");
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		
		WebView webView = (WebView)findViewById(R.id.web_view);
		if(webView.canGoBack()) {
			webView.goBack();
			return;
		}
		
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		super.onCreateOptionsMenu(menu);
		int base = Menu.FIRST;
		menu.add(base, base, base, "Big");
		menu.add(base, base + 1, base + 1, "Small");
		menu.add(base, base + 2, base + 2, "PC");
		menu.add(base, base + 3, base + 3, "Android");
//		menu.add(base, base + 4, base + 4, "Export");
//		menu.add(base, base + 5, base + 5, "Excute");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final WebView webView = (WebView)this.findViewById(R.id.web_view);
		switch(item.getItemId()) {
		case 1:
			{
				Sniffer sniffer = (Sniffer)getServiceProvider(Sniffer.class);
				SnifferEntity entity = sniffer.createBig(new Sniffer.BigSiteCallback() {
					@Override
					public void onCancel(String refer) {
					}

					@Override
					public void onComplete(String refer, String url, 	BigSiteAlbumResult result) {
						NetVideo video = VideoFactory.create(false).toNet();
						video.setName(SiteActivity.this.mTitle);
						video.setRefer(refer);
						video.setUrl(url);
						PlayerLauncher.startup(SiteActivity.this, video);
						
					}
				}, mSnifferWebView);
				
				entity.request(webView.getUrl());
			}
			break;
		case 2:
			{
				this.snifferBdhd();
			}
			break;
		case 3:
			webView.getSettings().setUserAgentString(mIEUA);
			mUAMode = 0;
			break;
		case 4:
			webView.getSettings().setUserAgentString(mDefaultUA);
			mUAMode = 1;
			break;
//		case 5:
//			exportToFile();
//			break;
//		case 6:
//			execute();
//			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
//	private List<TestUrl> mTestUrlsFromDB = null;
//	private List<TestUrl> mTestUrlsFromFile = null;
//	private int mCount = 0;
//	
//	private void exportToFile() {
//		String fileName =  mPath + "lastData.txt";
//		// 保存到一个临时文件中
//		TestUrlManager manager = (TestUrlManager)getServiceProvider(TestUrlManager.class);
//		mTestUrlsFromDB = manager.getAllTestUrls();
//		FileWriter fileWriter;
//		try {
//			if (!new File(mPath).exists()) {
//				new File(mPath).mkdirs();
//			}
//			File file = new File(fileName);
//			fileWriter = new FileWriter(file);
//			
//			for (TestUrl url : mTestUrlsFromDB) {
//				fileWriter.write(String.valueOf(url.getId())+ " ");
//				fileWriter.write(String.valueOf(url.getHost())+ " ");
//				fileWriter.write(String.valueOf(url.getUrl())+ " ");
//				fileWriter.write(String.valueOf(url.getMode())+ " ");
//				fileWriter.write(String.valueOf(url.isSucceed())+ " \n");
//			}
//			fileWriter.flush();
//			fileWriter.close();
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
//	private void execute() {
//		// 1.先清空数据库
//		TestUrlManager manager = (TestUrlManager)getServiceProvider(TestUrlManager.class);
//		//
//		
//		// 2.将数据从文件中读入
//		mTestUrlsFromFile = new ArrayList<TestUrl>();
//		
//		String full_path =  mPath + "lastData.txt";
//		FileReader fileReader;
//		try {
//			fileReader = new FileReader(full_path);
//			BufferedReader bufferReader = new BufferedReader(fileReader);
//			String s;
//			while ((s = bufferReader.readLine()) != null) {
//				// System.out.println(s);
//				String[] data = s.split(" ");
//				TestUrl url = new TestUrl();
//				url.setId(Long.valueOf(data[0].trim()));
//				url.setHost((data[1].trim()));
//				url.setUrl((data[2].trim()));
//				url.setMode(Integer.valueOf((data[3].trim())));
//				url.setSucceed(Boolean.valueOf((data[4].trim())));
//				mTestUrlsFromFile.add(url);
//			}
//			fileReader.close();
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		
//		// 3.遍历数据，并更新字段
//		check(mTestUrlsFromFile);
//	}
	
//	private void onSnifferComplete(long id, boolean succeed) {
//		for (int i = 0; i < mTestUrlsFromFile.size(); i++) {
//			if (mTestUrlsFromFile.get(i).getId() == id) {
//				mTestUrlsFromFile.get(i).setSucceed(succeed);
//				break;
//			}
//		}
//	   mCount++;
//	   if (mCount == mTestUrlsFromFile.size()) {
//		   output();
//		   mCount = 0;
//	   }
//	}
	
//	private void check(List<TestUrl> urls) {
//		
//		for (int i=0; i<urls.size(); i++) {
//			final TestUrl testUrl = urls.get(i);
////			Sniffer sniffer = (Sniffer)getServiceProvider(Sniffer.class);
////			SnifferEntity snifferEntity = sniffer.createBig(new Sniffer.BigSiteCallback() {
////				
////				@Override
////				public void onComplete(String refer, String url) {
////					if(!refer.equals(testUrl.getUrl())) {
////						return;
////					}
////					onSnifferComplete(testUrl.getId(), !"".equals(url));
////				}
////				
////				@Override
////				public void onCancel(String refer) {
////				}
////			});
////			snifferEntity.request(testUrl.getUrl());
//		}
//	}
	
//	private void output() {
//		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
//		String date =sdf.format(System.currentTimeMillis());
//		String result_name = "result_" + date + ".txt";
//		FileWriter fileWriter;
//		try {
//			if (!new File(mPath).exists()) {
//				new File(mPath).mkdirs();
//			}
//			File file = new File(mPath + result_name);
//			fileWriter = new FileWriter(file);
//			for (TestUrl url : mTestUrlsFromFile) {
//				fileWriter.write(String.valueOf(url.getId())+ " ");
//				fileWriter.write(String.valueOf(url.getHost())+ " ");
//				fileWriter.write(String.valueOf(url.getUrl())+ " ");
//				fileWriter.write(String.valueOf(url.getMode())+ " ");
//				fileWriter.write(String.valueOf(url.isSucceed())+ " \n");
//			}
//			fileWriter.flush();
//			fileWriter.close();
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

    /*
     * 阻止回到桌面后，flash仍然播放的情况
     */
	private void callHiddenWebViewMethod(String name){ 
		
		WebView webView = (WebView)findViewById(R.id.web_view);
        try {  
            Method method = WebView.class.getMethod(name);  
            method.invoke(webView);  
        } catch (NoSuchMethodException e) {  
            this.logger.e("No such method: " + name);
        } catch (IllegalAccessException e) {  
        	this.logger.e("Illegal Access: " + name);
        } catch (InvocationTargetException e) {  
        	this.logger.e("Invocation Target Exception: " + name);
        }  
	}
	
	private void snifferBdhd() {
		Thread t = new Thread(
				new BaiduPlayerJavaScript.JavaScriptThreadRunnable(
						(WebView)this.findViewById(R.id.web_view)));
		t.start();
	}
	
	private void snifferComplete(List<String> result) {

		if(result.isEmpty()) {
			Toast.makeText(SiteActivity.this, "not found bdhd link", Toast.LENGTH_LONG).show();
			return;
		}
		
		LayoutInflater inflater = LayoutInflater.from(this);
		View contentView = inflater.inflate(R.layout.test_sniffer_result, null);
		this.mWindow = new PopupWindow(contentView, 500, 400);
		this.mWindow.setTouchable(true);
		this.mWindow.setFocusable(true);
		this.mWindow.showAtLocation(this.findViewById(R.id.web_view), Gravity.CENTER, 0, 0);
		
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.drawable.sniffer_control_episode_adapter, result);
		ListView listView = (ListView)contentView.findViewById(R.id.listview);
		listView.setAdapter(arrayAdapter);
		arrayAdapter.notifyDataSetChanged();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				arg1.setSelected(true);
				
				TextView tv = (TextView)arg1.findViewById(R.id.tv);
				
				NetVideo video = VideoFactory.create(false).toNet();
				video.setName(mTitle);
				video.setRefer(mUrl);
				video.setUrl(tv.getText().toString());
				video.setType(NetVideo.NetVideoType.P2P_STREAM);
				PlayerLauncher.startup(SiteActivity.this, video);
				
				WebView webView = (WebView)findViewById(R.id.web_view);
				webView.pauseTimers();
				webView.stopLoading();
			}
		});
	}
}
