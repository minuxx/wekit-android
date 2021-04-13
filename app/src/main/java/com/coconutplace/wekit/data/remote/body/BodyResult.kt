package com.coconutplace.wekit.data.remote.body

import com.coconutplace.wekit.data.entities.BodyInfo
import com.google.gson.annotations.SerializedName

class BodyResult {
    @SerializedName(value = "age")
    var age: Int = 0

    @SerializedName(value = "gender")
    var gender: String = "M"

    @SerializedName(value = "bodyInfoList")
    var bodyList: ArrayList<BodyInfo> = ArrayList()
}