package com.coconutplace.wekit.data.repository.interest

import com.coconutplace.wekit.data.entities.BodyInfo
import com.coconutplace.wekit.data.entities.Interest
import com.coconutplace.wekit.data.remote.interest.InterestResponse
import com.coconutplace.wekit.data.remote.interest.InterestService
import com.coconutplace.wekit.data.repository.BaseRepository

class InterestRepository(private val interestService: InterestService) : BaseRepository() {
    suspend fun postInterest(interest: Interest): InterestResponse {
        return apiRequest { interestService.postInterest(interest) }
    }

}


