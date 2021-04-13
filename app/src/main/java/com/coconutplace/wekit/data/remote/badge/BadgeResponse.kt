package com.coconutplace.wekit.data.remote.badge

import com.google.gson.annotations.SerializedName

data class BadgeResponse (
    @SerializedName(value = "result") val result: BadgeResult?,
    @SerializedName(value = "isSuccess") val isSuccess: Boolean,
    @SerializedName(value = "code") val code: String,
    @SerializedName(value = "message") val message: String,
)

data class BadgeResult(
    @SerializedName(value = "existBadgeList")
    val existList: ArrayList<BadgeInfo>,
    @SerializedName(value = "nonExistBadgeList")
    val nonExistList: ArrayList<BadgeInfo>
)

data class BadgeInfo(
    @SerializedName(value = "badgeIdx")
    val badgeIdx:Int,
    @SerializedName(value = "badgeName")
    val badgeName:String,
    @SerializedName(value = "badgeImageUrl")
    val badgeImageUrl:String,
    @SerializedName(value = "backgroundColor")
    val backgroundColor:String
)