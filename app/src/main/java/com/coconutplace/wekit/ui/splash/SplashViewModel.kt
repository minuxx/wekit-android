package com.coconutplace.wekit.ui.splash

import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.remote.auth.listeners.SplashListener
import com.coconutplace.wekit.data.repository.auth.AuthRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.coconutplace.wekit.utils.SharedPreferencesManager
import kotlinx.coroutines.delay

class SplashViewModel(private val repository: AuthRepository, private val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {
    var splashListener: SplashListener? = null

    init {
        getVersion()
    }

    fun autoLogin() {
        splashListener?.onStarted()

        Coroutines.main {
            try {
                delay(3000)
                val authResponse = repository.autoLogin()

                if(authResponse.isSuccess) {
                    authResponse.auth?.let {
                        splashListener?.onAutoLoginSuccess(authResponse.message)
                        sharedPreferencesManager.saveJwtToken(authResponse.auth.jwtToken!!)
                        return@main
                    }
                }else{
                    splashListener?.onAutoLoginFailure(authResponse.code, authResponse.message)
                }
            } catch (e: ApiException) {
                splashListener?.onAutoLoginFailure(404, e.message!!)
            } catch (e: Exception){
                splashListener?.onAutoLoginFailure(404, e.message!!)
            }
        }
    }

    fun autoLoginWithChannelUrl(channelUrl:String){
        Coroutines.main {
            try {
                delay(1000)
                val authResponse = repository.autoLogin()

                if(authResponse.isSuccess) {
                    authResponse.auth?.let {
                        splashListener?.onAutoLoginSuccessWithChannelUrl(authResponse.message,channelUrl)
                        sharedPreferencesManager.saveJwtToken(authResponse.auth.jwtToken!!)
                        return@main
                    }
                }else{
                    splashListener?.onAutoLoginFailure(authResponse.code, authResponse.message)
                }
            } catch (e: ApiException) {
                splashListener?.onAutoLoginFailure(404, e.message!!)
            } catch (e: Exception){
                splashListener?.onAutoLoginFailure(404, e.message!!)
            }
        }
    }

    private fun getVersion() {
        splashListener?.onStarted()

        Coroutines.main {
            try {
                val authResponse = repository.getVersion()

                if(authResponse.isSuccess) {
                    authResponse.auth?.let {
                        splashListener?.onGetVersionSuccess(authResponse.auth)
                        return@main
                    }
                }else{
                    splashListener?.onGetVersionFailure(authResponse.code, authResponse.message)
                }
            } catch (e: ApiException) {
                splashListener?.onGetVersionFailure(404, e.message!!)
            } catch (e: Exception){
                splashListener?.onGetVersionFailure(404, e.message!!)
            }
        }
    }
}