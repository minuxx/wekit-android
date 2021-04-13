package com.coconutplace.wekit.data.repository.home

import com.coconutplace.wekit.data.entities.Auth
import com.coconutplace.wekit.data.remote.auth.AuthResponse
import com.coconutplace.wekit.data.remote.home.HomeResponse
import com.coconutplace.wekit.data.remote.home.HomeService
import com.coconutplace.wekit.data.repository.BaseRepository

class HomeRepository(private val homeService: HomeService) : BaseRepository() {
    suspend fun home(): HomeResponse {
        return apiRequest { homeService.home() }
    }

    suspend fun sendFcmToken(fcmToken: Auth?): HomeResponse {
        return apiRequest { homeService.sendFcmToken(fcmToken) }
    }
}