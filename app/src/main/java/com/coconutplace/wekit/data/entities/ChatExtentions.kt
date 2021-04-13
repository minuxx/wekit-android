package com.coconutplace.wekit.data.entities

import com.google.gson.annotations.SerializedName

data class ChatExtentions ( //추방하기, 나가기, 신고하기 등 채팅방 내의 추가기능들

    //나가기,신고하기,추방하기,시작하기
    @SerializedName(value = "roomIdx") val roomIdx: Int,
    //신고하기,추방하기
    @SerializedName(value = "reason") val reason: String?,
    //추방하기
   @SerializedName(value = "banUserIdx") val banUserIdx: Int?
)