package com.coconutplace.wekit.data.remote.auth.listeners

interface CertifyEmailListener {
    fun onCertifyEmailStarted()
    fun onCertifyEmailSuccess(certificationNumber: Int)
    fun onCertifyEmailFailure(code: Int, message: String)
}