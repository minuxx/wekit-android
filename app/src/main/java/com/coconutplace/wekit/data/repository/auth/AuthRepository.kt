package com.coconutplace.wekit.data.repository.auth

import com.coconutplace.wekit.data.entities.Auth
import com.coconutplace.wekit.data.entities.BodyInfo
import com.coconutplace.wekit.data.entities.User
import com.coconutplace.wekit.data.remote.auth.AuthResponse
import com.coconutplace.wekit.data.remote.auth.AuthService
import com.coconutplace.wekit.data.repository.BaseRepository

class AuthRepository(private val authService: AuthService) : BaseRepository(){
    suspend fun signUp(user: User): AuthResponse {
        return apiRequest { authService.signUp(user) }
    }

    suspend fun checkUser(user: User): AuthResponse {
        return apiRequest { authService.checkUser(user) }
    }

    suspend fun poll(body: BodyInfo): AuthResponse {
        return apiRequest { authService.poll(body) }
    }

    suspend fun login(user: User): AuthResponse {
        return apiRequest { authService.login(user) }
    }

    suspend fun autoLogin(): AuthResponse {
        return apiRequest { authService.autoLogin() }
    }

    suspend fun getProfile(): AuthResponse {
        return apiRequest { authService.getProfile() }
    }

    suspend fun sendFcmToken(fcmToken: Auth?): AuthResponse {
        return apiRequest { authService.fcm(fcmToken) }
    }

    suspend fun patchProfile(user: User): AuthResponse {
        return apiRequest { authService.patchProfile(user) }
    }

    suspend fun patchPassword(user: User): AuthResponse {
        return apiRequest { authService.patchPassword(user) }
    }

    suspend fun deleteUser(user: User): AuthResponse {
        return apiRequest { authService.deleteUser(user) }
    }

    suspend fun getVersion(): AuthResponse {
        return apiRequest { authService.getVersion() }
    }

    suspend fun certifyEmail(user: User): AuthResponse {
        return apiRequest { authService.certifyEmail(user) }
    }
}
