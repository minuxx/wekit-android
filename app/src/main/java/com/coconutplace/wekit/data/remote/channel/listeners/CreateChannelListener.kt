package com.coconutplace.wekit.data.remote.channel.listeners

interface CreateChannelListener {
    fun onCreateChannelSuccess()
    fun onCreateChannelSuccess(badgeTitle : String, badgeUrl: String, badgeExplain:String,backgroundColor:String)
    fun onCreateChannelFailure()
    fun makeSnackBar(str:String)
}