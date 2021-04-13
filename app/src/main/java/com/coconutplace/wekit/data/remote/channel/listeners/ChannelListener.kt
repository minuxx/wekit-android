package com.coconutplace.wekit.data.remote.channel.listeners

interface ChannelListener {
    fun callChatActivity(channelUrl:String, roomIdx:Int)
    fun makeSnackBar(str:String)
    fun showCardView(hasChatRoom:Boolean)
    fun noChannelSearched()
}