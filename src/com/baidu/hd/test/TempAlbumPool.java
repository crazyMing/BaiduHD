package com.baidu.hd.test;

import java.util.ArrayList;

import com.baidu.hd.module.album.Album;
import com.baidu.hd.module.album.BigServerAlbum;
import com.baidu.hd.module.album.NetVideo;
import com.baidu.hd.module.album.VideoFactory;

@Deprecated
public class TempAlbumPool {

	public static Album getMockAlbum(){
		BigServerAlbum album=new BigServerAlbum();
		String[] arr=new String[]{
				"bdhd://293602988|9CA49A635394427206325D7F88653CF4|[TSKS]想你01.rmvb",
				"bdhd://286464167|4E399199429B75B6A677E14E9A577D66|[TSKS]想你02.rmvb",
				"bdhd://286745972|69290B41770DCD9443E96DFC76960AFB|[TSKS]想你03.rmvb",
				"bdhd://287512142|80418B3D6B3A1C458252DA8EC5B4068C|[TSKS]想你04",
				"bdhd://282340237|08F433A8AFE70DFB1F95521E17D45BBE|[TSKS]想你05",
				"bdhd://285255074|AFF5825560B26462EAA730E5E4D9A34A|[TSKS]想你06",
				"bdhd://288507688|D079D2612635B833801BF36BECF97413|[TSKS]想你07",
				"bdhd://285267276|CFB20FFE72E8D56D18B584B52BB438EE|[TSKS]想你08",
				"bdhd://257975245|79B3FF11E39EDBA22B5AA413C2203DF2|[TSKS]想你09",
				"bdhd://286277198|54C7E9F89270D0FAB798221F7577FBE1|[TSKS]想你10",
				"bdhd://284819327|60D41E1821FDB3367F16ADC5BA6FEC91|[TSKS]想你11",
				"bdhd://284704823|A841EC39EBE3452EB96618F9D834917A|[TSKS]想你12",
				"bdhd://226337513|3A7BFB25E6EA6E2C6F13E30B866F8B85|想你13.rmvb",
				};
		ArrayList<NetVideo> vlist=new ArrayList<NetVideo>();
		for(String str:arr){
			NetVideo video = VideoFactory.create(false).toNet();
			video.setType(NetVideo.NetVideoType.P2P_STREAM);
			video.setName(parseUrl(str));
			video.setUrl(str);
			vlist.add(video);
		}
		album.setVideos(vlist);
		return album;
	}
	
	public static  String parseUrl(String bdhd) {
		int pos;
		try{
		for (int i=0; i<2; ++i) {
			pos = bdhd.indexOf('|') + 1;
			bdhd = bdhd.substring(pos);
		}}catch (Exception e) {
		}
		return bdhd;
	}

}
