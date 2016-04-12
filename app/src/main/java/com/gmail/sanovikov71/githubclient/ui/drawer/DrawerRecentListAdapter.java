
package com.gmail.sanovikov71.githubclient.ui.drawer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.sanovikov71.githubclient.R;

import java.util.ArrayList;
import java.util.List;

class DrawerRecentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private List<String> mLogins = new ArrayList<>();

    public DrawerRecentListAdapter(Context context) {
        mContext = context;
    }

    public void updateDataset(List<String> data) {
        mLogins = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drawer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder typedHolder = (ViewHolder) holder;

        typedHolder.mName.setText(mLogins.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mLogins.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mName;

        public ViewHolder(View view) {
            super(view);
            mName = (TextView) view.findViewById(R.id.item_drawer_name);
        }
    }

}
