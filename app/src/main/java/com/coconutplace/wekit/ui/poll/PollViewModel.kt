package com.coconutplace.wekit.ui.poll

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.BodyInfo
import com.coconutplace.wekit.data.remote.auth.listeners.PollListener
import com.coconutplace.wekit.data.repository.auth.AuthRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.coconutplace.wekit.utils.SharedPreferencesManager
import java.lang.Exception
import java.util.regex.Pattern

class PollViewModel(
    private val repository: AuthRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {
    var pollListener: PollListener? = null

    val height: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val weight: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val targetWeight: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    private fun getBody(): BodyInfo = BodyInfo(
        height = height.value!!.toDouble(),
        weight = weight.value!!.toDouble(),
        date = "",
        targetWeight = if(targetWeight.value!!.toString().isEmpty()){ weight.value!!.toString().toDouble() } else { targetWeight.value!!.toString().toDouble() }
    )

    fun poll() {
        val _height = height.value.toString()
        val _weight = weight.value.toString()
        val _targetWeight = targetWeight.value.toString()

        pollListener?.onPollStarted()

        if(_height.isEmpty()){
            pollListener?.onPollFailure(303, "키를 입력해주세요.")
            return
        }

        if(_weight.isEmpty()){
            pollListener?.onPollFailure(305, "몸무게를 입력해주세요.")
            return
        }

        if(!Pattern.matches("(^\\d+\$)|(^\\d+.\\d{1,2}\$)", _height)){
            pollListener?.onPollFailure(353, "키를 올바르게 입력해주세요. 예) 172.2")
            return
        }

        if(!Pattern.matches("(^\\d+\$)|(^\\d+.\\d{1,2}\$)", _weight)){
            pollListener?.onPollFailure(355, "몸무게를 올바르게 입력해주세요. 예) 65.5")
            return
        }

        if(!Pattern.matches("(^\\d+\$)|(^\\d+.\\d{1,2}\$)", _targetWeight)){
            pollListener?.onPollFailure(356, "몸무게를 올바르게 입력해주세요. 예)  65.5")
            return
        }

        Coroutines.main {
            try {
                val authResponse = repository.poll(getBody())

                if (authResponse.isSuccess) {
                    pollListener?.onPollSuccess(authResponse.message)
                    sharedPreferencesManager.saveBody(getBody())
                    return@main
                } else {
                    pollListener?.onPollFailure(authResponse.code, authResponse.message)
                }
            } catch (e: ApiException) {
                pollListener?.onPollFailure(404, e.message!!)
            } catch (e: Exception){
                pollListener?.onPollFailure(404, e.message!!)
            }
        }
    }
}