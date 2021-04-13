package com.coconutplace.wekit.data.remote.auth.listeners

interface PollListener {
    fun onPollStarted()
    fun onPollSuccess(message: String)
    fun onPollFailure(code: Int, message: String)
}