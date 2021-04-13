package com.coconutplace.wekit.data.entities

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName(value = "userIdx")
    var userIdx:Int,
    @SerializedName(value = "id")
    val id: String?,
    @SerializedName(value = "nickname")
    var nickname:String,
    @SerializedName(value = "countNum")
    var countNum:Int,
    @SerializedName(value = "todayCount")
    var todayCount: Int,
    @SerializedName(value = "type")
    val type: String
)
