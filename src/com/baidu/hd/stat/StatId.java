package com.baidu.hd.stat;

public class StatId {

	/** 设备信息 */
	public static final int Device = 1;

	/** 分辨率 */
	public static final int Resolution = 2;

	/** CPU信息 */
	public static final int CPUInfo = 3;

	/** 启动时为WIFI环境数 */
	public static final int Wifi = 101;
	
	/** 启动时为非WIFI环境数 */
	public static final int NoWifi = 102;
	
	/** 启动时无SD卡用户数 */
	public static final int NoSDCard = 103;
	
	/** 总播放时长 */
	public static final int TotalPlayTime = 104;
	
	
	/** 播放量 */
	public static final int BasePlayCount = 200;
	
	/** 有效观看量 */
	public static final int BaseValidPlayCount = 210;
	
	/** 播放时长 */
	public static final int BasePlayTime = 220;
	
	/** 大站视频失败播放量 */
	public static final int BigSitePlayFailCount = 230;
	
	/** P2P视频失败播放量 */
	public static final int P2PPlayFailCount = 231;
	
	
	/** 下载量 */
	public static final int BaseDownloadCount = 300;
	
	/** 大站视频下载失败量 */
	public static final int BigSiteDownloadFailCount = 310;
	
	/** P2P视频下载失败量 */
	public static final int P2PDownloadFailCount = 311;
	
	/** 大站视频下载完成量 */
	public static final int BigSiteDownloadCompleteCount = 312;
	
	/** P2P视频下载完成量 */
	public static final int P2PDownloadCompleteCount = 313;
	
	/** SD卡数量 */
	public static final int LocalVideoCount = 314;
	
	public static final int HomeNoSugSearch = 400;
	public static final int HomeSugSearch = 401;
	public static final int RankNoSugSearch = 402;
	public static final int RankSugSearch = 403;
	
	public static final int HomeTab = 500;
	public static final int RankTab = 501;
	public static final int LocalTab = 502;
	public static final int HistoryTab = 503;
	public static final int MoreTab = 504;
	public static final int HistoryPlay = 505;
	public static final int HistorySpec = 506;
	public static final int AppCheckUpdate = 507;
	public static final int PlayCoreCheckUpdate = 508;
	public static final int ClearGarbage = 509;
	
	public class Startup {
		public static final String Name = "Start";
		public static final String HaveSDCard = "HaveSDCard";
		public static final String LocalCount = "LocalCount";
		public static final String IsWife = "IsWifi";
		public static final String IsNotWife = "IsNotWifi";
		public static final String HaveNoSDCard = "HaveNoSDCard";
	}
	
	/** main tab 切换 */
	public class TabClick {
		public static final String Name = "Tab";
		public static final String Home = "Home";
		public static final String Rank = "Rank";
		public static final String Personal = "Personal";
		public static final String More = "More";
		public static final String Browser = "Browser";
		public static final String InputUrl = "InputUrl";
	}
	
	/** 个人面板切换 */
	public class Personal {
		public static final String Name = "Personal";
		public static final String BufferPage = "BufPage";
		public static final String LocalPage = "LocPage";
		public static final String FavoritePage = "FavPage";
		public static final String HistoryPage = "HisPage";
		public static final String BufferPlay = "BufPlay";
		public static final String LocalPlay = "LocPlay";
		public static final String FavoritePlay = "FavPlay";
		public static final String HistoryPlay = "HisPlay";
	}
	
	/** 浏览器模式历史书签页 */
	public class BrowserHistoryAndBookmark {
		public static final String Name = "Bmk";
		public static final String AddressAdd = "AddrAdd";
		public static final String AddressRemove = "AddrRemove";
		public static final String MenuAdd = "MenuAdd";
		public static final String MenuRemove = "MenuRemove";
		public static final String TabHistory = "TabHistory";
		public static final String TabBookmark = "TabBookmark";
		public static final String BrowserHistory = "BrowHistory";
		public static final String BrowserBookmark = "BrowBookmark";
		public static final String ClearHistory = "ClearHistory";
	}
	
	/** 浏览器模式首页*/
	public class BrowserHomeList {
		public static final String Name = "Bhl";
		public static final String ToBrowser = "ToBrowser";
		public static final String Remove = "Remove";
	}
	
	/** 浏览器模式地址栏*/
	public class BrowserAdress {
		public static final String Name = "BA";
		public static final String Search = "Search";
		public static final String Go = "Go";
		public static final String Sug = "Sug";
	}
	
	/** 浏览器模式搜索栏*/
	public class BrowserSearch {
		public static final String Name = "BS";
		public static final String Search = "Search";
		public static final String Sug = "Sug";
	}
	
	/** 浏览器模式控制*/
	public class BrowCtrl {
		public static final String Name = "BrowCtrl";
		public static final String Back = "Back";
		public static final String Forward = "Forward";
		public static final String Stop = "Stop";
		public static final String Album = "Album";
		public static final String Menu = "Menu";
		public static final String Home = "Home";
		public static final String MarkHis = "MarkHis";
		public static final String InMark = "InMark";
		public static final String DelMark = "DelMark";
		public static final String Refresh = "Refresh";
		public static final String BackTo = "BackTo";
	}
	
	/** 浏览器模式播放*/
	public class BrowPlay {
		public static final String Name = "BrowPlay";
		public static final String Play = "Play";
		public static final String Down = "Down";
		public static final String Stow = "Stow";
		public static final String StowCa = "StowCa";
		public static final String ListIn = "ListIn";
		public static final String Plays = "Plays";
		public static final String Downs = "Downs";
	}
	
	
	public class Settings {
		public static final String Name = "Settings";
		public static final String ClearBuffer = "ClearBuffer";
		public static final String Wifi = "Wifi";
		public static final String Privacy = "Privacy";
		public static final String File = "File";
		public static final String AppUpgrade = "AppUpgrade";
		public static final String PlayerCore = "PlayerCore";
		public static final String Feedback = "Feedback";
		public static final String About = "About";
	}
	
	public class Search {
		public static final String Name = "Sch";
		public static final String HomeNoSug = "HomeNoSug";
		public static final String HomeSug = "HomeSug";
		public static final String RankNoSug = "RankNoSug";
		public static final String RankSug = "RankSug";
	}
	
	public class Download {
		public static final String Name = "Dl";

		/** 下载量 */
		public static final String Count = "Count";
		
		/** 大站视频下载失败量 */
		public static final String BigSiteFailCount = "BSFailCount";
		
		/** P2P视频下载失败量 */
		public static final String P2PFailCount = "P2PFailCount";
		
		/** 大站视频下载完成量 */
		public static final String BigSiteCompleteCount = "BSCompleteCount";
		
		/** P2P视频下载完成量 */
		public static final String P2PCompleteCount = "P2PCompleteCount";
	}
	
	public class Play {
		public static final String Name = "Pl";
		
		/** 总播放时长 */
		public static final String TotalTime = "TotalTime";
		/** 大站视频播放时长*/
		public static final String BigTotalTime="BigTotalTime";
		/**小站视频播放时长*/
		public static final String P2PTotalTime="P2PTotalTime";
		/** 大站视频播放总量*/
		public static final String BigTotalCount="BigTotalCount";
		/**小站视频播放总量*/
		public static final String P2PTotalCount="P2PTotalCount";
		/** 播放量 */
		public static final String Count = "Count";
		
		/** 有效观看量 */
		public static final String ValidCount = "ValidCount";
		
		/** 播放时长 */
		public static final String Time = "Time";
		
		/** 大站视频失败播放量 */
		public static final String BigSiteFailCount = "BSFailCount";
		
		/** P2P视频失败播放量 */
		public static final String P2PFailCount = "P2PFailCount";
	}
	
	public class PlCtr{
		public static final String Name="PlCtr";
		
		public static final String Btn_play="Btn_play";
		
		public static final String Btn_next="Btn_next";
		
		public static final String Btn_last="Btn_last";
		
		public static final String Btn_list="Btn_list";
		
		public static final String Btn_download="Btn_download";
		
		public static final String Btn_voice="Btn_voice";
		
		public static final String Btn_bright="Btn_bright";
		
		public static final String Btn_prograss="Btn_prograss";
		
		public static final String Hand_prograss="Hand_prograss";
		
		public static final String Hand_voice="Hand_voice";
		
		public static final String Hand_bright="Hand_bright";
		
		public static final String Btn_lock="Btn_lock";
	}
	public class App
	{
		public static final String Name = "App";
		
		// 有效使用
		public static final String ValidCount = "ValidCount";
	}
	
	public class Home
	{
		public static final String Name = "Home";
		public static final String PlayHis = "PlayHis";
		public static final String PlaySite = "PlaySite";
		public static final String Navigation = "Navigation";
		public static final String Search_ranking ="Search_ranking";
		public static final String Hot_ranking = "Hot_ranking";
	}
	
	public class Address {
		public static final String Name = "Address";
		public static final String Visite = "Visite";
		public static final String Search = "Search";
		public static final String Searchboard = "Searchboard";
		public static final String Sug_Bdplayer = "Sug_Bdplayer";
	}
	
	public class Browser {
		public static final String Name = "Browser";
		public static final String Forward = "Forward";
		public static final String Back = "Back";
		public static final String Reload = "Reload";
		public static final String Bookmark_add = "Bookmark_add";
		public static final String Menu = "Menu";
		public static final String Album = "Album";
		public static final String Multi_window = "Multi_window";
		public static final String Personal = "Personal";
	}
	
	public class Menu {
		public static final String Name = "Menu";
		public static final String Go_home = "Go_home";
		public static final String Bookmark_history = "Bookmark_history";
		public static final String Bookmard_add = "Bookmard_add";
		public static final String Reload = "Reload";
		public static final String Settings = "Settings";
		public static final String Clear = "Clear";
		public static final String Feedback = "Feedback";
		public static final String Exit = "Exit";
	}
	
	public class Partner {
		public static final String Name = "Partner";
	}
	public class MoreApp {
		public static final String Name = "MoreApp";
	}
	
	public class Computer {
		public static final int Device = 9001;
		public static final int CPUInfo = 9002;
		public static final int Resolution = 9003;
	}
	
	public class Playing {
		public static final String Name = "Playing";
		public static final int TaskHandle = 1001;
		public static final int FirstBufferTime = 1002;
		public static final int BufferCount = 1003;
	}
}
