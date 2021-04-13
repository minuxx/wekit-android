package com.coconutplace.wekit.data.remote.auth

import com.coconutplace.wekit.data.entities.Auth
import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName(value = "isSuccess") val isSuccess : Boolean,
    @SerializedName(value = "code") val code : Int,
    @SerializedName(value = "message") val message : String,
    @SerializedName(value = "result") val auth : Auth?
)
