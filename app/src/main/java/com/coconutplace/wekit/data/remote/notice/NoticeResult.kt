package com.coconutplace.wekit.data.remote.notice

import com.coconutplace.wekit.data.entities.Notice
import com.google.gson.annotations.SerializedName

class NoticeResult {
    @SerializedName(value = "noticeList")
    var notices: ArrayList<Notice>? = null
}