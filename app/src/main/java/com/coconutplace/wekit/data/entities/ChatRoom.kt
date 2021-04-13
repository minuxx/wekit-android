package com.coconutplace.wekit.data.entities

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList

data class ChatRoom( //채팅방 정보
    @SerializedName(value = "roomIdx") val roomIdx: Int,
    @SerializedName(value = "roomName") val roomName: String?,
    @SerializedName(value = "chatDescription") val chatDescription: String?,
    @SerializedName(value = "chatRoomImg") val chatRoomImg: String?,
    @SerializedName(value = "chatUrl") val chatUrl: String?,
    @SerializedName(value = "currentNum") val currentNum: Int?,
    @SerializedName(value = "certificationCount") val certificationCount: Int?,
    @SerializedName(value = "maxLimit") val maxLimit: Int?,
    @SerializedName(value = "tagList") val tagList: ArrayList<String>?,
    @SerializedName(value = "roomTerm") val roomTerm: String?,
    @SerializedName(value = "isStart") val isStart: String?
):Serializable