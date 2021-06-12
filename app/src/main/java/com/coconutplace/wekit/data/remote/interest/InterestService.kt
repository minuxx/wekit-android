package com.coconutplace.wekit.data.remote.interest

import com.coconutplace.wekit.data.entities.Auth
import com.coconutplace.wekit.data.entities.BodyInfo
import com.coconutplace.wekit.data.entities.Interest
import com.coconutplace.wekit.data.remote.auth.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface InterestService {
    @POST("users/interest")
    suspend fun postInterest(@Body interest: Interest) : Response<InterestResponse>

}
