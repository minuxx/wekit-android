package com.coconutplace.wekit.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.User
import com.coconutplace.wekit.data.remote.auth.listeners.LoginListener
import com.coconutplace.wekit.data.repository.auth.AuthRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.coconutplace.wekit.utils.SharedPreferencesManager
import java.util.regex.Pattern


class LoginViewModel(private val repository: AuthRepository, private val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {
    var loginListener: LoginListener? = null

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

    private fun getUser(): User = User(id= id.value.toString(),pw= pw.value.toString())

    fun login() {
        val _id = id.value.toString()
        val _pw = pw.value.toString()

        if(_id.isEmpty() || !Pattern.matches("^[a-zA-Z]+[a-zA-Z0-9]{5,15}\$", _id)){
            return
        }

        if(_pw.isEmpty() || !Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&^])[A-Za-z[0-9]\$@\$!%*#?&]{10,16}\$", _pw)){
            return
        }

        loginListener?.onLoginStarted()

        Coroutines.main {
            try {
                val authResponse = repository.login(getUser())

                if(authResponse.isSuccess){
                    authResponse.auth?.let {
                        loginListener?.onLoginSuccess(it)
                        sharedPreferencesManager.saveJwtToken(authResponse.auth.jwtToken!!)
                        sharedPreferencesManager.saveClientID(id.value.toString())

                        return@main
                    }
                }else{
                    loginListener?.onLoginFailure(authResponse.code, authResponse.message)
                }
            } catch (e: ApiException) {
                loginListener?.onLoginFailure(404, e.message!!)
            } catch (e: Exception){
                loginListener?.onLoginFailure(404, e.message!!)
            }
        }
    }
}