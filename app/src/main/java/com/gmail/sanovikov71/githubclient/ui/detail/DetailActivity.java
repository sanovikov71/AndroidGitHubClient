
package com.gmail.sanovikov71.githubclient.ui.detail;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gmail.sanovikov71.githubclient.R;
import com.gmail.sanovikov71.githubclient.data.DataService;
import com.gmail.sanovikov71.githubclient.model.Repo;
import com.gmail.sanovikov71.githubclient.storage.DBConstants;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.RepoEntry;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;
import com.gmail.sanovikov71.githubclient.ui.BoundActivity;
import com.gmail.sanovikov71.githubclient.ui.interfaces.UiElement;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends BoundActivity implements LoaderManager.LoaderCallbacks<Cursor>
        , UiElement {

    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    private static final int USER_LOADER_ID = 1;
    private static final int REPOS_LOADER_ID = 2;

    private int mUserId;
    private String mUserName;

    private ImageView mAvatar;
    private TextView mUserLogin;
    private ReposListAdapter mReposListAdapter;
    private static final String TAG = "DetailActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mUserId = getIntent().getIntExtra(EXTRA_USER_ID, 0);
        Log.i(TAG, "mUserId: " + mUserId);

        initServiceConnection();

        mAvatar = (ImageView) findViewById(R.id.detail_user_avatar);
        mUserLogin = (TextView) findViewById(R.id.detail_user_login);

        RecyclerView userReposList = (RecyclerView) findViewById(R.id.detail_repos_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        userReposList.setLayoutManager(layoutManager);

        mReposListAdapter = new ReposListAdapter(this);
        userReposList.setAdapter(mReposListAdapter);

        getSupportLoaderManager().initLoader(USER_LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(REPOS_LOADER_ID, null, this);
    }

    private void initServiceConnection() {
        mConnection = new ServiceConnection() {
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
                uri = RepoEntry.CONTENT_URI;
                String selection = RepoEntry.COLUMN_OWNER_ID + " = " + mUserId;
                return new CursorLoader(this, uri, DBConstants.REPO_COLUMNS, selection, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case USER_LOADER_ID:
                data.moveToFirst();
                mUserName = data.getString(data.getColumnIndex(UserEntry.COLUMN_LOGIN));
                mUserLogin.setText(mUserName);
                Glide.with(this)
                        .load(data.getString(data.getColumnIndex(UserEntry.COLUMN_AVATAR_URL)))
                        .into(mAvatar);
                updateReposData();
                break;
            case REPOS_LOADER_ID:
                List<Repo> repos = new ArrayList<>();
                if (data != null && data.moveToFirst()) {
                    do {
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

    private void updateReposData() {
        if (null != mDataService && null != mUserName) {
            mDataService.fetchRepos(this, mUserName);
        }
    }

    @Override
    public void render() {
        getSupportLoaderManager().getLoader(REPOS_LOADER_ID).forceLoad();
    }

    @Override
    public void showError(int stringId) {
        Toast.makeText(this, String.valueOf(stringId), Toast.LENGTH_LONG).show();
    }

}
