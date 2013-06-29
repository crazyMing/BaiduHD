package com.baidu.browser.visitesite;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.baidu.browser.db.Suggestion;
import com.baidu.hd.ui.SuggestionAdapter.SuggestionType;
import com.baidu.hd.util.Json;
import com.baidu.hd.util.StringUtil;
import com.baidu.hd.R;


public class WebSuggestListParser 
{

	private boolean error = false;

	public boolean isError() 
	{
		return error;
	}

	public static List<Suggestion> Parse(Context context, String value) {
		
		List<Suggestion> suggestList = new ArrayList<Suggestion>();

		if (StringUtil.isEmpty(value)) {
			return suggestList;
		}

		try {
			JSONObject o = new JSONObject(Json.filterJsonp(value));
			JSONArray array = o.optJSONArray("s");
			for (int i = 0; i < array.length(); ++i) {
				Suggestion hs = new Suggestion();
				hs.title = array.optString(i);
				hs.url = array.optString(i);
				hs.type = SuggestionType.SUGGESTION;

				suggestList.add(hs);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return suggestList;
	}
 
}
