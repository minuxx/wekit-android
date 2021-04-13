package com.coconutplace.wekit.data.entities

import com.google.gson.annotations.SerializedName

data class ChannelResult(
    @SerializedName(value = "chatList")
    var chatList: ArrayList<ChatRoom>? = null,

    @SerializedName(value = "userIdx")
    var userIdx: Int?,

    @SerializedName(value = "badgeName")
    val badgeTitle: String?,

    @SerializedName(value = "badgeImageUrl")
    val badgeUrl : String?,

    @SerializedName(value = "badgeDescription")
    val badgeExplain: String?,

    @SerializedName(value = "backgroundColor")
    val backgroundColor: String?

)