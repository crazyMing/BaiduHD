package com.baidu.hd.ui;

import com.baidu.browser.db.Suggestion;

/**
 * Listener interface for clicks on suggestions.
 */
public interface SuggestionClickListener {
    /**
     * Called when a suggestion is clicked.
     *
     * @param suggestion  clicked suggestion.
     */
    void onSuggestionClicked(Suggestion suggestion);


    /**
     * Called when the "query refine" button of a suggestion is clicked.
     *
     * @param suggestion suggestion.
     */
    void onSuggestionQueryRefineClicked(Suggestion suggestion);    
}
