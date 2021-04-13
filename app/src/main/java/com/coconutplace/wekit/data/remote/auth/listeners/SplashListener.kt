package com.coconutplace.wekit.data.remote.auth.listeners

import com.coconutplace.wekit.data.entities.Auth

interface SplashListener {
    fun onStarted()
    fun onAutoLoginSuccess(message: String)
    fun onAutoLoginFailure(code: Int, message: String)
    fun onAutoLoginSuccessWithChannelUrl(message:String, channelUrl:String)
    fun onGetVersionSuccess(auth: Auth)
    fun onGetVersionFailure(code: Int, message: String)
}