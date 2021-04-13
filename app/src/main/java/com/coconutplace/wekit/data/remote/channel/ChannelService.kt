package com.coconutplace.wekit.data.remote.channel

import com.coconutplace.wekit.data.entities.ChatExtentions
import com.coconutplace.wekit.data.entities.ChatRoom
import com.coconutplace.wekit.data.entities.CreateChannelInfo
import retrofit2.Response
import retrofit2.http.*

interface ChannelService {
    @POST("/chat/room")
    suspend fun createChannel(@Body createChannelInfo: CreateChannelInfo) : Response<ChannelResponse>

    @GET("/chat/recent-list?")
    suspend fun getRecentChannelList(@Query("page") page: Int): Response<ChannelResponse>

    @GET("/chat/list?")
    suspend fun getAllChannelList(@Query("page") page: Int) : Response<ChannelResponse>

    @GET("/chat/filter?")
    suspend fun getFilteredChannelList(
        @Query("authenticCount")authCount:Int,
        @Query("roomTerm")roomTerm: Int,
        @Query("memberCount")memberCount: Int,
        @Query("isStart")isStart:Int,
        @Query("page")page:Int
    ) : Response<ChannelResponse>

    @GET("/chat/search?")
    suspend fun getSearchChannelList(
        @Query("content")content: String,
        @Query("page")page: Int
    ) : Response<ChannelResponse>

    @GET("/chat/mylist")
    suspend fun getMyChannel(): Response<ChannelResponse>

    @POST("/chat/join")
    suspend fun enterChannel(@Body chatRoom: ChatRoom): Response<ChannelResponse>

    @PATCH("/chat/leave")
    suspend fun leaveChannel(@Body chatEx: ChatExtentions) : Response<ChannelResponse>
}