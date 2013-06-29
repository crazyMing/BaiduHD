package com.baidu.browser.core;

import android.content.Context;
import android.os.Bundle;

/**
 * @ClassName: IElement 
 * @Description:Element Interface
 * @author LEIKANG 
 * @date 2012-12-11 обнГ5:17:46
 */
public interface IElement {

	/**
	 * onInit
	 * 
	 * @param aBundle
	 *            Bundle
	 */
	void onInit(Bundle aBundle);

	/**
	 * onSave
	 * 
	 * @param aContext
	 *            Context
	 */
	void onSave(Context aContext);

	/**
	 * onLoad
	 * 
	 * @param aContext
	 *            Context
	 */
	void onLoad(Context aContext);

	/**
	 * onDestroy
	 */
	void onDestroy();
}
