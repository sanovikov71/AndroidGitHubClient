
package com.gmail.sanovikov71.githubclient.ui.drawer.search;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.sanovikov71.githubclient.R;
import com.gmail.sanovikov71.githubclient.model.User;

import java.util.List;

public class SearchFragment extends Fragment {

    private SearchResultAdapter mSearchResultsAdapter;
    private SearchViewStateListener mSearchListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mSearchListener = (SearchViewStateListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException("MainActivity must implement SearchStateListener interface");
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        RecyclerView searchResults = (RecyclerView) view.findViewById(R.id.search_results);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        searchResults.setLayoutManager(layoutManager);

        mSearchResultsAdapter = new SearchResultAdapter(getActivity());
        searchResults.setAdapter(mSearchResultsAdapter);

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
                mSearchResultsAdapter.resetDataset();
                return false;
            }
        });
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchListener.onSearchQuerySubmitted(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return view;
    }

    public void searchResultsLoaded(List<User> searchResult) {
        mSearchResultsAdapter.updateDataset(searchResult);
    }

}
