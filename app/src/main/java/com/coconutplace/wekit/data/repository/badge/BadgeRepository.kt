package com.coconutplace.wekit.data.repository.badge

import com.coconutplace.wekit.data.remote.badge.BadgeResponse
import com.coconutplace.wekit.data.remote.badge.BadgeService
import com.coconutplace.wekit.data.repository.BaseRepository

class BadgeRepository(private val badgeService:BadgeService) : BaseRepository(){
    suspend fun getBadge(): BadgeResponse {
        return apiRequest { badgeService.getBadge() }
    }
}