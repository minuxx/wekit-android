package com.coconutplace.wekit.ui.set

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.Auth
import com.coconutplace.wekit.data.remote.auth.listeners.SetListener
import com.coconutplace.wekit.data.repository.auth.AuthRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.coconutplace.wekit.utils.PushUtil
import com.coconutplace.wekit.utils.SharedPreferencesManager
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.ERROR_TAG
import com.sendbird.android.SendBirdException
import com.sendbird.android.SendBirdPushHelper.OnPushRequestCompleteListener

class SetViewModel(
    private val repository: AuthRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {
    var setListener: SetListener? = null
    var mFlagLogout = false
    val profileUrl: MutableLiveData<String> = MutableLiveData<String>()

    init {
        profileUrl.postValue(null)
    }

    fun getProfile() {
        setListener?.onGetProfileStarted()

        Coroutines.main {
            try {
                val authResponse = repository.getProfile()

                if (authResponse.isSuccess) {
                    authResponse.auth?.let {
                        setListener?.onGetProfileSuccess(authResponse.auth)
                        return@main
                    }
                } else {
                    setListener?.onGetProfileFailure(authResponse.code, authResponse.message)
                }
            } catch (e: ApiException) {
                setListener?.onGetProfileFailure(404, e.message!!)
            } catch (e: Exception) {
                setListener?.onGetProfileFailure(404, e.message!!)
            }
        }
    }

    fun sendFcmToken(fcmToken: String?) {
        setListener?.onSendFcmTokenStarted()

        Coroutines.main {
            try {
                val authResponse =
                    repository.sendFcmToken(Auth(jwtToken = null, fcmToken = fcmToken))

                if (authResponse.isSuccess) {
                    setListener?.onSendFcmTokenSuccess()

                    if (mFlagLogout) {
                        sharedPreferencesManager.removeAll()
                        disconnectPushNotification()

                    }

                    return@main
                } else {
                    setListener?.onSendFcmTokenFailure(authResponse.code, authResponse.message)
                }
            } catch (e: ApiException) {
                setListener?.onSendFcmTokenFailure(404, e.message!!)
            } catch (e: Exception) {
                setListener?.onSendFcmTokenFailure(404, e.message!!)
            }
        }
    }

    private fun disconnectPushNotification() {
        PushUtil.unregisterPushHandler(object : OnPushRequestCompleteListener {
            override fun onComplete(isActive: Boolean, token: String?) {
                Log.e(CHECK_TAG,"푸시 등록해제 성공")
            }
            override fun onError(e: SendBirdException) {
                Log.e(ERROR_TAG,"푸시 등록해제 실패 $e")
            }
        })
    }
}