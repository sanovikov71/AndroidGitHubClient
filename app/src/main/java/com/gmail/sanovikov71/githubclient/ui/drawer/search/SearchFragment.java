
package com.gmail.sanovikov71.githubclient.ui.drawer.search;

import android.content.Context;
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
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.sanovikov71.githubclient.R;
import com.gmail.sanovikov71.githubclient.storage.DBConstants;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SearchResultAdapter mSearchResultsAdapter;
    private SearchStateListener mSearchListener;
    private String mSearchQuery;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mSearchListener = (SearchStateListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException("MainActivity must implement SearchStateListener interface");
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(400, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        SearchView search = (SearchView) view.findViewById(R.id.search_view);
        search.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchListener.onSearchResultShown();
            }
        });
        search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mSearchListener.onNoSearchResults();
                return false;
            }
        });
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchQuery = query;
                mSearchListener.onSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        RecyclerView searchResults = (RecyclerView) view.findViewById(R.id.search_results);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        searchResults.setLayoutManager(layoutManager);

        mSearchResultsAdapter = new SearchResultAdapter(getActivity());
        searchResults.setAdapter(mSearchResultsAdapter);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_GITHUB_ID + " ASC";
        Uri uri = UserEntry.CONTENT_URI;
        return new CursorLoader(getActivity(), uri, DBConstants.USER_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<String> logins = new ArrayList<>();
        if (data != null && data.moveToFirst()) {
            do {
                String login =
                        data.getString(data.getColumnIndex(UserEntry.COLUMN_LOGIN));
                if (mSearchQuery != null && mSearchQuery.equals(login)) {
                    logins.add(login);
                }
            } while (data.moveToNext());
        }
        if (mSearchResultsAdapter != null) {
            mSearchResultsAdapter.updateDataset(logins);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
