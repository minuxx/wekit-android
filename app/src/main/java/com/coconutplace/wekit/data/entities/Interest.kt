package com.coconutplace.wekit.data.entities

import android.media.Image
import com.bumptech.glide.load.engine.Resource
import com.google.gson.annotations.SerializedName

data class Interest(

    @SerializedName(value = "miracle") val miracle: String,
    @SerializedName(value = "routine") val routine: String

)

data class InnerInterest(

    @SerializedName(value = "interestImg") val interestImg: Int,
    @SerializedName(value = "interestText") val interestText: String

)