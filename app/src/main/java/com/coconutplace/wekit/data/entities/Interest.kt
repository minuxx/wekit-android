package com.coconutplace.wekit.data.entities

import com.coconutplace.wekit.utils.GlobalConstant.Companion.MIRACLE_EMPTY
import com.google.gson.annotations.SerializedName

data class Interest(@SerializedName(value = "miracle") var mircle: String = MIRACLE_EMPTY,
                    @SerializedName(value = "routineIdxList") val routineIdxList: ArrayList<Int> = ArrayList())
