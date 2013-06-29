/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>
#include <Android/log.h> 

#define TAG "JNIMsg"

#define ErrorCode_Success		0
#define ErrorCode_Unknown		1
#define ErrorCode_InvalParam	2

#define State_Unknown	0
#define State_Start		1
#define State_Stop		2
#define State_Complete	3
#define State_Error		4

typedef struct tagTaskInfo
{
	char fileName[100];
	char url[100];
	int state;
	int handle;
	int totalSize;
	int downloadedSize;

	struct tagTaskInfo* next;
} TaskInfo;


jclass taskInfoClazz;
jmethodID getHandle;
jmethodID setHandle;
jmethodID getUrl;
jmethodID setUrl;
jmethodID getFileName;
jmethodID setFileName;
jmethodID getState;
jmethodID setState;
jmethodID getDownloadedSize;
jmethodID setDownloadedSize;
jmethodID getTotalSize;
jmethodID setTotalSize;

void InitTaskInfoClazz( JNIEnv* jniEnv )
{
	taskInfoClazz = (*jniEnv)->FindClass(jniEnv,"com/baidu/player/JNIP2PTaskInfo");
	if( taskInfoClazz == 0 )
	{
		__android_log_print( ANDROID_LOG_INFO, TAG, "Init Cannot find JNIP2PTaskInfo class" );
		return;
	}

	getUrl = (*jniEnv)->GetMethodID(jniEnv, taskInfoClazz, "getUrl","()Ljava/lang/String;");
	if( getUrl == 0 )
		__android_log_print( ANDROID_LOG_INFO, TAG, "Init Cannot find JNIP2PTaskInfo::getUrl" );
	setUrl = (*jniEnv)->GetMethodID(jniEnv, taskInfoClazz, "setUrl","(Ljava/lang/String;)V");  
	if( setUrl == 0 )
		__android_log_print( ANDROID_LOG_INFO, TAG, "Init Cannot find JNIP2PTaskInfo::setUrl" );
	getFileName = (*jniEnv)->GetMethodID(jniEnv, taskInfoClazz, "getFileName","()Ljava/lang/String;");
	if( getFileName == 0 )
		__android_log_print( ANDROID_LOG_INFO, TAG, "Init Cannot find JNIP2PTaskInfo::getFileName" );
	setFileName = (*jniEnv)->GetMethodID(jniEnv, taskInfoClazz, "setFileName","(Ljava/lang/String;)V");
	if( setFileName == 0 )
		__android_log_print( ANDROID_LOG_INFO, TAG, "Init Cannot find JNIP2PTaskInfo::setFileName" );

	getHandle = (*jniEnv)->GetMethodID(jniEnv, taskInfoClazz, "getHandle","()I");
	if( getHandle == 0 )
		__android_log_print( ANDROID_LOG_INFO, TAG, "Init Cannot find JNIP2PTaskInfo::getHandle" );
	setHandle = (*jniEnv)->GetMethodID(jniEnv, taskInfoClazz, "setHandle","(I)V");
	if( setHandle == 0 )
		__android_log_print( ANDROID_LOG_INFO, TAG, "Init Cannot find JNIP2PTaskInfo::setHandle" );
	getState = (*jniEnv)->GetMethodID(jniEnv, taskInfoClazz, "getState","()I");
	if( getState == 0 )
		__android_log_print( ANDROID_LOG_INFO, TAG, "Init Cannot find JNIP2PTaskInfo::getState" );
	setState = (*jniEnv)->GetMethodID(jniEnv, taskInfoClazz, "setState","(I)V");
	if( setState == 0 )
		__android_log_print( ANDROID_LOG_INFO, TAG, "Init Cannot find JNIP2PTaskInfo::setState" );
	getDownloadedSize = (*jniEnv)->GetMethodID(jniEnv, taskInfoClazz, "getDownloadedSize","()I");
	if( getDownloadedSize == 0 )
		__android_log_print( ANDROID_LOG_INFO, TAG, "Init Cannot find JNIP2PTaskInfo::getDownloadedSize" );
	setDownloadedSize = (*jniEnv)->GetMethodID(jniEnv, taskInfoClazz, "setDownloadedSize","(I)V");
	if( setDownloadedSize == 0 )
		__android_log_print( ANDROID_LOG_INFO, TAG, "Init Cannot find JNIP2PTaskInfo::setDownloadedSize" );
	getTotalSize = (*jniEnv)->GetMethodID(jniEnv, taskInfoClazz, "getTotalSize","()I");
	if( getTotalSize == 0 )
		__android_log_print( ANDROID_LOG_INFO, TAG, "Init Cannot find JNIP2PTaskInfo::getTotalSize" );
	setTotalSize = (*jniEnv)->GetMethodID(jniEnv, taskInfoClazz, "setTotalSize","(I)V");
	if( setTotalSize == 0 )
		__android_log_print( ANDROID_LOG_INFO, TAG, "Init Cannot find JNIP2PTaskInfo::setTotalSize" );

	__android_log_print( ANDROID_LOG_INFO, TAG, "Inited" );
}

void UninitTaskInfoClazz( JNIEnv* jniEnv )
{
	if( taskInfoClazz != 0 )
		(*jniEnv)->DeleteLocalRef( jniEnv, taskInfoClazz );
	if( getUrl != 0 )
		(*jniEnv)->DeleteLocalRef( jniEnv, getUrl );
	if( setUrl != 0 )
		(*jniEnv)->DeleteLocalRef( jniEnv, setUrl );
	if( getFileName != 0 )
		(*jniEnv)->DeleteLocalRef( jniEnv, getFileName );
	if( setFileName != 0 )
		(*jniEnv)->DeleteLocalRef( jniEnv, setFileName );
	if( getHandle != 0 )
		(*jniEnv)->DeleteLocalRef( jniEnv, getHandle );
	if( setHandle != 0 )
		(*jniEnv)->DeleteLocalRef( jniEnv, setHandle );
	if( getState != 0 )
		(*jniEnv)->DeleteLocalRef( jniEnv, getState );
	if( setState != 0 )
		(*jniEnv)->DeleteLocalRef( jniEnv, setState );
	if( getDownloadedSize != 0 )
		(*jniEnv)->DeleteLocalRef( jniEnv, getDownloadedSize );
	if( setDownloadedSize != 0 )
		(*jniEnv)->DeleteLocalRef( jniEnv, setDownloadedSize );
	if( getTotalSize != 0 )
		(*jniEnv)->DeleteLocalRef( jniEnv, getTotalSize );
	if( setTotalSize != 0 )
		(*jniEnv)->DeleteLocalRef( jniEnv, setTotalSize );
}

void FillJavaTaskInfo( JNIEnv* jniEnv, TaskInfo* taskInfo, jobject obj )
{
	jstring jstrUrl = (*jniEnv)->NewStringUTF(jniEnv, taskInfo->url);  
	(*jniEnv)->CallVoidMethod(jniEnv, obj, setUrl,jstrUrl);
	(*jniEnv)->DeleteLocalRef(jniEnv, jstrUrl);

	jstring jstrFileName = (*jniEnv)->NewStringUTF(jniEnv, taskInfo->fileName);  
	(*jniEnv)->CallVoidMethod(jniEnv, obj, setFileName,jstrFileName);
	(*jniEnv)->DeleteLocalRef(jniEnv, jstrFileName);

	(*jniEnv)->CallVoidMethod(jniEnv, obj, setHandle, taskInfo->handle );
	(*jniEnv)->CallVoidMethod(jniEnv, obj, setState, taskInfo->state );
	(*jniEnv)->CallVoidMethod(jniEnv, obj, setTotalSize, taskInfo->totalSize );
	(*jniEnv)->CallVoidMethod(jniEnv, obj, setDownloadedSize, taskInfo->downloadedSize );
}

void FillCTaskInfo( JNIEnv* jniEnv, TaskInfo* taskInfo, jobject obj )
{
	jstring jstrUrl = (*jniEnv)->CallObjectMethod(jniEnv, obj, getUrl);
	const char* url = (char*) (*jniEnv)->GetStringUTFChars(jniEnv, jstrUrl, 0);
	strcpy( taskInfo->url, url );
	(*jniEnv)->ReleaseStringUTFChars(jniEnv, jstrUrl, url);
	(*jniEnv)->DeleteLocalRef(jniEnv, jstrUrl); 

	jstring jstrFileName = (*jniEnv)->CallObjectMethod(jniEnv, obj, getFileName);
	const char* fileName = (char*) (*jniEnv)->GetStringUTFChars(jniEnv, jstrFileName, 0);
	strcpy( taskInfo->fileName, fileName );
	(*jniEnv)->ReleaseStringUTFChars(jniEnv, jstrFileName, fileName);
	(*jniEnv)->DeleteLocalRef(jniEnv, jstrFileName);

	taskInfo->handle = (*jniEnv)->CallIntMethod(jniEnv, obj, getHandle);
	taskInfo->state = (*jniEnv)->CallIntMethod(jniEnv, obj, getState);
	taskInfo->totalSize = (*jniEnv)->CallIntMethod(jniEnv, obj, getTotalSize);
	taskInfo->downloadedSize = (*jniEnv)->CallIntMethod(jniEnv, obj, getDownloadedSize);
}

void logJTaskInfo( JNIEnv* jniEnv, jobject taskInfo )
{

}

TaskInfo* header = 0;
int curHandle = 1000000;
int curTotalSize = 1000000;
int curTimeTick = 0;

const char* findFileName( const char* src )
{
	int index = 0;
	int i = 0;
	for( ; i < strlen( src ); ++i )
	{
		if( *( src + i ) == '/' )
		{
			index = i;
		}
	}
	return src + index + 1;
}

TaskInfo* findTaskInfo( int handle )
{
	TaskInfo* current = header;
	while( current != 0 )
	{
		if( handle == current->handle )
		{
			return current;
		}
		current = current->next;
	}
	return 0;
}

TaskInfo* findPreTaskInfo( int handle )
{
	TaskInfo* current = header;
	while( current != 0 )
	{
		if( current->next != 0 && handle == current->next->handle )
		{
			return current;
		}
		current = current->next;
	}
	return 0;
}

// 刷新所有任务
void RefreshAllTask()
{
	TaskInfo* current = header;
	while( current != 0 )
	{
		switch ( current->state )
		{
		case State_Start:
			if( current->totalSize == 0 )
			{
				current->totalSize = curTotalSize + 10000;
			}
			current->downloadedSize += 10000;
			if( current->downloadedSize >= current->totalSize )
			{
				current->downloadedSize = current->totalSize;
				current->state = State_Complete;
			}
			break;
		case State_Stop:
		case State_Complete:
		case State_Error:
			break;
		default:
			break;
		}
		current = current->next;
	}
}

jint
Java_com_baidu_player_JNIP2P_netInit(JNIEnv * jniEnv, jobject thiz)
{
	__android_log_print( ANDROID_LOG_INFO, TAG, "Init" );
	InitTaskInfoClazz( jniEnv );
	return ErrorCode_Success;
}

jint
Java_com_baidu_player_JNIP2P_netQuit(JNIEnv * jniEnv, jobject thiz)
{
	__android_log_print( ANDROID_LOG_INFO, TAG, "Quit" );

	TaskInfo* current = header;
	while( 1 )
	{
		if( current != 0 )
		{
			TaskInfo* tmp = current;
			current = current->next;
			free( tmp );
		}
	}
	header = 0;

	return ErrorCode_Success;
}

jint 
Java_com_baidu_player_JNIP2P_netCreate(JNIEnv * jniEnv, jobject jthis, jstring url, jstring fileName)
{
	__android_log_print( ANDROID_LOG_INFO, TAG, "Create url %s", (*jniEnv)->GetStringUTFChars(jniEnv, url, NULL) );
	TaskInfo* taskInfo = ( TaskInfo* )malloc( sizeof(TaskInfo) );
	memset( taskInfo, 0, sizeof( taskInfo ) );
	taskInfo->handle = ++curHandle;
	taskInfo->state = 0;
	taskInfo->totalSize = 0;
	taskInfo->downloadedSize = 0;

	const char *utf8Url = (*jniEnv)->GetStringUTFChars(jniEnv, url, NULL);
	strcpy( taskInfo->url, utf8Url );
	strcpy( taskInfo->fileName, findFileName( utf8Url ) );

	taskInfo->next = header;
	header = taskInfo;

	__android_log_print( ANDROID_LOG_INFO, TAG, "Created %s", utf8Url );
	return taskInfo->handle;
}

jint Java_com_baidu_player_JNIP2P_netDelete(JNIEnv * jniEnv, jobject jthis, jint handle)
{
	__android_log_print( ANDROID_LOG_INFO, TAG, "Delete %d", handle );
	TaskInfo* taskInfo = findTaskInfo( handle );
	if( taskInfo == 0 )
	{
		return ErrorCode_InvalParam;
	}
	TaskInfo* preTaskInfo = findPreTaskInfo( handle );
	if( preTaskInfo != 0 )
	{
		preTaskInfo->next = taskInfo->next;
	}
	else
	{
		header = 0;
	}
	free( taskInfo );
	return ErrorCode_Success;
}

jint Java_com_baidu_player_JNIP2P_netStart(JNIEnv * jniEnv, jobject jthis, jint handle)
{
	__android_log_print( ANDROID_LOG_INFO, TAG, "Start %d", handle );
	TaskInfo* taskInfo = findTaskInfo( handle );
	if( taskInfo == 0 )
	{
		return ErrorCode_InvalParam;
	}
	taskInfo->state = State_Start;
	return ErrorCode_Success;
}

jint Java_com_baidu_player_JNIP2P_netStop(JNIEnv * jniEnv, jobject jthis, jint handle)
{
	__android_log_print( ANDROID_LOG_INFO, TAG, "Stop %d", handle );
	TaskInfo* taskInfo = findTaskInfo( handle );
	if( taskInfo == 0 )
	{
		return ErrorCode_InvalParam;
	}
	taskInfo->state = State_Stop;
	return ErrorCode_Success;
}

jint Java_com_baidu_player_JNIP2P_netQueryTaskInfo(JNIEnv * jniEnv, jobject jthis, jint handle, jobject jTaskInfo)
{
	__android_log_print( ANDROID_LOG_INFO, TAG, "Query %d", handle );
	TaskInfo* taskInfo = findTaskInfo( handle );
	if( taskInfo == 0 )
	{
		return ErrorCode_InvalParam;
	}
	RefreshAllTask();
	FillJavaTaskInfo( jniEnv, taskInfo, jTaskInfo );
	__android_log_print( ANDROID_LOG_INFO, TAG, "Queryed %d url %s", handle, ( taskInfo == 0 ) ? "" : taskInfo->url );
	return ErrorCode_Success;
}

jint Java_com_baidu_player_JNIP2P_netSetDownloadPath(JNIEnv * jniEnv, jobject jthis, jstring path)
{
	return ErrorCode_Success;
}

jint Java_com_baidu_player_JNIP2P_netParseURL(JNIEnv * jniEnv, jobject jthis, jstring url, jobject jTaskInfo)
{
	__android_log_print( ANDROID_LOG_INFO, TAG, "ParseUrl url %s", (*jniEnv)->GetStringUTFChars(jniEnv, url, NULL) );
	TaskInfo* taskInfo = ( TaskInfo* )malloc( sizeof(TaskInfo) );
	memset( taskInfo, 0, sizeof( taskInfo ) );

	const char *utf8Url = (*jniEnv)->GetStringUTFChars(jniEnv, url, NULL);
	strcpy( taskInfo->fileName, findFileName( utf8Url ) );

	FillJavaTaskInfo( jniEnv, taskInfo, jTaskInfo );
	return ErrorCode_Success;
}

jint Java_com_baidu_player_JNIP2P_netSetSetting(JNIEnv * jniEnv, jobject jthis, jstring url)
{
	return ErrorCode_Success;
}

jint Java_com_baidu_player_JNIP2P_netSetNetworkStatus(JNIEnv * jniEnv, jobject jthis, jboolean canUse)
{
	return ErrorCode_Success;
}