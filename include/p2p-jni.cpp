#ifndef APP 

#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <p2pservice.h>
#include <android/log.h>
#include <XDefine.h>
#include <XTime.h>
#include <XFile.h>
#include <P2PStatReport.h>
#include <crc.h>
/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/HelloJni/HelloJni.java
 */


extern "C" {
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netInit( JNIEnv* env, jobject obj );
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netQuit( JNIEnv* env, jobject obj );
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netCreate( JNIEnv* env, jobject obj, jobject param);
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netStart( JNIEnv* env, jobject obj, jlong hTaskHandle);
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netStop( JNIEnv* env, jobject obj, jlong hTaskHandle);
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netDelete( JNIEnv* env, jobject obj, jlong hTaskHandle);
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetPlaying( JNIEnv* env, jobject obj, jlong hTaskHandle, jboolean bPlaying);
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetMediaTime( JNIEnv* env, jobject obj, jlong hTaskHandle, jint nMediaTimeSecond);
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netQueryTaskInfo( JNIEnv* env, jobject obj, jlong hTaskHandle, jobject info);
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netGetRedirectUrl( JNIEnv* env, jobject obj, jlong hTaskHandle, jobject info);
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netParseURL( JNIEnv* env, jobject obj, jstring strUrl, jobject taskInfo);
	
	
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netRead( JNIEnv* env, jobject obj, jlong hTaskHandle, jlong nOffset, jclass pBuffer, jlong nToRead, jboolean bMove);
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netAddEmergencyRange( JNIEnv* env, jobject obj, jlong hTaskHandle, jlong nBegin, jlong nEnd);	
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetPriorityWindow( JNIEnv* env, jobject obj, jlong hTaskHandle, jlong nShortBufLen, jlong nLongBufLen);	
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netGetBlockInfo( JNIEnv* env, jobject obj, jlong hTaskHandle, jclass pBuffer, jlong nToRead);	
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetCacheSize( JNIEnv* env, jobject obj, jlong nCacheSize);
	//JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetTaskCacheSize( JNIEnv* env, jobject obj, jlong hTaskHandle, jint nCacheSize);
	
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netGetPeerCount( JNIEnv* env, jobject obj, jlong hTaskHandle);	
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netDeleteFile( JNIEnv* env, jobject obj, jstring strFilePath, jstring strFileName, jlong nFileSize);	
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netDeleteDir( JNIEnv* env, jobject obj, jstring strFilePath);	
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netFileExist( JNIEnv* env, jobject obj, jstring strFilePath, jstring strFileName, jlong nFileSize);	
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netQueryTaskStat( JNIEnv* env, jobject obj, jlong hTaskHandle, jobject stat);
	
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetSpeedLimit( JNIEnv* env, jobject obj, jint nSpeedLimit);
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetTaskSpeedLimit( JNIEnv* env, jobject obj, jlong hTaskHandle, jint nSpeedLimit);
	
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetLogLevel( JNIEnv* env, jobject obj, jint nLevel);
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetDeviceID( JNIEnv* env, jobject obj, jstring strDeviceID);
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netReportFirstBufferTime( JNIEnv* env, jobject obj, jstring strVersion, jlong hTaskHandle, jint nBufferMilliSecond);	
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netEncrypt( JNIEnv* env, jobject obj, jobject inobj, jobject outobj);
	JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netStatReport( JNIEnv* env, jobject obj, jstring strProduct, jstring strVersion, jstring strSubstat, jstring strChannel, jstring strBody);
};
JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netInit( JNIEnv* env, jobject obj )
{
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","Init");
	return p2pservice_init(16, false);
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netQuit( JNIEnv* env, jobject obj )
{
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","Quit");
	int ret = p2pservice_destroy();
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","QuitEnd");
	return (jint)ret;
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netCreate( JNIEnv* env, jobject obj, jobject param )
{
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","CreateTask");

	task_param_t  tp;
	task_handle_t h = NULL;

        jclass paramClass = env->GetObjectClass(param);

        jfieldID id_nFlag = env->GetFieldID(paramClass,"nFlag","I");
        //jint j_nFlag = 4;
        jint j_nFlag = env->GetIntField(param, id_nFlag);

        jfieldID id_strUrl = env->GetFieldID(paramClass,"strUrl","Ljava/lang/String;");
	jstring j_strUrl = (jstring)env->GetObjectField(param, id_strUrl);

        jfieldID id_strRefer = env->GetFieldID(paramClass,"strRefer","Ljava/lang/String;");
        jstring j_strRefer = (jstring)env->GetObjectField(param, id_strRefer);

        jfieldID id_strSavePath = env->GetFieldID(paramClass,"strSavePath","Ljava/lang/String;");
        jstring j_strSavePath = (jstring)env->GetObjectField(param, id_strSavePath);

        jfieldID id_strFileName = env->GetFieldID(paramClass,"strFileName","Ljava/lang/String;");
        jstring j_strFileName = (jstring)env->GetObjectField(param, id_strFileName);

	tp.url  = (char*)env->GetStringUTFChars(j_strUrl, false);
	tp.refer  = (char*)env->GetStringUTFChars(j_strRefer, false);
	tp.path = (char*)env->GetStringUTFChars(j_strSavePath, false);
	tp.filename = (char*)env->GetStringUTFChars(j_strFileName, false);
	tp.flag = j_nFlag<<16;
		
	char szTempBuffer[1024];
		
                if(!XFile::dir_exist(tp.path))
                {
                        if(!XFile::dir_create(tp.path, true))
                        {
				if(tp.flag==0x00020000)
				{
					//cache to memory
					tp.flag=0x00040000;
					sprintf(szTempBuffer,"Create %s Fail, SDCard may not found, Stream Task Cache to Memory", tp.path);
					__android_log_write(ANDROID_LOG_DEBUG,"P2P",szTempBuffer);
				}
				else if(tp.flag==0x00040000)
				{
					//
				}
				else
				{
					sprintf(szTempBuffer,"Create %s Fail, SDCard may not found, Normal Task CreateTask Fail", tp.path);
					__android_log_write(ANDROID_LOG_DEBUG,"P2P",szTempBuffer);
					return (jint)ERROR_CREATE_FAIL;	
				}
					
                        }
                }
	
	int ret = p2pservice_task_create(&tp, &h);

	jfieldID id_nHandle = env->GetFieldID(paramClass,"nHandle","J");
	env->SetLongField(param, id_nHandle, (jlong)h);
		
	char temp_log[1024]={0};
	sprintf(temp_log, "Create, h=%u, Ret=%d, flag=%d, url=%s, path=%s, fname=%s", (task_handle_t)h, ret, tp.flag, tp.url, tp.path, tp.filename);
	__android_log_write(ANDROID_LOG_DEBUG,"P2P",temp_log);		
	
	return (jint)ret;
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netStart( JNIEnv* env, jobject obj, jlong hTaskHandle )
{
	char temp_log[1024]={0};
	sprintf(temp_log, "Start, h=%u", (task_handle_t)hTaskHandle);
	__android_log_write(ANDROID_LOG_DEBUG,"P2P", temp_log);
	return (jint)p2pservice_task_start((task_handle_t)hTaskHandle);
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netStop( JNIEnv* env, jobject obj, jlong hTaskHandle )
{
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","Stop");
	return (jint)p2pservice_task_stop((task_handle_t)hTaskHandle);
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netDelete( JNIEnv* env, jobject obj, jlong hTaskHandle )
{
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","Delete");
	return (jint)p2pservice_task_destroy((task_handle_t)hTaskHandle);
}

/*

	class NetTaskInfo {
		  public int bErrorCode;   //错误码
		  public int bStatus;      //任务状态 
		  public long downloadLen;   //已下载大小
		  public int downloadRate;  //下载速度
		  public long fileLen;      //文件大小
		  public String szFileName; //文件名
		  public byte[] szReserved; //保留
	} 

typedef struct task_info_s
{
	int state; 
	int error;
	uint64_t filesize;
	uint64_t downloaded;
	int downspeed;
	int upspeed;
	int using_peers;
	int total_peers;
	int seeder_peers;
	int elapse;
	uint64_t uploaded;
} task_info_t;

*/

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netQueryTaskInfo( JNIEnv* env, jobject obj, jlong hTaskHandle, jobject info)
{
	unsigned int nTickBegin = GET_TICK();

	task_info_t ti;
	int ret = p2pservice_task_info( (task_handle_t)hTaskHandle, &ti );

        //task_stat_t ts;
        //p2pservice_task_stat((task_handle_t)hTaskHandle,&ts);

	char temp_log[1024]={0};

	if(ret!=0)
	{
		sprintf(temp_log, "p2pservice_task_info error, h=%u, ret=%u", (task_handle_t)hTaskHandle, ret);
		__android_log_write(ANDROID_LOG_DEBUG,"P2P",temp_log);
		return (jint)ret;
	}
	
	unsigned int nTickEnd = GET_TICK();

	//sprintf(temp_log, "[%d], h=%u, State=%d, Error=%d, Down=%llu, Dup=%llu, Speed=%d, Playing=%d, FSize=%llu, WinPos=%llu, UnFPos=%llu, FName=%s, DiskF=%d", nTickEnd - nTickBegin, (task_handle_t)hTaskHandle, ti.state, ti.error, ti.downloaded, ts.down_dup, ti.downspeed/1024, ts.is_playing, ti.filesize, ts.win_pos, ts.unfinish_pos, ti.szFilename, ts.diskfiles);
	sprintf(temp_log, "[%d], h=%u, State=%d, Error=%d, Down=%llu, Speed=%d, FSize=%llu, FName=%s", nTickEnd - nTickBegin, (task_handle_t)hTaskHandle, ti.state, ti.error, ti.downloaded, ti.downspeed/1024, ti.filesize, ti.szFilename);
	__android_log_write(ANDROID_LOG_VERBOSE,"P2P",temp_log);
	
	jclass infoClass = env->GetObjectClass(info);
	jfieldID id_nErrorCode = env->GetFieldID(infoClass,"nErrorCode","I");
	env->SetIntField(info,id_nErrorCode,(jint)ti.error);

	
	jfieldID id_nStatus = env->GetFieldID(infoClass,"nStatus","I");
	env->SetIntField(info,id_nStatus,(jint)ti.state);
	
	jfieldID id_downloadLen = env->GetFieldID(infoClass,"nDownloadLen","J");
	env->SetLongField(info,id_downloadLen,(jlong)ti.downloaded);
	
	jfieldID id_downloadRate = env->GetFieldID(infoClass,"nDownloadRate","I");
	env->SetIntField(info,id_downloadRate,(jint)ti.downspeed);
	
	jfieldID id_fileLen = env->GetFieldID(infoClass,"nFileLen","J");
	env->SetLongField(info,id_fileLen,(jlong)ti.filesize);

	jfieldID id_diskfiles = env->GetFieldID(infoClass,"nDiskFiles","I");
	env->SetIntField(info,id_diskfiles,(jint)ti.ndiskfiles);
	
	jfieldID id_filename = env->GetFieldID(infoClass,"szFileName","[B");
	
	jbyteArray jbyte_arr=env->NewByteArray(strlen(ti.szFilename));
	env->SetByteArrayRegion(jbyte_arr, 0, strlen(ti.szFilename), (jbyte*)ti.szFilename);
	env->SetObjectField(info, id_filename, jbyte_arr);
	return (jint)ret;
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netGetRedirectUrl( JNIEnv* env, jobject obj, jlong hTaskHandle, jobject info)
{
	TCHAR szURL[MAX_URL_LEN];
	int ret = p2pservice_get_redirect( (task_handle_t)hTaskHandle, szURL);

        char temp_log[1024]={0};

        {
                sprintf(temp_log, "p2pservice_get_redirect, h=%u, url=%s", (task_handle_t)hTaskHandle, szURL);
                __android_log_write(ANDROID_LOG_DEBUG,"P2P",temp_log);
        }
	
	if(strlen(szURL)==0)
	{
		return (jint)ERROR_UNKONWN;
	}
	
	jclass infoClass = env->GetObjectClass(info);
	jfieldID id_url = env->GetFieldID(infoClass,"szUrl","[B");
	jbyteArray jbyte_arr=env->NewByteArray(strlen(szURL));
	env->SetByteArrayRegion(jbyte_arr, 0, strlen(szURL), (jbyte*)szURL);
	env->SetObjectField(info, id_url, jbyte_arr);
	return (jint)ret;
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netQueryTaskStat( JNIEnv* env, jobject obj, jlong hTaskHandle, jobject stat)
{
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","QueryTaskStat");
	task_stat_t ts;
	int ret = p2pservice_task_stat( (task_handle_t)hTaskHandle, &ts );
        char temp_log[1024]={0};

        if(ret!=0)
        {
                sprintf(temp_log, "p2pservice_task_stat error, h=%u, ret=%u", (task_handle_t)hTaskHandle, ret);
                __android_log_write(ANDROID_LOG_DEBUG,"P2P",temp_log);
                return (jint)ret;
        }
        sprintf(temp_log, "h=%u, DownDup=%llu", (task_handle_t)hTaskHandle, ts.down_dup);
	__android_log_write(ANDROID_LOG_DEBUG,"P2P",temp_log);
	
	jclass statClass = env->GetObjectClass(stat);
        jfieldID id_nTotalDup = env->GetFieldID(statClass,"nTotalDup","J");
        env->SetLongField(stat,id_nTotalDup,(jlong)ts.down_dup);
	
	return (jint)ret;	
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netParseURL( JNIEnv* env, jobject obj, jstring strUrl, jobject info)
{
	
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","ParseURL");

	char* szURL  = (char*)env->GetStringUTFChars(strUrl, false);
	url_info_t ui;
	
	int ret = p2pservice_parse_url(szURL, &ui);
	jclass infoClass = env->GetObjectClass(info);
	jfieldID id_fileLen = env->GetFieldID(infoClass,"nFileLen","J");
	env->SetLongField(info,id_fileLen,(jlong)ui.nFileSize);
	jfieldID id_filename = env->GetFieldID(infoClass,"szFileName","[B");

	//jbyteArray jbyte_arr = (jbyteArray)env->GetObjectField(info, id_filename);
	jbyteArray jbyte_arr=env->NewByteArray((jsize)strlen(ui.szFilename));
	
	env->SetByteArrayRegion(jbyte_arr, 0, strlen(ui.szFilename), (jbyte*)ui.szFilename);
	env->SetObjectField(info, id_filename, jbyte_arr);
		
	return (jint)ret;
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netRead( JNIEnv* env, jobject obj, jlong hTaskHandle, jlong nOffset, jclass pBuffer, jlong nToRead, jboolean bMove)
{
	//__android_log_write(ANDROID_LOG_DEBUG,"P2P","Read");
	//p2pservice_read
	char *pNewBuffer = new char[nToRead];
	int ret = p2pservice_read((task_handle_t)hTaskHandle, (unsigned __int64)nOffset, pNewBuffer, (unsigned __int64)nToRead, (bool)bMove);
	
	jclass bufferClass = env->GetObjectClass(pBuffer);
	jfieldID id_arrays = env->GetFieldID(bufferClass,"szBuffer","[B");

	jbyteArray jbyte_arr = (jbyteArray)env->GetObjectField(pBuffer, id_arrays);
	if(ret>0)
	{
		env->SetByteArrayRegion(jbyte_arr, 0, ret, (jbyte*)pNewBuffer);
	}	
	delete []pNewBuffer;
	
	return (jint)ret;
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netAddEmergencyRange( JNIEnv* env, jobject obj, jlong hTaskHandle, jlong nBegin, jlong nEnd)
{
	return (jint)p2pservice_add_emergency_range((task_handle_t)hTaskHandle, nBegin, nEnd);
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetPriorityWindow( JNIEnv* env, jobject obj, jlong hTaskHandle, jlong nShortBufLen, jlong nLongBufLen)
{
	return (jint)p2pservice_set_priority_window((task_handle_t)hTaskHandle, nShortBufLen, nLongBufLen);
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netGetBlockInfo( JNIEnv* env, jobject obj, jlong hTaskHandle, jclass pBuffer, jlong nToRead)
{

	__android_log_write(ANDROID_LOG_DEBUG,"P2P","GetBlockInfo");
	char *pNewBuffer = new char[nToRead];
	int ret = p2pservice_get_block_info((task_handle_t)hTaskHandle, pNewBuffer, (unsigned __int64)nToRead);
	
	jclass bufferClass = env->GetObjectClass(pBuffer);
	jfieldID id_arrays = env->GetFieldID(bufferClass,"szBuffer","[B");

	jbyteArray jbyte_arr = (jbyteArray)env->GetObjectField(pBuffer, id_arrays);
	if(ret>0)
	{
		env->SetByteArrayRegion(jbyte_arr, 0, ret, (jbyte*)pNewBuffer);
	}	
	delete []pNewBuffer;

	jfieldID id_blocksize = env->GetFieldID(bufferClass,"nBlockSize","I");
	int nBlockSize = p2pservice_get_block_size((task_handle_t)hTaskHandle);

	env->SetIntField(pBuffer, id_blocksize, (jint)nBlockSize);
	
	return (jint)ret;
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netTest( JNIEnv* env, jobject obj, jclass pBuffer)
{
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","Test");
	
	const char *pNewBuffer = "JNI array test";

	jclass bufferClass = env->GetObjectClass(pBuffer);
	jfieldID id_arrays = env->GetFieldID(bufferClass,"szBuffer","[B");

	jbyteArray jbyte_arr = (jbyteArray)env->GetObjectField(pBuffer, id_arrays);

	env->SetByteArrayRegion(jbyte_arr, 0, 14, (jbyte*)pNewBuffer);	
	return (jint)0;
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetCacheSize( JNIEnv* env, jobject obj, jint nCacheSize)
{
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","SetCacheSize");
	return (jint)p2pservice_set_cache_size(nCacheSize);
}

/*

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetTaskCacheSize( JNIEnv* env, jobject obj, jlong hTaskHandle, jint nCacheSize)
{
	return (jint)p2pservice_set_cache_size((task_handle_t)hTaskHandle, nCacheSize);
}
*/

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netGetPeerCount( JNIEnv* env, jobject obj, jlong hTaskHandle)
{
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","GetPeerCount");
	return (jint)p2pservice_get_peer_count((task_handle_t)hTaskHandle);
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netDeleteFile( JNIEnv* env, jobject obj, jstring strFilePath, jstring strFileName, jlong nFileSize)
{
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","DeleteFile");
	char* szFilePath  = (char*)env->GetStringUTFChars(strFilePath, false);
	char* szFileName  = (char*)env->GetStringUTFChars(strFileName, false);
	return (jint)p2pservice_delete_file(szFilePath, szFileName, (unsigned __int64)nFileSize); 
}	

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netDeleteDir( JNIEnv* env, jobject obj, jstring strFilePath)
{
        __android_log_write(ANDROID_LOG_DEBUG,"P2P","DeleteDir");
        char* szFilePath  = (char*)env->GetStringUTFChars(strFilePath, false);
        return (jint)p2pservice_delete_dir(szFilePath);	
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netFileExist( JNIEnv* env, jobject obj, jstring strFilePath, jstring strFileName, jlong nFileSize)
{
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","FileExist");
	char* szFilePath  = (char*)env->GetStringUTFChars(strFilePath, false);
	char* szFileName  = (char*)env->GetStringUTFChars(strFileName, false);
	return (jint)p2pservice_file_exist(szFilePath, szFileName, (unsigned __int64)nFileSize); 
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetPlaying( JNIEnv* env, jobject obj, jlong hTaskHandle, jboolean bPlaying)
{
	return (jint)p2pservice_set_playing((task_handle_t)hTaskHandle, (bool)bPlaying);
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetMediaTime( JNIEnv* env, jobject obj, jlong hTaskHandle, jint nMediaTimeSecond)
{
	return (jint)p2psevice_set_media_time((task_handle_t)hTaskHandle, (unsigned int)nMediaTimeSecond);
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetSpeedLimit( JNIEnv* env, jobject obj, jint nSpeedLimit)
{
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","SetSpeedLimit");
	return (jint)p2pservice_set_speed_limit((unsigned int)nSpeedLimit);
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetTaskSpeedLimit( JNIEnv* env, jobject obj, jlong hTaskHandle, jint nSpeedLimit) {
	__android_log_write(ANDROID_LOG_DEBUG,"P2P","SetTaskSpeedLimit");
	return (jint)p2pservice_set_task_speed_limit((task_handle_t)hTaskHandle, (unsigned int)nSpeedLimit);
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetLogLevel( JNIEnv* env, jobject obj, jint nLevel )
{
	char szLogBuf[1024];
	sprintf(szLogBuf, "%s %d", "SetLogLevel",  (int)nLevel);
        __android_log_write(ANDROID_LOG_DEBUG,"P2P",szLogBuf);

        return (jint)p2pservice_set_log_level((unsigned int)nLevel);
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netSetDeviceID( JNIEnv* env, jobject obj, jstring strDeviceID)
{
	char* szDeviceID  = (char*)env->GetStringUTFChars(strDeviceID, false);
	
	if(szDeviceID==NULL) return ERROR_PARAM;

        char szLogBuf[1024];
        sprintf(szLogBuf, "%s %s", "SetDeviceID",  szDeviceID);
        __android_log_write(ANDROID_LOG_DEBUG,"P2P",szLogBuf);

	return (jint)p2pservice_set_deviceid(szDeviceID);
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netReportFirstBufferTime( JNIEnv* env, jobject obj, jstring strVersion, jlong hTaskHandle, jint nBufferMilliSecond)
{
	char* szVersion = (char*)env->GetStringUTFChars(strVersion, false);
	if(szVersion==NULL) return ERROR_PARAM;
	char szLogBuf[1024];
        sprintf(szLogBuf, "ReportFirstBufferTime, ver=%s, h=%d, time=%d", szVersion, (long)hTaskHandle, (int)nBufferMilliSecond);
        __android_log_write(ANDROID_LOG_DEBUG,"P2P",szLogBuf);

	CP2PStatReport rp("AndroidVideo", szVersion, "", "FirstBuffer");
	rp.StatAdd(0, (unsigned char*)VERSION, strlen(VERSION));
	rp.StatAdd(1, hTaskHandle);
	rp.StatAdd(2, nBufferMilliSecond);

	return ERROR_SUCCESS; 
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netEncrypt( JNIEnv* env, jobject obj, jobject inobj, jobject outobj)
{
        __android_log_write(ANDROID_LOG_DEBUG,"P2P","Encode");


        jclass inClass = env->GetObjectClass(inobj);
        jfieldID id_nInLength = env->GetFieldID(inClass,"nLength","I");
        jint j_nInLength = env->GetIntField(inobj, id_nInLength);
        jfieldID id_szInBUffer = env->GetFieldID(inClass,"szBuffer","[B");
        jbyteArray jbyte_inarr = (jbyteArray)env->GetObjectField(inobj, id_szInBUffer);
///
        jbyte* jbyte_in = (jbyte *)malloc(j_nInLength * sizeof(jbyte));
        env->GetByteArrayRegion(jbyte_inarr,0,j_nInLength,jbyte_in);
///
	P2P::crc32Buffer((char*)jbyte_in, j_nInLength);
///
        jclass outClass = env->GetObjectClass(outobj);
        jfieldID id_nOutLength = env->GetFieldID(outClass,"nLength","I");
        jfieldID id_szOutBUffer = env->GetFieldID(outClass,"szBuffer","[B");
        jbyteArray jbyte_outarr=env->NewByteArray((jsize)j_nInLength);

        env->SetByteArrayRegion(jbyte_outarr, 0, j_nInLength, jbyte_in);
        env->SetObjectField(outobj, id_szOutBUffer, jbyte_outarr);

	free(jbyte_in);

	return ERROR_SUCCESS;
}

bool SplitParams(const std::string &strIn, std::string flag, std::vector<std::string>& vecOut)
{
	vecOut.clear();
	if (strIn.empty())
		return false;
	std::string::size_type begin = 0;
	std::string::size_type end = strIn.find(flag,begin);
	while (end != std::string::npos)
	{
		std::string str = strIn.substr(begin, end - begin);

		if (str.empty())
		{
			begin=end+flag.size();
			end = strIn.find(flag,begin);
			continue;
		}
		vecOut.push_back(str);
		begin = end+flag.size();
		end = strIn.find(flag,begin);
	}

	std::string str = strIn.substr(begin, end - begin);
	if (!str.empty())
	{
		vecOut.push_back(str);
	}
	return true;
}

bool is_all_num(const std::string &strIn)
{
	for(size_t i=0; i<strIn.length(); i++)
	{
		if(strIn.at(i)<'0' || strIn.at(i)>'9')
		{
			return false;
		}
	}
	return true;
}

JNIEXPORT jint JNICALL Java_com_baidu_video_download_JNIP2P_netStatReport( JNIEnv* env, jobject obj, jstring strProduct, jstring strVersion, jstring strSubstat, jstring strChannel, jstring strBody)
{
	char* szProduct = (char*)env->GetStringUTFChars(strProduct, false);	
	char* szVersion = (char*)env->GetStringUTFChars(strVersion, false);	
	char* szSubstat = (char*)env->GetStringUTFChars(strSubstat, false);	
	char* szChannel = (char*)env->GetStringUTFChars(strChannel, false);	
	if(szProduct==NULL || szVersion==NULL || szSubstat==NULL || szChannel==NULL) return ERROR_PARAM; 

	char* szBody = (char*)env->GetStringUTFChars(strBody, false);
	std::string strTemp = szBody;
	
	std::vector<std::string> vecOut;
	SplitParams(strTemp, ",", vecOut);

	if(vecOut.size()==0) return ERROR_PARAM;

	CP2PStatReport rp(szProduct, szVersion, szChannel, szSubstat);

	for(size_t i=0; i<vecOut.size(); i++)
	{
		std::vector<std::string> vecItem;
		SplitParams(vecOut[i], "=", vecItem);
		if(vecItem.size()!=2) return ERROR_PARAM;
		
		if(is_all_num(vecItem[0]))
		{
			uint64_t nKey = _atoi64(vecItem[0].c_str());	
			if(is_all_num(vecItem[1]))
			{
				rp.StatAdd(nKey, _atoi64(vecItem[1].c_str()));
			}
			else
			{
				rp.StatAdd(nKey, (unsigned char*)vecItem[1].c_str(), vecItem[1].length());
			}
		}
		else
		{
			continue;
		}
		
	
	}

	return ERROR_SUCCESS;
}
	
#endif
