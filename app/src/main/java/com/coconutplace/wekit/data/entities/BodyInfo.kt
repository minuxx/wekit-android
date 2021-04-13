package com.coconutplace.wekit.data.entities

import com.google.gson.annotations.SerializedName

data class BodyInfo(
    @SerializedName(value = "height") val height: Double,
    @SerializedName(value = "weight") val weight: Double,
    @SerializedName(value = "date") val date: String,
    @SerializedName(value = "targetWeight") val targetWeight: Double
)
