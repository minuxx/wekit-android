package com.coconutplace.wekit.data.remote.auth.listeners

import com.coconutplace.wekit.data.entities.Auth

interface SetListener {
    fun onGetProfileStarted()
    fun onGetProfileSuccess(auth: Auth)
    fun onGetProfileFailure(code: Int, message: String)

    fun onSendFcmTokenStarted()
    fun onSendFcmTokenSuccess()
    fun onSendFcmTokenFailure(code: Int, message: String)
}