package com.coconutplace.wekit.data.entities

import com.google.gson.annotations.SerializedName
import java.util.*

data class Home(
    @SerializedName(value = "nickname") val nickname: String,
    @SerializedName(value = "day") val day: Int,
    @SerializedName(value = "challengeText") val challengeText: String,
    @SerializedName(value = "totalDay") val totalDay: Int,
    @SerializedName(value = "age") val age: Int,
    @SerializedName(value = "gender") val gender: String,
    @SerializedName(value = "targetWeight") val targetWeight: Double,
    @SerializedName(value = "graphInfo") val graphInfo: ArrayList<BodyInfo>,
){
    var bodyGraph: BodyGraph? = null
}
