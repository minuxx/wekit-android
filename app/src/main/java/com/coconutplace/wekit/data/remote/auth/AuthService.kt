package com.coconutplace.wekit.data.remote.auth

import com.coconutplace.wekit.data.entities.Auth
import com.coconutplace.wekit.data.entities.BodyInfo
import com.coconutplace.wekit.data.entities.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthService {
    @POST("users/signup")
    suspend fun signUp(@Body user : User) : Response<AuthResponse>

    @POST("users/check-info")
    suspend fun checkUser(@Body user : User) : Response<AuthResponse>

    @POST("users/bodyinfo")
    suspend fun poll(@Body body : BodyInfo) : Response<AuthResponse>

    @POST("users/login")
    suspend fun login(@Body user : User) : Response<AuthResponse>

    @GET("users/autologin")
    suspend fun autoLogin() : Response<AuthResponse>

    @GET("users/profile")
    suspend fun getProfile() : Response<AuthResponse>

    @PATCH("fcm-token")
    suspend fun fcm(@Body fcmToken : Auth?) : Response<AuthResponse>

    @PATCH("users/profile")
    suspend fun patchProfile(@Body user : User) : Response<AuthResponse>

    @PATCH("users/password")
    suspend fun patchPassword(@Body user : User) : Response<AuthResponse>

    @PATCH("users/status")
    suspend fun deleteUser(@Body user : User) : Response<AuthResponse>

    @GET("version")
    suspend fun getVersion() : Response<AuthResponse>

    @POST("users/email")
    suspend fun certifyEmail(@Body user: User) : Response<AuthResponse>
}
