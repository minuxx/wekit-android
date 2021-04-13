package com.coconutplace.wekit.data.repository.body

import com.coconutplace.wekit.data.entities.BodyInfo
import com.coconutplace.wekit.data.remote.body.BodyResponse
import com.coconutplace.wekit.data.remote.body.BodyService
import com.coconutplace.wekit.data.repository.BaseRepository

class BodyRepository(private val bodyService: BodyService) : BaseRepository() {
    suspend fun getBodyInfo(): BodyResponse {
        return apiRequest { bodyService.getBodyInfo() }
    }

    suspend fun postBodyInfo(body: BodyInfo): BodyResponse {
        return apiRequest { bodyService.postBodyInfo(body) }
    }
}