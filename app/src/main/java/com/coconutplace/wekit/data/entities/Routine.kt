package com.coconutplace.wekit.data.entities

import com.google.gson.annotations.SerializedName

data class Routine(@SerializedName(value = "id") var id: Int = 0,
                   @SerializedName(value = "imgRes") var imgRes: Int = 0,
                   @SerializedName(value = "name") var name: String = "",)
