package com.coconutplace.wekit.data.entities

import com.google.gson.annotations.SerializedName

data class CreateChannelInfo ( //채팅방 만들때만 쓰이는 데이터 형식
    @SerializedName(value = "roomName") val roomName: String,
    @SerializedName(value = "chatDescription") val chatDescription:String,
    @SerializedName(value = "chatRoomImg") val chatRoomImg:String,
    @SerializedName(value = "chatUrl") val chatUrl:String,
    @SerializedName(value = "maxLimit") val maxLimit:Int,
    @SerializedName(value = "roomType") val roomType:String,
    @SerializedName(value = "roomTerm") val roomTerm:String,
    @SerializedName(value = "certificationCount") val certificationCount:Int,
    @SerializedName(value = "tag") val tag:String
)