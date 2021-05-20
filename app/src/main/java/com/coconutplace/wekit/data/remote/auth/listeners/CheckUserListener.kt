package com.coconutplace.wekit.data.remote.auth.listeners

interface CheckUserListener {
    fun onCheckUserStarted()
    fun onCheckUserSuccess(message: String)
    fun onCheckUserFailure(code: Int, message: String)
}