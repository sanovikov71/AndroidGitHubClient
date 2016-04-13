package com.gmail.sanovikov71.githubclient.ui.detail;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.gmail.sanovikov71.githubclient.R;
import com.gmail.sanovikov71.githubclient.data.DataService;
import com.gmail.sanovikov71.githubclient.model.Repo;
import com.gmail.sanovikov71.githubclient.storage.DBConstants;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.RepoEntry;
import com.gmail.sanovikov71.githubclient.ui.UiElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
        , UiElement {

    private String mUserId;
    private String mUserName;

    private ReposListAdapter mReposListAdapter;
    private static final String TAG = "DetailActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mUserId = "1"; // TODO: get it from getIntent() extra
        mUserName = "mojombo"; // TODO: get from the provider via the loader using id

        RecyclerView userReposList = (RecyclerView) findViewById(R.id.detail_repos_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        userReposList.setLayoutManager(layoutManager);

        mReposListAdapter = new ReposListAdapter(this);
        userReposList.setAdapter(mReposListAdapter);

        updateReposData();

        getSupportLoaderManager().initLoader(500, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = RepoEntry.COLUMN_ID + " ASC";
        Uri uri = RepoEntry.CONTENT_URI;
        return new CursorLoader(this, uri, DBConstants.REPO_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(TAG, "onLoadFinished");
        List<Repo> repos = new ArrayList<>();
        Log.i(TAG, "data size: " + data.getColumnCount());
        Log.i(TAG, "data columns: " + Arrays.asList(data.getColumnNames()));
        if (data != null && data.moveToFirst()) {
            do {
                int id = data.getInt(data.getColumnIndex(RepoEntry.COLUMN_ID));
                String name =
                        data.getString(data.getColumnIndex(RepoEntry.COLUMN_NAME));
                int size =
                        data.getInt(data.getColumnIndex(RepoEntry.COLUMN_SIZE));
                Log.i(TAG, "repo name: " + name);
                Log.i(TAG, "repo size: " + size);
                Repo repo = new Repo(id, name, size);
                repos.add(repo);
            } while (data.moveToNext());
        }
        if (mReposListAdapter != null) {
            mReposListAdapter.updateDataset(repos);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    // TODO: Дублирование кода шо прям в угол забиться и плакать хочется
    private DataService mDataService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DataService.LocalBinder binder = (DataService.LocalBinder) service;
            mDataService = binder.getService();
            updateReposData();
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

    private void updateReposData() {
        if (null != mDataService) {
            mDataService.fetchRepos(this, mUserName);
        }
    }

    @Override
    public void showProgressDialog() {

    }

    @Override
    public void hideProgressDialog() {

    }

    @Override
    public void showError(int stringId) {

    }
}