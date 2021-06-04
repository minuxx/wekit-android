package com.coconutplace.wekit.data.remote.interest

import com.coconutplace.wekit.data.entities.Home
import com.google.gson.annotations.SerializedName

data class InterestResponse(
    @SerializedName(value = "isSuccess") val isSuccess : Boolean,
    @SerializedName(value = "code") val code : Int,
    @SerializedName(value = "message") val message : String
)
