package com.coconutplace.wekit.ui.main

import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.remote.auth.listeners.MainListener
import com.coconutplace.wekit.data.repository.auth.AuthRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines

class MainViewModel(private val repository: AuthRepository): ViewModel()  {
    var mainListener: MainListener? = null
    var pushChannelUrl:String? = null

    init {
        getVersion()
    }

     fun getVersion() {
        mainListener?.onStarted()

        Coroutines.main {
            try {
                val authResponse = repository.getVersion()

                if(authResponse.isSuccess) {
                    authResponse.auth?.let {
                        mainListener?.onGetVersionSuccess(authResponse.auth)
                        return@main
                    }
                }else{
                    mainListener?.onGetVersionFailure(authResponse.code, authResponse.message)
                }
            } catch (e: ApiException) {
                mainListener?.onGetVersionFailure(404, e.message!!)
            } catch (e: Exception){
                mainListener?.onGetVersionFailure(404, e.message!!)
            }
        }
    }

    fun setPushUrl(pushUrl: String?){
        pushChannelUrl = pushUrl
    }

    fun getPushUrl():String?{
        return pushChannelUrl
    }
}