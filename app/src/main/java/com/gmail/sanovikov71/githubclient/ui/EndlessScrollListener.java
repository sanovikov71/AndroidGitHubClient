
package com.gmail.sanovikov71.githubclient.ui;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public class EndlessScrollListener extends RecyclerView.OnScrollListener {

    private final ScrollingUi mUi;
    private boolean loading = true;

    private LinearLayoutManager mLayoutManager;

    public EndlessScrollListener(ScrollingUi ui, LinearLayoutManager layoutManager) {
        mUi = ui;
        mLayoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dy > 0) //check for scroll down
        {
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

            if (loading) {
                if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                    loading = false;
                    Log.i("Novikov", "Last Item Wow!");
                    mUi.loadMore();
                }
            }
        }

    }

}
