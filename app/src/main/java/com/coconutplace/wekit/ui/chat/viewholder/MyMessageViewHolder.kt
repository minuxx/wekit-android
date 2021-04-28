package com.coconutplace.wekit.ui.chat.viewholder

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.ui.chat.ChatMessageAdapter
import com.coconutplace.wekit.utils.ChatMessageUtil
import com.sendbird.android.BaseMessage

class MyMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val dateLayout: LinearLayout = itemView.findViewById(R.id.chat_date_layout)
    private val dateText: TextView = itemView.findViewById(R.id.chat_date_text)
    private val textMessage: TextView = itemView.findViewById(R.id.chat_msg_text)
    private val sendTime: TextView = itemView.findViewById(R.id.chat_time_text)

    fun onBind(
        message: BaseMessage,
        clickListener: ChatMessageAdapter.OnItemClickListener?,
        isNewDay: Boolean
    ) {
        textMessage.text = message.message
        sendTime.text = ChatMessageUtil.formatTime(message.createdAt)

        if(isNewDay){
            dateLayout.visibility = View.VISIBLE
            dateText.text = ChatMessageUtil.formatDate(message.createdAt)
        }
        else{
            dateLayout.visibility = View.GONE
        }

        if (clickListener != null) {
            //itemView.setOnClickListener { clickListener.onUserMessageItemClick(message as UserMessage?) }
            itemView.setOnClickListener { clickListener.onBackgroundClick() }
        }
    }

}