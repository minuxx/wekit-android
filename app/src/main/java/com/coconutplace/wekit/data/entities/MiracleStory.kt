package com.coconutplace.wekit.data.entities

import com.google.gson.annotations.SerializedName

data class MiracleStory( @SerializedName(value = "bannerTitle") val title: String,
                         @SerializedName(value = "bannerImageUrl") val imgUrl: String,
                         @SerializedName(value = "bannerSite") val link: String)
