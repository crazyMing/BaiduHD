package com.baidu.hd.ui;

import com.baidu.hd.ui.SuggestionAdapter.ClearLayoutClickType;

/**
 * Listener interface for clicks on suggestions.
 */
public interface ClearHistoryClickListener {
    /**
     * Called when a suggestion is clicked.
     *
     * @param suggestion  clicked suggestion.
     */
    void onClearHistoryClicked(ClearLayoutClickType clearLayoutClickType);
  
}
