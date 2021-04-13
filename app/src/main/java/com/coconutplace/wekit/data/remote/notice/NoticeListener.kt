package com.coconutplace.wekit.data.remote.notice

import com.coconutplace.wekit.data.entities.Notice

interface NoticeListener {
    fun onGetNoticeStarted()
    fun onGetNoticeSuccess(notices: ArrayList<Notice>)
    fun onGetNoticeFailure(code: Int, message: String)
}