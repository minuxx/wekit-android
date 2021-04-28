package com.coconutplace.wekit.ui.chat.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.ui.chat.ChatMessageAdapter
import com.coconutplace.wekit.utils.ChatMessageUtil
import com.coconutplace.wekit.utils.ChatMessageUtil.Companion.formatDate
import com.sendbird.android.FileMessage
import com.sendbird.android.FileMessage.Thumbnail
import java.util.*

internal class MyImgMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val dateLayout:LinearLayout = itemView.findViewById(R.id.chat_date_layout)
    private val dateText: TextView = itemView.findViewById(R.id.chat_date_text)
    private val fileThumbnailImage: ImageView = itemView.findViewById(R.id.chat_msg_img)
    private val sendTime: TextView = itemView.findViewById(R.id.chat_time_text)

    fun onBind(
        message: FileMessage,
        clickListener: ChatMessageAdapter.OnItemClickListener?,
        isNewDay: Boolean
    ) {
        sendTime.text = ChatMessageUtil.formatTime(message.createdAt)

        if(isNewDay){
            dateLayout.visibility = View.VISIBLE
            dateText.text = formatDate(message.createdAt)
        }
        else{
            dateLayout.visibility = View.GONE
        }

        val thumbnails = message.thumbnails as ArrayList<Thumbnail>
        if (thumbnails.size > 0) {
            //Log.e(SharedPreferencesManager.CHECK_TAG, "myImg thumbnail exist")
            if (message.type.toLowerCase(Locale.ROOT).contains("gif")) {
                ChatMessageUtil.displayGifImageFromUrl(dateLayout.context, message.url, fileThumbnailImage, thumbnails[0].url)
            } else {
                ChatMessageUtil.displayImageFromUrl(dateLayout.context, thumbnails[0].url, fileThumbnailImage, null)
            }
        } else {
            //Log.e(SharedPreferencesManager.CHECK_TAG, "myImg NO thumbnail")
            if (message.type.toLowerCase(Locale.ROOT).contains("gif")) {
                ChatMessageUtil.displayGifImageFromUrl(dateLayout.context, message.url, fileThumbnailImage, null)
            } else {
                ChatMessageUtil.displayImageFromUrl(dateLayout.context, message.url, fileThumbnailImage, null)
            }
        }
        if (clickListener != null) {
            fileThumbnailImage.setOnClickListener { clickListener.onFileMessageItemClick(message) }
            itemView.setOnClickListener { clickListener.onBackgroundClick() }
        }
    }

}