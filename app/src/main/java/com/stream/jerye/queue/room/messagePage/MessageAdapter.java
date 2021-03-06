package com.stream.jerye.queue.room.messagePage;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stream.jerye.queue.PreferenceUtility;
import com.stream.jerye.queue.R;

import java.util.List;

/**
 * Created by jerye on 6/12/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context mContext;
    private List<Message> mMessageList;
    private LinearLayout.LayoutParams paramsMe, paramsSystems, paramsYou;
    private String meName;

    public MessageAdapter(Context context, List<Message> list) {
        mContext = context;
        mMessageList = list;
        PreferenceUtility.initialize(mContext);
        meName = PreferenceUtility.getPreference(PreferenceUtility.PROFILE_NAME);

        paramsYou = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsMe = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsMe.gravity = Gravity.END;
        paramsSystems = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsSystems.gravity = Gravity.CENTER;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        String name = mMessageList.get(position).getName();
        holder.messageContent.setText(mMessageList.get(position).getText());
        holder.messageName.setText(name);

        if (meName.equals(name)) {
            holder.messageName.setVisibility(View.VISIBLE);
            holder.messageName.setLayoutParams(paramsMe);
            holder.messageContent.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            holder.messageContent.setLayoutParams(paramsMe);
            holder.messageContent.setBackground(ContextCompat.getDrawable(mContext, R.drawable.message_bubble_me));
        } else if (name.equals("SYSTEM ANNOUNCEMENT")) {
            holder.messageName.setVisibility(View.GONE);
            holder.messageContent.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            holder.messageContent.setLayoutParams(paramsSystems);
            holder.messageContent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.transparent));
        } else {
            holder.messageName.setLayoutParams(paramsYou);
            holder.messageName.setVisibility(View.VISIBLE);
            holder.messageContent.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            holder.messageContent.setLayoutParams(paramsYou);
            holder.messageContent.setBackground(ContextCompat.getDrawable(mContext, R.drawable.message_bubble_you));
        }
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.message_layout, parent, false);

        return new MessageViewHolder(view);
    }

    @Override
    public int getItemCount() {
        if (mMessageList == null) {
            return 0;
        } else {
            return mMessageList.size();
        }
    }

    public void add(Message message) {
        mMessageList.add(message);
        notifyDataSetChanged();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageName;
        TextView messageContent;

        public MessageViewHolder(View view) {
            super(view);

            messageName = (TextView) view.findViewById(R.id.message_name);
            messageContent = (TextView) view.findViewById(R.id.message_content);
        }

    }
}
