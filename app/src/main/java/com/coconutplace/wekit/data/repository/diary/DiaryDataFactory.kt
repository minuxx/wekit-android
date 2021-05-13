package com.coconutplace.wekit.data.repository.diary

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.remote.diary.DiaryService

class DiaryDataFactory(private val date: String, private val service: DiaryService) :
    DataSource.Factory<Int, Diary>() {
    val mutableLiveData: MutableLiveData<DiaryDataSource> = MutableLiveData<DiaryDataSource>()
    private var diaryDataSource: DiaryDataSource? = null

    override fun create(): DataSource<Int, Diary> {
        diaryDataSource = DiaryDataSource(date, service)
        mutableLiveData.postValue(diaryDataSource!!)

        return diaryDataSource!!
    }
}