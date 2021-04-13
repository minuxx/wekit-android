package com.coconutplace.wekit.data.entities

import android.os.Parcelable
import com.github.mikephil.charting.data.Entry
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class BodyGraph(
    @SerializedName(value = "xData")  var xData: ArrayList<String>? = null,
    @SerializedName(value = "weightData") var weightData: ArrayList<Entry>? = null,
    @SerializedName(value = "basalMetabolismData") var basalMetabolismData: ArrayList<Entry>? = null,
    @SerializedName(value = "bmiData") var bmiData: ArrayList<Entry>? = null): Parcelable