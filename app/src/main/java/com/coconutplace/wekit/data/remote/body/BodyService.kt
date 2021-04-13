package com.coconutplace.wekit.data.remote.body

import com.coconutplace.wekit.data.entities.BodyInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BodyService {
    @GET("users/body")
    suspend fun getBodyInfo() : Response<BodyResponse>

    @POST("users/bodyinfo")
    suspend fun postBodyInfo(@Body body: BodyInfo) : Response<BodyResponse>
}
