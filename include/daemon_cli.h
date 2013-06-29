#ifndef __DAEMON_CLI_H__
#define __DAEMON_CLI_H__

#include "p2pservice.h"

#define DAEMON_SEND_FAIL -100
#define DAEMON_RECV_HEAD_FAIL -101
#define DAEMON_RECV_BODY_FAIL -102

#ifdef __cplusplus
extern "C" {
#endif

int remote_task_info( task_handle_t h, task_info_t* inf);

int remote_read(task_handle_t h, unsigned __int64 nOffset, char* pBuffer, unsigned __int64 nToRead, bool bMove);

int remote_read_file(const char* szFileName, unsigned __int64 nFileSize, unsigned __int64 nOffset, char* pBuffer, unsigned __int64 nToRead);

int remote_add_emergency_range(task_handle_t h, unsigned __int64 nBegin, unsigned __int64 nEnd);
        
int remote_set_priority_window(task_handle_t h, unsigned __int64 nShortBufLen, unsigned __int64 nLongBufLen);

int remote_set_bitrate(task_handle_t h, unsigned int nBitRate);

#ifdef __cplusplus
};
#endif

#endif
