
package com.gmail.sanovikov71.githubclient.ui.drawer.recent;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.sanovikov71.githubclient.R;
import com.gmail.sanovikov71.githubclient.model.User;
import com.gmail.sanovikov71.githubclient.ui.interfaces.OnUserListClickListener;

import java.util.ArrayList;
import java.util.List;

class RecentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private List<User> mUsers = new ArrayList<>();

    public RecentListAdapter(Context context) {
        mContext = context;
    }

    public void updateDataset(List<User> data) {
        mUsers = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent, parent, false);
        return new ViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder typedHolder = (ViewHolder) holder;

        User user = mUsers.get(position);
        typedHolder.mUserId = user.getId();
        typedHolder.mName.setText(user.getLogin());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mName;
        int mUserId;

        public ViewHolder(Context context, View view) {
            super(view);
            mName = (TextView) view.findViewById(R.id.item_drawer_name);

            final OnUserListClickListener listener;
            try {
                listener = (OnUserListClickListener) context;
            } catch (ClassCastException cce) {
                throw new ClassCastException(context.getClass().getSimpleName()
                        + " must implement OnItemClickListener interface");
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickUser(mUserId);
                }
            });

        }
    }

}
