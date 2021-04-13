package com.coconutplace.wekit.data.remote.diary

import com.coconutplace.wekit.data.entities.Diary
import retrofit2.Response
import retrofit2.http.*

interface DiaryService {
    @GET("/diary/daily")
    suspend fun getDiaries(@Query("date") date: String, @Query("page") page: Int) : Response<DiaryResponse>

    @POST("/diary/record")
    suspend fun postDiary(@Body diary: Diary) : Response<DiaryResponse>

    @GET("/diary/detail")
    suspend fun getDiary(@Query("diaryIdx") diaryIdx: Int) : Response<DiaryResponse>

    @GET("/diary/{date}")
    suspend fun getWrittenDates(@Path("date") date: String) : Response<DiaryResponse>
}
