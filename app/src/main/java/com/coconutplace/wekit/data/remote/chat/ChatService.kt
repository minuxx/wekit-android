package com.coconutplace.wekit.data.remote.chat

import com.coconutplace.wekit.data.entities.ChatExtentions
import com.coconutplace.wekit.data.entities.Diary
import retrofit2.Response
import retrofit2.http.*

interface ChatService {
    @PATCH("/chat/leave")
    suspend fun leaveChannel(@Body chatEx: ChatExtentions) : Response<ChatResponse>
    @GET("/chat/roominfo?")
    suspend fun getRoomInfo(@Query("roomIdx") roomIdx: Int):Response<ChatResponse>
    @POST("/chat/reportroom")
    suspend fun reportChannel(@Body chatEx: ChatExtentions) : Response<ChatResponse>
    @POST("/chat/banish")
    suspend fun expelMember(@Body chatEx: ChatExtentions): Response<ChatResponse>
    @POST("/diary/record")
    suspend fun postDiaryWithChat(@Body diary:Diary): Response<ChatResponse>
    @POST("/chat/start")
    suspend fun startChallenge(@Body chatEx: ChatExtentions) : Response<ChatResponse>
    @POST("/chat/check-challenge")
    suspend fun checkChallenge(@Body chatEx: ChatExtentions) : Response<ChatResponse>
}