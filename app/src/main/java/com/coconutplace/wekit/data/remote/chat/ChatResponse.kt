package com.coconutplace.wekit.data.remote.chat

import com.coconutplace.wekit.data.entities.RoomInfo
import com.google.gson.annotations.SerializedName

data class ChatResponse (
    @SerializedName(value = "result") val result: RoomInfo?,
    @SerializedName(value = "isSuccess") val isSuccess: Boolean,
    @SerializedName(value = "code") val code: String,
    @SerializedName(value = "message") val message: String,
)