package com.gmail.sanovikov71.githubclient.ui;

import android.content.ComponentName;
import android.content.Context;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.gmail.sanovikov71.githubclient.R;
import com.gmail.sanovikov71.githubclient.data.DataService;
import com.gmail.sanovikov71.githubclient.model.User;
import com.gmail.sanovikov71.githubclient.storage.DBConstants;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;
import com.gmail.sanovikov71.githubclient.ui.detail.DetailActivity;
import com.gmail.sanovikov71.githubclient.ui.drawer.recent.RecentsFragment;
import com.gmail.sanovikov71.githubclient.ui.drawer.search.SearchStateListener;
import com.gmail.sanovikov71.githubclient.ui.drawer.search.ServerSearchListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements UiElement,
        SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor>,
        ScrollingUi, OnUserListClickListener, SearchStateListener, ServerSearchListener {

    public static final String TAG = "Novikov";
    private static final String TAG_RECENTS_FRAGMENT = "TAG_RECENTS_FRAGMENT";

    private FrameLayout mRecentContainer;
    private GithubUserAdapter mGithubUserAdapter;
    private SwipeRefreshLayout mRefreshView;
    private int mNumberOfLoadedEntries = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView githubUsersList = (RecyclerView) findViewById(R.id.recycler);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        githubUsersList.setLayoutManager(layoutManager);
        mGithubUserAdapter = new GithubUserAdapter(this);
        githubUsersList.setAdapter(mGithubUserAdapter);
        githubUsersList.addOnScrollListener(new EndlessScrollListener(this, layoutManager));

        mRefreshView = (SwipeRefreshLayout) findViewById(R.id.list_refresh);
        mRefreshView.setOnRefreshListener(this);

        mRecentContainer = (FrameLayout) findViewById(R.id.recents_fragment_container);

        // TODO: change loader id
        getSupportLoaderManager().initLoader(200, null, this);

        showRecentsFragment();
    }

    private DataService mDataService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DataService.LocalBinder binder = (DataService.LocalBinder) service;
            mDataService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDataService = null;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, DataService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mDataService) {
            unbindService(mConnection);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = UserEntry.COLUMN_ID + " ASC";
        Uri cardUri = UserEntry.CONTENT_URI;
        return new CursorLoader(this, cardUri, DBConstants.USER_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<User> users = new ArrayList<>();
        if (data != null && data.moveToFirst()) {
            do {
                int id =
                        data.getInt(data.getColumnIndex(UserEntry.COLUMN_ID));
                String login =
                        data.getString(data.getColumnIndex(UserEntry.COLUMN_LOGIN));
                String avatarUrl = data.getString(
                        data.getColumnIndex(UserEntry.COLUMN_AVATAR_URL));
                User user = new User(id, login, avatarUrl);
                users.add(user);
            } while (data.moveToNext());
        }
        mNumberOfLoadedEntries = users.size();
        Log.i(TAG, "mNumberOfLoadedEntries: " + mNumberOfLoadedEntries);
        mGithubUserAdapter.updateDataset(users);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
    public void showError(int stringId) {
        Toast.makeText(this, getString(stringId), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRefresh() {
        Log.i(TAG, "onRefresh");
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
//        intent.putExtra(CardDetailActivity.EXTRA_CARD_ID, id);
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

    // TODO: transaction is actually unnecessary here
    private void showRecentsFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.recents_fragment_container, new RecentsFragment(), TAG_RECENTS_FRAGMENT)
                .commit();
        mRecentContainer.setVisibility(View.VISIBLE);
    }

    // TODO: transaction is actually unnecessary here
    private void hideRecentsFragment() {
        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment recentsFragment = supportFragmentManager.findFragmentByTag(TAG_RECENTS_FRAGMENT);
        supportFragmentManager.beginTransaction().remove(recentsFragment).commit();
        mRecentContainer.setVisibility(View.GONE);
    }

    @Override
    public void onSearch(String query) {
        mDataService.fetchUser(this, query);
    }

    @Override
    public void onSearchResult() {

    }

    @Override
    public void onSearchError(int stringId) {
        Toast.makeText(this, "Server error: " + stringId, Toast.LENGTH_LONG).show();
    }

}