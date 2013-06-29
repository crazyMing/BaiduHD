package com.baidu.hd.player;

import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.baidu.hd.debug.P2PDownloadView;
import com.baidu.hd.debug.P2PSettingView;
import com.baidu.hd.debug.TaskListView;
import com.baidu.hd.test.RegionActivity;
import com.baidu.hd.R;
import com.baidu.player.download.DownloadServiceAdapter;

class DebugView {
	
	private PlayerAccessor mAccessor = null;
	
	public DebugView(PlayerAccessor accessor) {
		mAccessor = accessor;
	}
	
	public void createMenu(Menu menu) {
		int base = Menu.FIRST;
		menu.add(base, base, base, "Detect");
		menu.add(base, base + 1, base + 1, "Setting");
		menu.add(base, base + 6, base + 2, "Rate");
		menu.add(base, base + 2, base + 3, "TaskList");
		menu.add(base, base + 3, base + 4, "Region");
		menu.add(base, base + 4, base + 5, "Kill");
		menu.add(base, base + 5, base + 6, "KDown");
	}

	public void clickMenu(int itemId) {
		
		switch (itemId) {
		case Menu.FIRST: {
			P2PDownloadView view = (P2PDownloadView) mAccessor.getHost()
					.findViewById(R.id.debug_download_view);
			if (view.getVisibility() == View.VISIBLE) {
				view.setVisibility(View.GONE);
			} else {
				view.setContext(mAccessor.getHost());
				view.setServiceFactory(mAccessor.getServiceFactory());
				view.setTask(mAccessor.getTask());
				view.setVisibility(View.VISIBLE);
			}
		}
			break;
		case Menu.FIRST + 1: {
			P2PSettingView view = (P2PSettingView) mAccessor.getHost()
					.findViewById(R.id.debug_p2p_setting_view);
			if (view.getVisibility() == View.VISIBLE) {
				view.setVisibility(View.GONE);
			} else {
				view.setServiceFactory(mAccessor.getServiceFactory());
				view.setVisibility(View.VISIBLE);
			}
		}
			break;
		case Menu.FIRST + 2: {
			TaskListView view = (TaskListView) mAccessor.getHost()
					.findViewById(R.id.debug_task_list_view);
			if (view.getVisibility() == View.VISIBLE) {
				view.setVisibility(View.GONE);
			} else {
				view.setServiceFactory(mAccessor.getServiceFactory());
				view.setVisibility(View.VISIBLE);
			}
		}
			break;
		case Menu.FIRST + 3: {
			mAccessor.getHost().startActivity(new Intent(mAccessor.getHost(), RegionActivity.class));
		}
			break;
		case Menu.FIRST + 4: {
			android.os.Process.killProcess(android.os.Process.myPid());
		}
			break;
		case Menu.FIRST + 5: {
			DownloadServiceAdapter adapter = 
					(DownloadServiceAdapter)mAccessor.getServiceFactory().getServiceProvider(DownloadServiceAdapter.class);
			adapter.destroy();
		}
			break;
		case Menu.FIRST + 6: {
			Toast.makeText(mAccessor.getHost(), mAccessor.getVideoSize().getX() + "," + mAccessor.getVideoSize().getY(), Toast.LENGTH_LONG).show();
		}
			break;
		}
	}
	
	public void destroy() {
		P2PDownloadView downloadView = (P2PDownloadView) mAccessor.getHost()
				.findViewById(R.id.debug_download_view);
		downloadView.onDestroy();
	}
}
