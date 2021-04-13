package com.coconutplace.wekit.data.remote.body

import com.google.gson.annotations.SerializedName

data class BodyResponse(
    @SerializedName(value = "isSuccess") val isSuccess : Boolean,
    @SerializedName(value = "code") val code : Int,
    @SerializedName(value = "message") val message : String,
    @SerializedName(value = "result") val result : BodyResult?
)
