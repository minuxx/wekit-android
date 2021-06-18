package com.coconutplace.wekit.data.remote.auth.listeners

interface InterestListener {
    fun onInterestStarted()
    fun onInterestSuccess()
    fun onInterestFailure(code: Int, message: String)
}