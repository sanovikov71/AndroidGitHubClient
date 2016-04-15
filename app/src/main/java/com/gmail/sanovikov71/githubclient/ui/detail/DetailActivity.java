
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
import android.widget.TextView;

import com.gmail.sanovikov71.githubclient.R;
import com.gmail.sanovikov71.githubclient.data.DataService;
import com.gmail.sanovikov71.githubclient.model.Repo;
import com.gmail.sanovikov71.githubclient.storage.DBConstants;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.RepoEntry;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;
import com.gmail.sanovikov71.githubclient.ui.UiElement;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
        , UiElement {

    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    private static final int USER_LOADER_ID = 1;
    private static final int REPOS_LOADER_ID = 2;

    private int mUserId;
    private String mUserName;

    private TextView mUserLogin;
    private ReposListAdapter mReposListAdapter;
    private static final String TAG = "DetailActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mUserId = getIntent().getIntExtra(EXTRA_USER_ID, 0);
        Log.i(TAG, "mUserId: " + mUserId);

        mUserLogin = (TextView) findViewById(R.id.detail_user_login);

        RecyclerView userReposList = (RecyclerView) findViewById(R.id.detail_repos_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        userReposList.setLayoutManager(layoutManager);

        mReposListAdapter = new ReposListAdapter(this);
        userReposList.setAdapter(mReposListAdapter);

        getSupportLoaderManager().initLoader(USER_LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(REPOS_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder;
        Uri uri;
        switch (id) {
            case USER_LOADER_ID:
                sortOrder = UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_GITHUB_ID + " ASC";
                uri = UserEntry.buildUserUri(mUserId);
                return new CursorLoader(this, uri, DBConstants.USER_COLUMNS, null, null, sortOrder);
            case REPOS_LOADER_ID:
                sortOrder = RepoEntry.TABLE_NAME + "." + RepoEntry.COLUMN_GITHUB_ID + " ASC";
                uri = UserEntry.buildUserRepoUri();
                String selection = RepoEntry.COLUMN_OWNER_ID + " = " + mUserId;
                return new CursorLoader(this, uri, DBConstants.USER_DETAIL_COLUMNS, selection, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(TAG, "onLoadFinished");

        switch (loader.getId()) {
            case USER_LOADER_ID:
                Log.i(TAG, "data.getCount(): " + data.getCount());
                data.moveToFirst();
                mUserName = data.getString(data.getColumnIndex(UserEntry.COLUMN_LOGIN));
                mUserLogin.setText(mUserName);
                updateReposData();
                break;
            case REPOS_LOADER_ID:
                List<Repo> repos = new ArrayList<>();
                if (data != null && data.moveToFirst()) {
                    do {
                        int ownerId = data.getInt(data.getColumnIndex(RepoEntry.COLUMN_OWNER_ID));
                        int repoId = data.getInt(data.getColumnIndex(RepoEntry.COLUMN_GITHUB_ID));
                        String name =
                                data.getString(data.getColumnIndex(RepoEntry.COLUMN_NAME));
                        int size =
                                data.getInt(data.getColumnIndex(RepoEntry.COLUMN_SIZE));
                        Repo repo = new Repo(repoId, name, size);
                        repos.add(repo);
                    } while (data.moveToNext());
                }
                if (mReposListAdapter != null) {
                    mReposListAdapter.updateDataset(repos);
                }
                break;
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
        if (null != mDataService && null != mUserName) {
            mDataService.fetchRepos(this, mUserName);
        }
    }

    @Override
    public void showProgressDialog() {

    }

    @Override
    public void hideProgressDialog() {
        Log.i(TAG, "hasta luego");
//        getSupportLoaderManager().restartLoader(REPOS_LOADER_ID, null, this);
        getSupportLoaderManager().getLoader(REPOS_LOADER_ID).forceLoad();
    }

    @Override
    public void showError(int stringId) {

    }
}
