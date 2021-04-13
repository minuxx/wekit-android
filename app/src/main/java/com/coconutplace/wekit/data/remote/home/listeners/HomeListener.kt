package com.coconutplace.wekit.data.remote.home.listeners

import com.coconutplace.wekit.data.entities.Home

interface HomeListener {
    fun onHomeStarted()
    fun onHomeSuccess(home: Home)
    fun onHomeFailure(code: Int, message: String)

    fun onSendFcmTokenStarted()
    fun onSendFcmTokenSuccess()
    fun onSendFcmTokenFailure(code: Int, message: String)
}