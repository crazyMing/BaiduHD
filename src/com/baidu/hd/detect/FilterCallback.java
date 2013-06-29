package com.baidu.hd.detect;

public interface FilterCallback 
{
	enum DetectPromptReturn
	{
		eFalse, /** ȡ�� */
		eTrue, /** ȷ�� */
		eNoPrompt,		 /** ������ */
		eNoNetAvailable,		 /** ���粻���� */
	}
	
	void onDetectPromptReturn(DetectPromptReturn canUse);
}
