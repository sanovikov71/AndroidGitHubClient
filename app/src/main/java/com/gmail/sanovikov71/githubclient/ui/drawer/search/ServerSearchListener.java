package com.gmail.sanovikov71.githubclient.ui.drawer.search;

import com.gmail.sanovikov71.githubclient.model.User;

import java.util.List;

public interface ServerSearchListener {

    void onSearchResult(List<User> searhResult);
    void onSearchError(int code);

}
