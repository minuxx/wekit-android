package com.coconutplace.wekit.data.entities

import com.google.gson.annotations.SerializedName

data class Notice(
    @SerializedName(value = "noticeIdx") val noticeIdx: Int,
    @SerializedName(value = "noticeCategory") val noticeCategory: String,
    @SerializedName(value = "noticeTitle") val noticeTitle: String,
    @SerializedName(value = "noticeContent") val noticeContent: String,
    @SerializedName(value = "date") val date: String,
)
