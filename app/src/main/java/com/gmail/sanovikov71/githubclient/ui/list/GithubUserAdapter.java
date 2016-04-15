package com.gmail.sanovikov71.githubclient.ui.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gmail.sanovikov71.githubclient.R;
import com.gmail.sanovikov71.githubclient.model.User;
import com.gmail.sanovikov71.githubclient.ui.interfaces.OnUserListClickListener;

import java.util.ArrayList;
import java.util.List;

class GithubUserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private List<User> mUsersData = new ArrayList<>();

    public GithubUserAdapter(Context context) {
        mContext = context;
    }

    public void updateDataset(List<User> data) {
        mUsersData = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_row, parent, false);
        return new ViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder typedHolder = (ViewHolder) holder;

        User user = mUsersData.get(position);
        typedHolder.mUserId = user.getId();
        typedHolder.mName.setText(user.getLogin());

        Glide.with(mContext)
                .load(user.getAvatarUrl())
                .into(typedHolder.mAvatar);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mUsersData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        int mUserId;
        ImageView mAvatar;
        TextView mName;

        public ViewHolder(Context context, View view) {
            super(view);
            mAvatar = (ImageView) view.findViewById(R.id.item_avatar);
            mName = (TextView) view.findViewById(R.id.item_name);

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
