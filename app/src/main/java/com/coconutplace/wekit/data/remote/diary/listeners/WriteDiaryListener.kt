package com.coconutplace.wekit.data.remote.diary.listeners

import com.coconutplace.wekit.data.entities.Diary

interface WriteDiaryListener {
    fun onUploadToFirebaseStarted()
    fun onUploadToFirebaseSuccess()
    fun onUploadToFirebaseFailure()

    fun onPostDiaryStarted()
    fun onPostDiarySuccess(message: String)
    fun onPostDiarySuccess(message: String, badgeTitle:String, badgeUrl:String, badgeExplain:String, backgroundColor:String)
    fun onPostDiaryFailure(code: Int, message: String)

    fun onGetDiaryStarted()
    fun onGetDiarySuccess(diary: Diary)
    fun onGetDiaryFailure(code: Int, message: String)
}