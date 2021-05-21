package com.coconutplace.wekit.ui.chat.viewholder

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.ui.chat.ChatMessageAdapter
import com.coconutplace.wekit.utils.ChatMessageUtil
import com.coconutplace.wekit.utils.SharedPreferencesManager
import com.sendbird.android.FileMessage
import com.sendbird.android.FileMessage.Thumbnail
import java.util.*

internal class OthersImgMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val dateLayout: LinearLayout = itemView.findViewById(R.id.chat_date_layout)
    private val dateText: TextView = itemView.findViewById(R.id.chat_date_text)
    private val fileThumbnailImage: ImageView = itemView.findViewById(R.id.chat_msg_img)
    private val sendTime: TextView = itemView.findViewById(R.id.chat_time_text)
    private val sender: TextView = itemView.findViewById(R.id.chat_sender_text)
    private val profileImg = itemView.findViewById<ImageView>(R.id.chat_profile_img)

    fun onBind(
        message: FileMessage,
        clickListener: ChatMessageAdapter.OnItemClickListener?,
        isNewDay: Boolean
    ) {
        sendTime.text = ChatMessageUtil.formatTime(message.createdAt)
        sender.text = message.sender.nickname

        if(isNewDay){
            dateLayout.visibility = View.VISIBLE
            dateText.text = ChatMessageUtil.formatDate(message.createdAt)
        }
        else{
            dateLayout.visibility = View.GONE
        }

        val profileUrl:String? = message.sender.profileUrl
        Log.e(SharedPreferencesManager.CHECK_TAG,"profileUrl : $profileUrl")
        if(profileUrl==null||profileUrl==""){
            profileImg.setImageResource(R.drawable.character_sm_basic)
        }
        else{
            //ChatMessageUtil.displayProfileWithPicasso(profileUrl,profileImg)
            ChatMessageUtil.displayProfile(dateLayout.context,profileUrl,profileImg)
        }

        val thumbnails =
            message.thumbnails as ArrayList<Thumbnail>
        if (thumbnails.size > 0) {
            //Log.e(CHECK_TAG,"otherImg thumbnail exist");
            if (message.type.toLowerCase(Locale.ROOT).contains("gif")) {
                ChatMessageUtil.displayGifImageFromUrl(
                    dateLayout.context,
                    message.url,
                    fileThumbnailImage,
                    thumbnails[0].url
                )
            } else {
                ChatMessageUtil.displayImageFromUrl(dateLayout.context, thumbnails[0].url, fileThumbnailImage, null)
            }
        } else {
            //Log.e(CHECK_TAG,"otherImg NO thumbnail");
            if (message.type.toLowerCase(Locale.ROOT).contains("gif")) {
                ChatMessageUtil.displayGifImageFromUrl(
                    dateLayout.context,
                    message.url,
                    fileThumbnailImage,
                    null as String?
                )
            } else {
                ChatMessageUtil.displayImageFromUrl(dateLayout.context, message.url, fileThumbnailImage, null)
            }
        }
        if (clickListener != null) {
            fileThumbnailImage.setOnClickListener { clickListener.onFileMessageItemClick(message) }
            itemView.setOnClickListener { clickListener.onBackgroundClick() }
            if(!profileUrl.isNullOrEmpty()){
                profileImg.setOnClickListener {
                    clickListener.onProfileClick(profileUrl)
                }
            }
        }
    }

}