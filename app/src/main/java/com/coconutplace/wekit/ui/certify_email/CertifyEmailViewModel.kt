package com.coconutplace.wekit.ui.certify_email

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.User
import com.coconutplace.wekit.data.remote.auth.listeners.SignUpListener
import com.coconutplace.wekit.data.repository.auth.AuthRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.coconutplace.wekit.utils.SharedPreferencesManager
import com.sendbird.android.SendBird
import java.lang.Exception
import java.util.regex.Pattern

class CertifyEmailViewModel(private val repository: AuthRepository, private val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {
    var signUpListener: SignUpListener? = null

    val email: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val nickname: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val gender: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            postValue(true)
        }
    }

    val id: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val pw: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val pwCheck: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val birthday: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val tncAgree: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            postValue(false)
        }
    }


    private fun getUser(): User = User(id = id.value.toString(), pw = pw.value.toString(), email= email.value.toString(),
                                       nickname = nickname.value.toString(), gender = if(gender.value == true) "M" else "F",
                                       birthday = birthday.value.toString())

    fun signUp() {
        val _email = email.value.toString()
        val _nickname = nickname.value.toString()
        val _birthday = birthday.value.toString()
        val _id = id.value.toString()
        val _pw = pw.value.toString()
        val _pwCheck = pwCheck.value.toString()

        if(!tncAgree.value!!){
            signUpListener?.onSignUpFailure(319, "이용약관 및 개인정보처리방침에 동의해주세요.")
            return
        }

        if(_email.isEmpty() || !Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.0-]+\\.[a-zA-Z]{2,6}\$", _email)){
            return
        }

        if(_id.isEmpty() || !Pattern.matches("^[a-zA-Z]+[a-zA-Z0-9]{5,15}\$", _id)){
            return
        }

        if(_pw.isEmpty() || !Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&^])[A-Za-z[0-9]\$@\$!%*#?&]{10,16}\$", _pw)){
            return
        }

        if(_pw != _pwCheck){
            return
        }

        if(_nickname.isEmpty() || !Pattern.matches("^[a-zA-Z0-9가-힣]{1,10}\$", _nickname)){
            return
        }

        if(_birthday.isEmpty() || !Pattern.matches("^(19[0-9][0-9]|20\\d{2})-(0[0-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])\$", _birthday)){
            return
        }

        signUpListener?.onSignUpStarted()

        Coroutines.main {
            try {
                val authResponse = repository.signUp(getUser())

                authResponse.auth?.let {
                    signUpListener?.onSignUpSuccess(authResponse.message)
                    sharedPreferencesManager.saveJwtToken(authResponse.auth.jwtToken!!)
                    sharedPreferencesManager.saveClientID(id.value.toString())
                    return@main
                }

                signUpListener?.onSignUpFailure(authResponse.code, authResponse.message)
            } catch (e: ApiException) {
                signUpListener?.onSignUpFailure(404, e.message!!)
            } catch (e: Exception){
                signUpListener?.onSignUpFailure(404, e.message!!)
            }
        }
    }
}