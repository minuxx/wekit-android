package com.coconutplace.wekit.data.remote.notice

import com.google.gson.annotations.SerializedName

data class NoticeResponse(
    @SerializedName(value = "isSuccess") val isSuccess : Boolean,
    @SerializedName(value = "code") val code : Int,
    @SerializedName(value = "message") val message : String,
    @SerializedName(value = "result") val result : NoticeResult,
)
