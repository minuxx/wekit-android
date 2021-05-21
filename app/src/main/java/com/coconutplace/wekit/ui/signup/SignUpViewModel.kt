package com.coconutplace.wekit.ui.signup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.User
import com.coconutplace.wekit.data.remote.auth.listeners.CertifyEmailListener
import com.coconutplace.wekit.data.remote.auth.listeners.CheckUserListener
import com.coconutplace.wekit.data.remote.auth.listeners.SignUpListener
import com.coconutplace.wekit.data.repository.auth.AuthRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_CERTIFY_EMAIL
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_EDIT_PASSWORD
import com.coconutplace.wekit.utils.SharedPreferencesManager
import java.util.*
import java.util.regex.Pattern

class SignUpViewModel(private val repository: AuthRepository, private val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {
    var signUpListener: SignUpListener? = null
    var certifyEmailListener: CertifyEmailListener? = null
    var checkUserListener: CheckUserListener? = null
    var flag = FLAG_CERTIFY_EMAIL
    var nextFlag = FLAG_EDIT_PASSWORD
    var receivedUser: User? = null
    var timer: Timer? = null
    var second = 180

    val email: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val certificationNumber: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val receivedCertificationNumber: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply {
            postValue(0)
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


    fun getUser(): User = User(id = id.value.toString(), pw = pw.value.toString(), email= email.value.toString(),
                                       nickname = nickname.value.toString(), gender = if(gender.value == true) "M" else "F",
                                       birthday = birthday.value.toString())

    fun signUp() {
        if(receivedUser == null){
            return
        }

        signUpListener?.onSignUpStarted()

        Coroutines.main {
            try {
                val authResponse = repository.signUp(receivedUser!!)

                authResponse.auth?.let {
                    signUpListener?.onSignUpSuccess(authResponse.message)
                    sharedPreferencesManager.saveJwtToken(authResponse.auth.jwtToken!!)
                    sharedPreferencesManager.saveClientID(id.value.toString())
                    sharedPreferencesManager.saveEmail(email.value.toString())

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

    fun checkUser(){
        val _email = email.value.toString()
        val _nickname = nickname.value.toString()
        val _birthday = birthday.value.toString()
        val _id = id.value.toString()
        val _pw = pw.value.toString()
        val _pwCheck = pwCheck.value.toString()

        if(!tncAgree.value!!){
            checkUserListener!!.onCheckUserFailure(340, "이용약관 및 개인정보처리방침에 동의해주세요.")
            return
        }

        if(_email.isEmpty()){
            checkUserListener!!.onCheckUserFailure(307, "이메일 주소를 입력해주세요.")
            return
        }

        if(!Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.0-]+\\.[a-zA-Z]{2,6}\$", _email)){
            checkUserListener!!.onCheckUserFailure(308, "정확한 이메일 주소를 입력해주세요.")
            return
        }

        if(_id.isEmpty()){
            checkUserListener!!.onCheckUserFailure(301, "아이디를 입력해주세요.")
            return
        }

        if(!Pattern.matches("^[a-zA-Z]+[a-zA-Z0-9]{5,15}\$", _id)){
            checkUserListener!!.onCheckUserFailure(302, "아이디는 대/소문자, 숫자를 포함한 6~15자로 입력해주세요.")
            return
        }

        if(_pw.isEmpty()){
            checkUserListener!!.onCheckUserFailure(304, "비밀번호를 입력해주세요.")
            return
        }

        if(!Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&^])[A-Za-z[0-9]\$@\$!%*#?&]{10,16}\$", _pw)){
            checkUserListener!!.onCheckUserFailure(305, "비밀번호는 영문, 숫자, 특수문자를 포함한 10~16자로 입력해주세요.")
            return
        }

        if(_pw != _pwCheck){
            checkUserListener!!.onCheckUserFailure(305, "비밀번호가 일치하지 않습니다.")
            return
        }

        if(_nickname.isEmpty()){
            checkUserListener!!.onCheckUserFailure(309, "닉네임을 입력해주세요.")
            return
        }

        if(!Pattern.matches("^[a-zA-Z0-9가-힣]{1,10}\$", _nickname)){
            checkUserListener!!.onCheckUserFailure(310, "닉네임은 10자 이내의 한글로 입력해주세요.")
            return
        }

        if(_birthday.isEmpty()){
            checkUserListener!!.onCheckUserFailure(314, "생일을 입력해주세요.")
            return
        }

        if(!Pattern.matches("^(19[0-9][0-9]|20\\d{2})-(0[0-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])\$", _birthday)){
            checkUserListener!!.onCheckUserFailure(315, "생일은 yyyy-mm-dd 형식으로 입력해주세요.")
            return
        }

        checkUserListener?.onCheckUserStarted()

        Coroutines.main {
            try {
                val authResponse = repository.checkUser(getUser())

                if(authResponse.isSuccess){
                    checkUserListener?.onCheckUserSuccess(authResponse.message)

                    return@main
                }

                checkUserListener?.onCheckUserFailure(authResponse.code, authResponse.message)
            } catch (e: ApiException) {
                checkUserListener?.onCheckUserFailure(404, e.message!!)
            } catch (e: Exception){
                checkUserListener?.onCheckUserFailure(404, e.message!!)
            }
        }
    }

    fun certifyEmail() {
        val _email = email.value.toString()

        if(_email.isEmpty() || !Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.0-]+\\.[a-zA-Z]{2,6}\$", _email)){
            return
        }

        certifyEmailListener?.onCertifyEmailStarted()

        Coroutines.main {
            try {
                val user: User = getUser()
                if(flag == FLAG_CERTIFY_EMAIL){
                    user.isRegister = "Y"
                }

                val authResponse = repository.certifyEmail(user)

                authResponse.auth?.let {
                    certifyEmailListener?.onCertifyEmailSuccess(authResponse.auth.authenticNum)
                    return@main
                }

                certifyEmailListener?.onCertifyEmailFailure(authResponse.code, authResponse.message)
            } catch (e: ApiException) {
                certifyEmailListener?.onCertifyEmailFailure(404, e.message!!)
            } catch (e: Exception){
                certifyEmailListener?.onCertifyEmailFailure(404, e.message!!)
            }
        }
    }
}