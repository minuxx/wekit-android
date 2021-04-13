package com.coconutplace.wekit.data.remote.auth.listeners

interface SignUpListener {
    fun onSignUpStarted()
    fun onSignUpSuccess(message: String)
    fun onSignUpFailure(code: Int, message: String)
}