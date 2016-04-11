package com.gmail.sanovikov71.githubclient.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
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
import android.view.MenuItem;
import android.widget.Toast;

import com.gmail.sanovikov71.githubclient.R;
import com.gmail.sanovikov71.githubclient.data.DataService;
import com.gmail.sanovikov71.githubclient.model.User;
import com.gmail.sanovikov71.githubclient.storage.DBConstants;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements UiElement,
        NavigationView.OnNavigationItemSelectedListener,
        SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor>,
ScrollingUi{

    public static final String TAG = "Novikov";

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mRefreshView = (SwipeRefreshLayout) findViewById(R.id.list_refresh);
        mRefreshView.setOnRefreshListener(this);

        // TODO: change loader id
        getSupportLoaderManager().initLoader(200, null, this);
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
                String login =
                        data.getString(data.getColumnIndex(UserEntry.COLUMN_LOGIN));
                String avatarUrl = data.getString(
                        data.getColumnIndex(UserEntry.COLUMN_AVATAR_URL));
                User user = new User(login, avatarUrl);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
}