package com.coconutplace.wekit.data.repository.diary

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.remote.diary.DiaryService
import retrofit2.HttpException
import java.io.IOException

class DiaryPagingSource(private val date: String, private val diaryService: DiaryService) : PagingSource<Int, Diary>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Diary> {
        return try{
            val nextPage = params.key ?: 1
            val response = diaryService.getDiaries(date, nextPage).body()
            val data = response!!.result!!.diaryList!!

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

    override fun getRefreshKey(state: PagingState<Int, Diary>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}