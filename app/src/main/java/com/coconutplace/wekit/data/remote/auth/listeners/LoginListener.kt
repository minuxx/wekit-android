package com.coconutplace.wekit.data.remote.auth.listeners

import com.coconutplace.wekit.data.entities.Auth

interface LoginListener {
    fun onLoginStarted()
    fun onLoginSuccess(auth: Auth)
    fun onLoginFailure(code: Int, message: String)
}