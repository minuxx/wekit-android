package com.coconutplace.wekit.data.repository.notice

import com.coconutplace.wekit.data.remote.notice.NoticeResponse
import com.coconutplace.wekit.data.remote.notice.NoticeService
import com.coconutplace.wekit.data.repository.BaseRepository

class NoticeRepository(val noticeService: NoticeService) : BaseRepository() {
    suspend fun getNotices(page: Int): NoticeResponse {
        return apiRequest { noticeService.getNotices(page) }
    }
}