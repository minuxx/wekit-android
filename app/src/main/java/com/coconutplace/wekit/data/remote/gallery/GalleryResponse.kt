package com.coconutplace.wekit.data.remote.gallery

import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap

data class GalleryResponse (
    @SerializedName(value="result") val result: LinkedTreeMap<String,ArrayList<String>>,
    @SerializedName(value="isSuccess") val isSuccess: Boolean,
    @SerializedName(value = "code") val code: String,
    @SerializedName(value = "message") val message: String,
)