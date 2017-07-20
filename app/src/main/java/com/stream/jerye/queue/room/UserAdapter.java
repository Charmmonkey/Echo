package com.stream.jerye.queue.room;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stream.jerye.queue.R;
import com.stream.jerye.queue.lobby.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jerye on 7/14/2017.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> mUserList = new ArrayList<>();
    private Context mContext;
    private BitmapDrawable userPictureDrawable;


    public UserAdapter(Context context){
        mContext = context;


    }

    public class UserViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.user_profile_picture)
        ImageView userProfilePicture;
        @BindView(R.id.user_name)
        TextView userName;

        public UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_layout, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, int position) {
        String picturePath;
        if(mUserList.get(position).getPicture() == null || mUserList.get(position).getPicture().equals("")){
            holder.userProfilePicture.setImageDrawable(mContext.getDrawable(R.drawable.default_profile_icon));
        }else{
            Picasso.with(mContext).load(mUserList.get(position).getPicture()).into(holder.userProfilePicture);

        }

        Log.d("Profile", mUserList.get(position).getPicture());

        holder.userName.setText(mUserList.get(position).getName());

    }

    public void addUser(User user){
        mUserList.add(user);
        notifyDataSetChanged();
    }
}
