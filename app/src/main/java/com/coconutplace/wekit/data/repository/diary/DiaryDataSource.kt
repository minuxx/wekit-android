package com.coconutplace.wekit.data.repository.diary

import android.app.SearchManager.QUERY
import androidx.paging.PageKeyedDataSource
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.remote.diary.DiaryService

class DiaryDataSource(private val date:String, private val diaryService: DiaryService) : PageKeyedDataSource<Int, Diary>() {
    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Diary>
    ) {

    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Diary>) {

    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Diary>) {
        //diaryService.getDiaries(date, params.key)
    }
}