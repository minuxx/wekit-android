package com.coconutplace.wekit.data.remote.auth.listeners

import com.coconutplace.wekit.data.entities.Auth

interface MainListener {
    fun onStarted()
    fun onGetVersionSuccess(auth: Auth)
    fun onGetVersionFailure(code: Int, message: String)
}