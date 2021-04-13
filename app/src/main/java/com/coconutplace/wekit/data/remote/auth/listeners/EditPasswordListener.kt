package com.coconutplace.wekit.data.remote.auth.listeners

interface EditPasswordListener {
    fun onEditPasswordStarted()
    fun onEditPasswordSuccess()
    fun onEditPasswordFailure(code: Int, message: String)
}