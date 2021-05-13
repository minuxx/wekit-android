package com.coconutplace.wekit.data.repository.diary

import android.app.SearchManager.QUERY
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.remote.diary.DiaryService

class DiaryDataSource(private val date: String, private val diaryService: DiaryService) : PageKeyedDataSource<Int, Diary>() {
    val networkErrors: MutableLiveData<String> = MutableLiveData()

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Diary>
    ) {
        val curPage = 1
        val nextPage = curPage + 1
//        val list: List<Diary> = diaryService.getDiaries(date, curPage)

//        callback.onResult(list, curPage, nextPage)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Diary>) {

    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Diary>) {
//        val list: List<Diary> = diaryService.getDiaries(date, params.key)
//        callback.onResult(list)
    }
}