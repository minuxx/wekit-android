package com.coconutplace.wekit.data.repository.diary

import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.remote.diary.DiaryResponse
import com.coconutplace.wekit.data.remote.diary.DiaryService
import com.coconutplace.wekit.data.repository.BaseRepository

class DiaryRepository(private val diaryService: DiaryService) : BaseRepository() {
    suspend fun getDiaries(date: String, page: Int): DiaryResponse {
        return apiRequest { diaryService.getDiaries(date, page) }
    }

    suspend fun postDiary(diary: Diary): DiaryResponse {
        return apiRequest { diaryService.postDiary(diary) }
    }

    suspend fun getDiary(diaryIdx: Int): DiaryResponse {
        return apiRequest { diaryService.getDiary(diaryIdx) }
    }

    suspend fun getWrittenDates(date: String): DiaryResponse {
        return apiRequest { diaryService.getWrittenDates(date) }
    }
}