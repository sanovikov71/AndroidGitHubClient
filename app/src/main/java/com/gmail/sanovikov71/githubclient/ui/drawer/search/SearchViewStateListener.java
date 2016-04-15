package com.gmail.sanovikov71.githubclient.ui.drawer.search;

public interface SearchViewStateListener {

    void onSearchResultShown();

    void onNoSearchResults();

    void onSearch(String query);
}
