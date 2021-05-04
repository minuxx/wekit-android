package com.coconutplace.wekit.data.remote.channel

import com.coconutplace.wekit.data.entities.ChannelResult
import com.google.gson.annotations.SerializedName

data class ChannelResponse(
    @SerializedName(value = "result") val result: ChannelResult?,
    @SerializedName(value = "isSuccess") val isSuccess: Boolean,
    @SerializedName(value = "code") val code: Int,
    @SerializedName(value = "message") val message: String,
)