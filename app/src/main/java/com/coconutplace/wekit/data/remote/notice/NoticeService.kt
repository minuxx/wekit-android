package com.coconutplace.wekit.data.remote.notice

import com.coconutplace.wekit.data.entities.Auth
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Query

interface NoticeService {
    @GET("notices")
    suspend fun getNotices(@Query("page") page: Int) : Response<NoticeResponse>
}
