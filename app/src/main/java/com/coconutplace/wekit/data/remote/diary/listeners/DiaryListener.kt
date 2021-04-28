package com.coconutplace.wekit.data.remote.diary.listeners

import com.coconutplace.wekit.data.entities.Diary

interface DiaryListener {
    fun onDiaryStarted()
    fun onDiarySuccess(diaries: ArrayList<Diary>)
    fun onDiaryFailure(code: Int, message: String)

    fun onGetWrittenDatesStarted()
    fun onGetWrittenDatesSuccess()
    fun onGetWrittenDatesFailure(code: Int, message: String)
}