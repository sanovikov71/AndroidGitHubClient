package com.gmail.sanovikov71.githubclient.ui.drawer.search;

public interface SearchStateListener {

    void onSearchResultShown();

    void onNoSearchResults();

    void onSearch(String query);
}
