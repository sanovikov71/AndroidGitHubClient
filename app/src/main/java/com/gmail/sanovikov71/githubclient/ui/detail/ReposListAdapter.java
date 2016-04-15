package com.gmail.sanovikov71.githubclient.ui.detail;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.sanovikov71.githubclient.R;
import com.gmail.sanovikov71.githubclient.model.Repo;

import java.util.ArrayList;
import java.util.List;

public class ReposListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Repo> mReposData = new ArrayList<>();

    public void updateDataset(List<Repo> data) {
        mReposData = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_repo_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder typedHolder = (ViewHolder) holder;

        Repo repo = mReposData.get(position);
        typedHolder.mRepoName.setText(repo.getFullName());
        typedHolder.mRepoSize.setText(String.valueOf(repo.getSize()));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mReposData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mRepoName;
        TextView mRepoSize;

        public ViewHolder(View view) {
            super(view);
            mRepoName = (TextView) view.findViewById(R.id.item_repo_name);
            mRepoSize = (TextView) view.findViewById(R.id.item_repo_size);
        }
    }

}
