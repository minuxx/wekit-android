package com.coconutplace.wekit.data.remote.home

import com.coconutplace.wekit.data.entities.Auth
import com.coconutplace.wekit.data.remote.auth.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface HomeService {
    @GET("home")
    suspend fun home() : Response<HomeResponse>

    @PATCH("fcm-token")
    suspend fun sendFcmToken(@Body fcmToken: Auth?) : Response<HomeResponse>
}
