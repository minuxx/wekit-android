package com.coconutplace.wekit.data.remote.badge

import retrofit2.Response
import retrofit2.http.GET

interface BadgeService {
    @GET("/badges")
    suspend fun getBadge(): Response<BadgeResponse>
}