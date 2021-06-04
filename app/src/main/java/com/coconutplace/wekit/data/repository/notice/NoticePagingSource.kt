package com.coconutplace.wekit.data.repository.notice

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.coconutplace.wekit.data.entities.Notice
import com.coconutplace.wekit.data.remote.notice.NoticeService
import retrofit2.HttpException
import java.io.IOException

class NoticePagingSource(private val noticeService: NoticeService) : PagingSource<Int, Notice>() {
//    suspend fun getNotices(page: Int): NoticeResponse {
//        return apiRequest { noticeService.getNotices(page) }
//    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Notice> {
        return try{
            val nextPage = params.key ?: 1
            val response = noticeService.getNotices(nextPage).body()
            val data = response!!.result.notices!!

            LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = if (data.isEmpty()) null else nextPage + 1
            )
        } catch (e: IOException){
            LoadResult.Error(e)
        } catch (e: HttpException){
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Notice>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}