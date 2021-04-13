package com.coconutplace.wekit.data.remote.body.listeners

interface InputBodyListener {
    fun onInputBodyStarted()
    fun onInputBodySuccess(message: String)
    fun onInputBodyFailure(code: Int, message: String)
}