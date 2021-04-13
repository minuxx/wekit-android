package com.coconutplace.wekit.ui.notice

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.Notice
import com.coconutplace.wekit.data.remote.notice.NoticeListener
import com.coconutplace.wekit.data.repository.notice.NoticeRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines

class NoticeViewModel(
    private val repository: NoticeRepository
) : ViewModel() {
    var noticeListener: NoticeListener? = null
    val notices = ObservableArrayList<Notice>()

    fun getNotices(page: Int) {
        noticeListener?.onGetNoticeStarted()

        Coroutines.main {
            try {
                val response = repository.getNotices(page)

                if (response.isSuccess) {
                    noticeListener?.onGetNoticeSuccess(response.result.notices!!)
                    return@main
                }

                noticeListener?.onGetNoticeFailure(response.code, response.message)
            } catch (e: ApiException) {
                noticeListener?.onGetNoticeFailure(404, e.message!!)
            } catch (e: Exception) {
                noticeListener?.onGetNoticeFailure(404, e.message!!)
            }
        }
    }
}