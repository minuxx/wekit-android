package com.coconutplace.wekit.data.remote.auth.listeners

interface ProfileListener {
    fun onPatchProfileStarted()
    fun onPatchProfileSuccess()
    fun onPatchProfileFailure(code: Int, message: String)

    fun onUploadToFirebaseStarted()
    fun onUploadToFirebaseSuccess()
    fun onUploadToFirebaseFailure()

    fun onDeleteUserStarted()
    fun onDeleteUserSuccess()
    fun onDeleteUserFailure(code: Int, message: String)
}