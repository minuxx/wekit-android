package com.coconutplace.wekit.ui.channel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coconutplace.wekit.R;
import com.coconutplace.wekit.data.entities.ChatRoom;

import java.util.ArrayList;
import java.util.List;

public class ChannelRecyclerAdapter extends RecyclerView.Adapter<ChannelRecyclerAdapter.ItemViewHolder>{

    private List<ChatRoom> mChannelList;
    private OnItemClickListener mItemClickListener;

    interface OnItemClickListener {
        void onItemClick(ChatRoom channel);
    }

    public ChannelRecyclerAdapter(){
        mChannelList = new ArrayList<>();
    }

    public void clear(){
        mChannelList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChannelRecyclerAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel_list,parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelRecyclerAdapter.ItemViewHolder holder, int position) {
        holder.onBind(mChannelList.get(position),mItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mChannelList.size();
    }

    void setGroupChannelList(List<ChatRoom> channelList){
        mChannelList = channelList;
        notifyDataSetChanged();
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView roomName, roomExplain, memberCount, roomTerm;

        ItemViewHolder(View itemView){
            super(itemView);

            roomName = itemView.findViewById(R.id.channel_name_text);
            roomExplain = itemView.findViewById(R.id.channel_explain_text);
            memberCount = itemView.findViewById(R.id.channel_member_count_text);
            roomTerm = itemView.findViewById(R.id.channel_duration_text);
        }

        void onBind(ChatRoom mChannel, OnItemClickListener clickListener){
            String name = mChannel.getRoomName();
            String explain = mChannel.getChatDescription();
            String term="";
            if(mChannel.getRoomTerm()==""){}
            else if(mChannel.getRoomTerm().equals("2주방")){
                term = "2주 도전방";
            }
            else if(mChannel.getRoomTerm().equals("한달방")){
                term = "한달 도전방";
            }

            roomName.setText(name);
            roomExplain.setText(explain);
            memberCount.setText(String.format("%s/%s", mChannel.getCurrentNum().toString(), mChannel.getMaxLimit().toString()));
            roomTerm.setText(term);

            if (clickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onItemClick(mChannel);
                    }
                });
            }
        }
    }
}
