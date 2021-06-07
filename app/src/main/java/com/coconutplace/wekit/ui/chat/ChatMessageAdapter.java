package com.coconutplace.wekit.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coconutplace.wekit.R;
import com.coconutplace.wekit.ui.chat.viewholder.AdminMessageHolder;
import com.coconutplace.wekit.ui.chat.viewholder.MyImgMessageViewHolder;
import com.coconutplace.wekit.ui.chat.viewholder.MyMessageViewHolder;
import com.coconutplace.wekit.ui.chat.viewholder.OthersImgMessageViewHolder;
import com.coconutplace.wekit.ui.chat.viewholder.OthersMessageViewHolder;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.SendBird;
import com.sendbird.android.UserMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER_MESSAGE_ME = 10;
    private static final int VIEW_TYPE_USER_MESSAGE_OTHER = 11;
    private static final int VIEW_TYPE_FILE_MESSAGE_IMAGE_ME = 22;
    private static final int VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER = 23;
    private static final int VIEW_TYPE_ADMIN_MESSAGE = 30;

    OnItemClickListener mItemClickListener;
    private ArrayList<BaseMessage> mMessageList;

    public ChatMessageAdapter(){
        mMessageList = new ArrayList<>();
    }

    public void setItemClickListener(OnItemClickListener listener){
        mItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onUserMessageItemClick(UserMessage message);
        void onFileMessageItemClick(FileMessage message);
        void onBackgroundClick();
        void onProfileClick(String profileUrl);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        switch (viewType){
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_other,parent,false);
                return new OthersMessageViewHolder(view);
            case VIEW_TYPE_USER_MESSAGE_ME:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_me,parent,false);
                return new MyMessageViewHolder(view);
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_image_me,parent,false);
                return new MyImgMessageViewHolder(view);
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_image_other,parent,false);
                return new OthersImgMessageViewHolder(view);
            case VIEW_TYPE_ADMIN_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_admin, parent, false);
                return new AdminMessageHolder(view);
            default:
                return null;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseMessage message = mMessageList.get(position);

        boolean isNewDay = false;
        if(position<mMessageList.size()-1){
            BaseMessage prevMessage = mMessageList.get(position+1);
            if(!hasSameDate(message.getCreatedAt(),prevMessage.getCreatedAt())){
                isNewDay = true;
            }
        }
        else if(position==mMessageList.size()-1){
            isNewDay = true;
        }

        int viewType = holder.getItemViewType();
        switch (viewType){
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                ((OthersMessageViewHolder)holder).onBind(message,mItemClickListener,isNewDay);
                break;
            case VIEW_TYPE_USER_MESSAGE_ME:
                ((MyMessageViewHolder)holder).onBind(message,mItemClickListener,isNewDay);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                ((MyImgMessageViewHolder)holder).onBind((FileMessage)message,mItemClickListener,isNewDay);
                break;
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                ((OthersImgMessageViewHolder)holder).onBind((FileMessage)message,mItemClickListener,isNewDay);
                break;
            case VIEW_TYPE_ADMIN_MESSAGE:
                ((AdminMessageHolder)holder).onBind((AdminMessage)message,mItemClickListener,isNewDay);

        }
    }

    @Override
    public int getItemViewType(int position){
        BaseMessage message = mMessageList.get(position);
        if (message instanceof UserMessage) {
            UserMessage userMessage = (UserMessage) message;
            if (userMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                return VIEW_TYPE_USER_MESSAGE_ME;
            } else{
                return VIEW_TYPE_USER_MESSAGE_OTHER;
            }
        }
        else if(message instanceof FileMessage){
            FileMessage fileMessage = (FileMessage) message;
            if (fileMessage.getType().toLowerCase().startsWith("image")) {
                if (fileMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    return VIEW_TYPE_FILE_MESSAGE_IMAGE_ME;
                } else {
                    return VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER;
                }
            }
        }
        else if (message instanceof AdminMessage) {
            return VIEW_TYPE_ADMIN_MESSAGE;
        }
        /*
        else if (message instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) message;
            if (fileMessage.getType().toLowerCase().startsWith("image")) {
                // If the sender is current user
                if (fileMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    return VIEW_TYPE_FILE_MESSAGE_IMAGE_ME;
                } else {
                    return VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER;
                }
            } else if (fileMessage.getType().toLowerCase().startsWith("video")) {
                if (fileMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    return VIEW_TYPE_FILE_MESSAGE_VIDEO_ME;
                } else {
                    return VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER;
                }
            } else {
                if (fileMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    return VIEW_TYPE_FILE_MESSAGE_ME;
                } else {
                    return VIEW_TYPE_FILE_MESSAGE_OTHER;
                }
            }
        }*/

        return -1;
    }


    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public void addLast(BaseMessage msg){
        mMessageList.add(0,msg);
    }

    public void addFirst(List<BaseMessage> msgList){
        mMessageList.addAll(msgList);
    }

    public void clearList(){
        mMessageList.clear();
    }

    public boolean hasSameDate(long millisFirst, long millisSecond) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        return dateFormat.format(millisFirst).equals(dateFormat.format(millisSecond));
    }
}
