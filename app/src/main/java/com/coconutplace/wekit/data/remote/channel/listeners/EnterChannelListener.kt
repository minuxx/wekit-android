package com.coconutplace.wekit.data.remote.channel.listeners

interface EnterChannelListener {
    fun callChatActivity(channelUrl: String, roomIdx:Int)
    fun makeSnackBar(str:String)
}