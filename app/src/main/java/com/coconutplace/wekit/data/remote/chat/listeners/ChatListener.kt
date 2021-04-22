package com.coconutplace.wekit.data.remote.chat.listeners

import com.sendbird.android.BaseMessage

interface ChatListener {
    fun onExitSuccess()
    fun onSendMessageSuccess()
    fun showStartChallengeButton(isHost:Boolean)
    fun addOldMsg(msgList:List<BaseMessage>)
    fun addRecentMessage(msg: BaseMessage)
    fun onBadgeResponse(badgeTitle:String,badgeUrl:String,badgeExplain:String, backgroundColor:String)
    fun startDiary()
}