package com.coconutplace.wekit.data.remote.diary.listeners

import com.coconutplace.wekit.data.entities.Diary

interface DiaryListener {
    fun onGetDiaryStarted()
    fun onGetDiarySuccess(diaries: ArrayList<Diary>)
    fun onGetDiaryFailure(code: Int, message: String)

    fun onGetWrittenDatesStarted()
    fun onGetWrittenDatesSuccess()
    fun onGetWrittenDatesFailure(code: Int, message: String)
}