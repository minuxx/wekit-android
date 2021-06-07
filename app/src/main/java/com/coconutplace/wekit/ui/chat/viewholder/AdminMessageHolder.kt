package com.coconutplace.wekit.ui.chat.viewholder

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.ui.chat.ChatMessageAdapter
import com.coconutplace.wekit.utils.ChatMessageUtil
import com.sendbird.android.BaseMessage
import org.json.JSONArray
import org.json.JSONObject

class AdminMessageHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    private val dateLayout: LinearLayout = itemView.findViewById(R.id.chat_date_layout)
    private val dateText: TextView = itemView.findViewById(R.id.chat_date_text)
    private val adminMessage: TextView = itemView.findViewById(R.id.chat_admin_message)

    fun onBind(
        message: BaseMessage,
        clickListener: ChatMessageAdapter.OnItemClickListener?,
        isNewDay: Boolean){

        //adminMessage.text = String.format("%s : %s",message.message, message.data)

        if(isNewDay){
            dateLayout.visibility = View.VISIBLE
            dateText.text = ChatMessageUtil.formatDate(message.createdAt)
        }
        else{
            dateLayout.visibility = View.GONE
        }

        if (clickListener != null) {
            itemView.setOnClickListener { clickListener.onBackgroundClick() }
        }

        if(message.data==null||message.data==""){ //dashboard에서 보낸 관리자메세지인 경우
            val adMsg = " ${message.message} "
            adminMessage.text = adMsg
            return
        }

        val adminData = JSONObject(message.data)
        val type = adminData.getString("type")

        if(type=="USER_JOIN"){
            val usersJson = JSONArray(adminData.getString("users"))
            var userNicknames = ""
            for(i in  0 until usersJson.length()){
                if(i!=0){
                    userNicknames += ", "
                }
                userNicknames += usersJson.getJSONObject(i).getString("nickname")
            }
            adminMessage.text = String.format("%s 님이 입장하였습니다.", userNicknames)
        }
        else if(type=="USER_LEAVE"){
            val usersJson = JSONArray(adminData.getString("users"))
            var userNicknames = ""
            for(i in 0 until usersJson.length()){
                if(i!=0){
                    userNicknames += ", "
                }
                userNicknames += usersJson.getJSONObject(i).getString("nickname")
            }

            val reason = adminData.getString("reason")
            if(reason=="LEFT_BY_OWN_CHOICE"){
                adminMessage.text = String.format("%s 님이 나갔습니다.", userNicknames)

            }
            else if(reason == "LEFT"){
                adminMessage.text = String.format("%s 님이 추방당하였습니다.", userNicknames)
            }
        }
        else{
            adminMessage.text = message.message
        }
    }
}