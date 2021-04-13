package com.coconutplace.wekit.ui.input_body

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.BodyInfo
import com.coconutplace.wekit.data.remote.body.listeners.InputBodyListener
import com.coconutplace.wekit.data.repository.body.BodyRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.coconutplace.wekit.utils.SharedPreferencesManager
import com.google.gson.Gson
import java.util.regex.Pattern

class InputBodyViewModel(
    private val repository: BodyRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {
    var inputBodyListener: InputBodyListener? = null
    val height: MutableLiveData<String> = MutableLiveData<String>()
    val weight: MutableLiveData<String> = MutableLiveData<String>()
    val targetWeight: MutableLiveData<String> = MutableLiveData<String>()

    init {
        val gson = Gson()
        val bodyJson: String? = sharedPreferencesManager.getBody()
        if(bodyJson == null){
            setBody("", "", "")
        }else{
            val obj: BodyInfo = gson.fromJson(sharedPreferencesManager.getBody(), BodyInfo::class.java)

            setBody(obj.height.toString(), obj.weight.toString(), obj.targetWeight.toString())
        }
    }

    private fun setBody(height: String?, weight: String?, targetWeight: String?){
        this.height.postValue(height!!)
        this.weight.postValue(weight!!)
        this.targetWeight.postValue(targetWeight!!)
    }

    private fun getBody(): BodyInfo = BodyInfo(
        weight = weight.value!!.toString().toDouble(),
        height = height.value!!.toString().toDouble(),
        date = "",
        targetWeight = if(targetWeight.value!!.toString().isEmpty()){ weight.value!!.toString().toDouble() } else { targetWeight.value!!.toString().toDouble() }
    )

    fun postBody() {
        val _height = height.value.toString()
        val _weight = weight.value.toString()
        val _targetWeight = targetWeight.value.toString()

        inputBodyListener?.onInputBodyStarted()

        if(_height.isEmpty()){
            inputBodyListener?.onInputBodyFailure(303, "키를 입력해주세요.")
            return
        }

        if(_weight.isEmpty()){
            inputBodyListener?.onInputBodyFailure(305, "몸무게를 입력해주세요.")
            return
        }

        if (_height.isNotEmpty() && !Pattern.matches(
                "(^\\d+\$)|(^\\d+.\\d{1,2}\$)",
                _height
            )) {
            return
        }

        if (_weight.isNotEmpty() && !Pattern.matches(
                "(^\\d+\$)|(^\\d+.\\d{1,2}\$)",
                _weight
            )) {
            return
        }

        if (_targetWeight.isNotEmpty() && !Pattern.matches(
                "(^\\d+\$)|(^\\d+.\\d{1,2}\$)",
                _targetWeight
            )) {
            return
        }

        Coroutines.main {
            try {
                val response = repository.postBodyInfo(getBody())

                if (response.isSuccess) {
                    inputBodyListener?.onInputBodySuccess(response.message)
                    sharedPreferencesManager.saveBody(getBody())
                    return@main
                }

                inputBodyListener?.onInputBodyFailure(response.code, response.message)
            } catch (e: ApiException) {
                inputBodyListener?.onInputBodyFailure(404, e.message!!)
            } catch (e: Exception) {
                inputBodyListener?.onInputBodyFailure(404, e.message!!)
            }
        }
    }
}