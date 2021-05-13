package com.coconutplace.wekit.data.remote.diary

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.coconutplace.wekit.data.entities.Diary
import com.google.gson.annotations.SerializedName

class DiaryResult {
    @SerializedName(value = "diaryList")
    var diaryList: ArrayList<Diary>? = null
//    var diaryList: LiveData<PagedList<Diary>>? = null


    @SerializedName(value = "diaryInfo")
    var diaryInfo: Diary? = null

    @SerializedName(value = "dayList")
    var dateList: ArrayList<String>? = null

    @SerializedName(value = "badgeName")
    var badgeTitle: String? = null

    @SerializedName(value = "badgeImageUrl")
    var badgeUrl: String? = null

    @SerializedName(value = "badgeDescription")
    var badgeExplain: String? = null

    @SerializedName(value = "backgroundColor")
    var backgroundColor: String? = null
}