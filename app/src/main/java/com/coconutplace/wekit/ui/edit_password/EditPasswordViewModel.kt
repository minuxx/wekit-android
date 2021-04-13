package com.coconutplace.wekit.ui.edit_password

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.User
import com.coconutplace.wekit.data.remote.auth.listeners.EditPasswordListener
import com.coconutplace.wekit.data.remote.auth.listeners.SignUpListener
import com.coconutplace.wekit.data.repository.auth.AuthRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.coconutplace.wekit.utils.SharedPreferencesManager
import com.sendbird.android.SendBird
import java.lang.Exception
import java.util.regex.Pattern

class EditPasswordViewModel(private val repository: AuthRepository,  private val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {
    var editPasswordListener: EditPasswordListener? = null

    val curPw: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val newPw: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val newPwCheck: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    private fun getUser(): User = User(pw = curPw.value.toString(), newPw = newPw.value.toString(), confirmPw = newPwCheck.value.toString())

    fun editPassword() {
        val _curPw = curPw.value.toString()
        val _newPw = newPw.value.toString()
        val _newPwCheck = newPwCheck.value.toString()

        if(_curPw.isEmpty() || !Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&])[A-Za-z[0-9]\$@\$!%*#?&]{10,16}\$", _curPw)){
            return
        }

        if(_newPw.isEmpty() || !Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&])[A-Za-z[0-9]\$@\$!%*#?&]{10,16}\$", _newPw)){
            return
        }

        if(_newPwCheck.isEmpty() || !Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&])[A-Za-z[0-9]\$@\$!%*#?&]{10,16}\$", _newPwCheck)){
            return
        }

        if(_newPw != _newPwCheck){
            return
        }

        editPasswordListener?.onEditPasswordStarted()

        Coroutines.main {
            try {
                val response = repository.patchPassword(getUser())

                if(response.isSuccess){
                    editPasswordListener?.onEditPasswordSuccess()
                    sharedPreferencesManager.saveJwtToken(response.auth!!.jwtToken!!)
                    return@main
                }

                editPasswordListener?.onEditPasswordFailure(response.code, response.message)
            } catch (e: ApiException) {
                editPasswordListener?.onEditPasswordFailure(404, e.message!!)
            } catch (e: Exception){
                editPasswordListener?.onEditPasswordFailure(404, e.message!!)
            }
        }
    }
}