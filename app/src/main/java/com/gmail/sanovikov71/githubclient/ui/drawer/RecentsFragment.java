
package com.gmail.sanovikov71.githubclient.ui.drawer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.sanovikov71.githubclient.R;
import com.gmail.sanovikov71.githubclient.storage.DBConstants;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecentsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "Novikov";

    private List<String> mRecentIds = new ArrayList<>();
    private DrawerRecentListAdapter mDrawerListAdapter;

    public static final String RECENT_LIST_PREFERENCES_NAME = "recents";
    public static final String KEY_RECENTS = "recents";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(300, null, this);

        SharedPreferences recents = getActivity()
                .getSharedPreferences(RECENT_LIST_PREFERENCES_NAME, Context.MODE_PRIVATE);
        reloadRecents(recents);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_drawer, container, false);

        RecyclerView drawerList = (RecyclerView) view.findViewById(R.id.drawer_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        drawerList.setLayoutManager(layoutManager);

        mDrawerListAdapter = new DrawerRecentListAdapter(getActivity());
        drawerList.setAdapter(mDrawerListAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences recents = getActivity()
                .getSharedPreferences(RECENT_LIST_PREFERENCES_NAME, Context.MODE_PRIVATE);
        recents.registerOnSharedPreferenceChangeListener(mRecentPrefListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences recents = getActivity()
                .getSharedPreferences(RECENT_LIST_PREFERENCES_NAME, Context.MODE_PRIVATE);
        recents.unregisterOnSharedPreferenceChangeListener(mRecentPrefListener);
    }

    SharedPreferences.OnSharedPreferenceChangeListener mRecentPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            reloadRecents(sharedPreferences);
        }
    };

    private void reloadRecents(SharedPreferences recents) {
        mRecentIds = getRecentUsers(recents.getString(KEY_RECENTS, ""));
        getActivity().getSupportLoaderManager().getLoader(300).forceLoad();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = GithubDataContract.UserEntry.COLUMN_ID + " ASC";
        Uri cardUri = GithubDataContract.UserEntry.CONTENT_URI;
        return new CursorLoader(getActivity(), cardUri, DBConstants.USER_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<String> logins = new ArrayList<>();
        for (int i = 0; i < mRecentIds.size(); i++) {
            if (data != null && data.moveToFirst()) {
                do {
                    int id = data.getInt(data.getColumnIndex(UserEntry.COLUMN_ID));
                    // TODO: дичь какая-то
                    if (mRecentIds.get(i).equals(String.valueOf(id))) {
                        String login =
                                data.getString(data.getColumnIndex(UserEntry.COLUMN_LOGIN));
                        logins.add(login);
                    }
                } while (data.moveToNext());
            }
        }
        Log.i(TAG, "updateDataset: " + logins.size());
        mDrawerListAdapter.updateDataset(logins);
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    public static final String RECENT_SEPARATOR = ";";

    public static List<String> getRecentUsers(String prefsEntry) {
        int recentListSize = 5;

        // for first run
        if ("".equals(prefsEntry)) {
            for (int i = 0; i < recentListSize; i++) {
                prefsEntry += (" " + RECENT_SEPARATOR);
            }
        }

        String arr[] = prefsEntry.split(RECENT_SEPARATOR);

        List<String> recentList = new ArrayList<>();
        Collections.addAll(recentList, arr);

        return recentList;
    }

}
