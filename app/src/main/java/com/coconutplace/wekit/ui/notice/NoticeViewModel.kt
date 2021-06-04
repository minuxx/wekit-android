package com.coconutplace.wekit.ui.notice

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.coconutplace.wekit.data.entities.Notice
import com.coconutplace.wekit.data.remote.notice.NoticeListener
import com.coconutplace.wekit.data.remote.notice.NoticeService
import com.coconutplace.wekit.data.repository.notice.NoticePagingSource
import com.coconutplace.wekit.data.repository.notice.NoticeRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines

class NoticeViewModel(
    private val repository: NoticeRepository
) : ViewModel() {
    var noticeListener: NoticeListener? = null
//    val notices = ObservableArrayList<Notice>()

    val noticeFlow = Pager(PagingConfig(pageSize = 10)){
        NoticePagingSource(repository.noticeService)
    }.flow.cachedIn(viewModelScope)

//    fun getNotices(page: Int) {
//        noticeListener?.onGetNoticeStarted()
//
//        Coroutines.main {
//            try {
//                val response = repository.getNotices(page)
//
//                if (response.isSuccess) {
//                    noticeListener?.onGetNoticeSuccess(response.result.notices!!)
//                    return@main
//                }
//
//                noticeListener?.onGetNoticeFailure(response.code, response.message)
//            } catch (e: ApiException) {
//                noticeListener?.onGetNoticeFailure(404, e.message!!)
//            } catch (e: Exception) {
//                noticeListener?.onGetNoticeFailure(404, e.message!!)
//            }
//        }
//    }
}