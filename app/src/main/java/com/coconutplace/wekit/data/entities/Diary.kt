package com.coconutplace.wekit.data.entities

import com.google.gson.annotations.SerializedName

data class Diary(
    @SerializedName(value = "roomIdx") val roomIdx: Int,
    @SerializedName(value = "diaryIdx") val diaryIdx: Int?,
    @SerializedName(value = "date") val date: String,
    @SerializedName(value = "timezone") val timezone: Int,
    @SerializedName(value = "time") val time: String?,
    @SerializedName(value = "satisfaction") val satisfaction: Int,
    @SerializedName(value = "imageList") val imageList: ArrayList<String>,
    @SerializedName(value = "thumbnailUrl") val thumbnailUrl: String?,
    @SerializedName(value = "memo") val memo: String,
)