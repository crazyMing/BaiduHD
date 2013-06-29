package com.baidu.hd.detect;

public interface FilterCallback 
{
	enum DetectPromptReturn
	{
		eFalse, /** 取消 */
		eTrue, /** 确定 */
		eNoPrompt,		 /** 不弹窗 */
		eNoNetAvailable,		 /** 网络不可用 */
	}
	
	void onDetectPromptReturn(DetectPromptReturn canUse);
}
