
package com.gmail.sanovikov71.githubclient.ui.list;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.gmail.sanovikov71.githubclient.R;
import com.gmail.sanovikov71.githubclient.data.DataService;
import com.gmail.sanovikov71.githubclient.model.User;
import com.gmail.sanovikov71.githubclient.storage.DBConstants;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;
import com.gmail.sanovikov71.githubclient.ui.BoundActivity;
import com.gmail.sanovikov71.githubclient.ui.detail.DetailActivity;
import com.gmail.sanovikov71.githubclient.ui.drawer.recent.RecentsFragment;
import com.gmail.sanovikov71.githubclient.ui.drawer.search.SearchFragment;
import com.gmail.sanovikov71.githubclient.ui.drawer.search.SearchViewStateListener;
import com.gmail.sanovikov71.githubclient.ui.drawer.search.ServerSearchListener;
import com.gmail.sanovikov71.githubclient.ui.interfaces.OnUserListClickListener;
import com.gmail.sanovikov71.githubclient.ui.interfaces.ProgressBarUiElement;
import com.gmail.sanovikov71.githubclient.ui.interfaces.ScrollingUi;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BoundActivity
        implements ProgressBarUiElement,
        SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor>,
        ScrollingUi, OnUserListClickListener, SearchViewStateListener, ServerSearchListener {

    private static final String TAG_RECENTS_FRAGMENT = "TAG_RECENTS_FRAGMENT";

    private FrameLayout mRecentContainer;
    private GithubUserAdapter mGithubUserAdapter;
    private SwipeRefreshLayout mRefreshView;
    private int mNumberOfLoadedEntries = 0;

    public static final int MAIN_ACTIVITY_LOADER_ID = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initServiceConnection();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        RecyclerView githubUsersList = (RecyclerView) findViewById(R.id.recycler);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        githubUsersList.setLayoutManager(layoutManager);
        mGithubUserAdapter = new GithubUserAdapter(this);
        githubUsersList.setAdapter(mGithubUserAdapter);
        githubUsersList.addOnScrollListener(new EndlessScrollListener(this, layoutManager));

        mRefreshView = (SwipeRefreshLayout) findViewById(R.id.list_refresh);
        mRefreshView.setOnRefreshListener(this);

        mRecentContainer = (FrameLayout) findViewById(R.id.recents_fragment_container);

        getSupportLoaderManager().initLoader(MAIN_ACTIVITY_LOADER_ID, null, this);

        showRecentsFragment();
    }

    private void initServiceConnection() {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DataService.LocalBinder binder = (DataService.LocalBinder) service;
                mDataService = binder.getService();
                mDataService.fetchUsers(MainActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mDataService = null;
            }
        };
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_GITHUB_ID + " ASC";
        Uri cardUri = UserEntry.CONTENT_URI;
        return new CursorLoader(this, cardUri, DBConstants.USER_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<User> users = new ArrayList<>();
        if (data != null && data.moveToFirst()) {
            do {
                int id =
                        data.getInt(data.getColumnIndex(UserEntry.COLUMN_GITHUB_ID));
                String login =
                        data.getString(data.getColumnIndex(UserEntry.COLUMN_LOGIN));
                String avatarUrl = data.getString(
                        data.getColumnIndex(UserEntry.COLUMN_AVATAR_URL));
                User user = new User(id, login, avatarUrl);
                users.add(user);
            } while (data.moveToNext());
        }
        mNumberOfLoadedEntries = users.size();
        mGithubUserAdapter.updateDataset(users);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getSupportLoaderManager().getLoader(MAIN_ACTIVITY_LOADER_ID).forceLoad();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void showProgressDialog() {
        mRefreshView.setRefreshing(true);
    }

    @Override
    public void hideProgressDialog() {
        mRefreshView.setRefreshing(false);
    }

    @Override
    public void render() {
        getSupportLoaderManager().getLoader(MAIN_ACTIVITY_LOADER_ID).forceLoad();
    }

    @Override
    public void showError(int stringId) {
        Toast.makeText(this, String.valueOf(stringId), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRefresh() {
        if (null != mDataService) {
            showProgressDialog();
            mDataService.fetchUsers(this);
        }
    }

    @Override
    public void loadMore() {
        mDataService.fetchMoreUsers(this, mNumberOfLoadedEntries);
    }

    @Override
    public void onClickUser(int userId) {
        updateRecentsStorage(userId);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_USER_ID, userId);
        startActivity(intent);
    }

    private void updateRecentsStorage(int newRecentUserId) {

        final String newRecentId = String.valueOf(newRecentUserId);

        SharedPreferences recents =
                getSharedPreferences(RecentsFragment.RECENT_LIST_PREFERENCES_NAME, MODE_PRIVATE);

        // format: 1;7;8;3;12;
        String recentsStr = recents.getString(RecentsFragment.KEY_RECENTS, "");

        List<String> recentList = RecentsFragment.getRecentUsers(recentsStr);

        if (newRecentId.equals(recentList.get(0))) {
            return;
        }

        int indexOf = recentList.indexOf(newRecentId);
        if (-1 != indexOf) {
            recentList.remove(indexOf);
        } else {
            recentList.remove(recentList.size() - 1);
        }
        recentList.add(0, newRecentId);

        // save changes
        StringBuilder newRecentsStrBuilder = new StringBuilder();
        for (int i = 0; i < recentList.size(); i++) {
            newRecentsStrBuilder.append(recentList.get(i)).append(RecentsFragment.RECENT_SEPARATOR);
        }
        recents.edit().putString(RecentsFragment.KEY_RECENTS, newRecentsStrBuilder.toString()).apply();

    }

    @Override
    public void onSearchResultShown() {
        hideRecentsFragment();
    }

    @Override
    public void onNoSearchResults() {
        showRecentsFragment();
    }

    @Override
    public void onSearchQuerySubmitted(String query) {
        mDataService.searchUsers(this, query);
    }

    private void showRecentsFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.recents_fragment_container, new RecentsFragment(), TAG_RECENTS_FRAGMENT)
                .commit();
        mRecentContainer.setVisibility(View.VISIBLE);
    }

    private void hideRecentsFragment() {
        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment recentsFragment = supportFragmentManager.findFragmentByTag(TAG_RECENTS_FRAGMENT);
        supportFragmentManager.beginTransaction().remove(recentsFragment).commit();
        mRecentContainer.setVisibility(View.GONE);
    }

    @Override
    public void onSearchResult(List<User> searchResult) {
        ((SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment))
                .searchResultsLoaded(searchResult);
    }

    @Override
    public void onSearchError(int stringId) {
        Toast.makeText(this, "Server error: " + stringId, Toast.LENGTH_LONG).show();
    }

}
