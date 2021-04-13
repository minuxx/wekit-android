package com.coconutplace.wekit.data.remote.diary

import com.google.gson.annotations.SerializedName

data class DiaryResponse(
    @SerializedName(value = "isSuccess") val isSuccess : Boolean,
    @SerializedName(value = "code") val code : Int,
    @SerializedName(value = "message") val message : String,
    @SerializedName(value = "result") val result : DiaryResult?
)
