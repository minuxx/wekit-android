package com.coconutplace.wekit.ui.profile

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.User
import com.coconutplace.wekit.data.remote.auth.listeners.ProfileListener
import com.coconutplace.wekit.data.repository.auth.AuthRepository
import com.coconutplace.wekit.di.TEST_URL
import com.coconutplace.wekit.di.getBaseUrl
import com.coconutplace.wekit.utils.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sendbird.android.SendBird
import com.sendbird.android.SendBirdException
import com.sendbird.android.SendBirdPushHelper
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern

class ProfileViewModel(
    private val repository: AuthRepository,
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {
    private val storage = Firebase.storage(GlobalConstant.FIREBASE_STORAGE_URL)
    var profileListener: ProfileListener? = null
    var mFlagDeleteUser = false
    val nickname: MutableLiveData<String> = MutableLiveData<String>()
    val profileUrl: MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()

    init {
        nickname.postValue(sharedPreferencesManager.getNickname())
        profileUrl.postValue(null)
    }

    val profileUrlFromFirebase: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue(null)
        }
    }

    val oldPassword: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val newPassword: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val newPasswordCheck: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    private fun getUser(): User = User(nickname = nickname.value.toString(), profileImg = profileUrlFromFirebase.value.toString(), pw= oldPassword.value.toString())

    fun patchProfile() {
        val _nickname = nickname.value.toString()
        val _profileUrlFromFirebase = profileUrlFromFirebase.value.toString()

        profileListener?.onPatchProfileStarted()

        if(_nickname.isEmpty()){
            profileListener?.onPatchProfileFailure(303, "닉네임을 입력해주세요.")
            return
        }

        if(!Pattern.matches("^[a-zA-Z0-9가-힣]{1,10}\$", _nickname)){
            profileListener?.onPatchProfileFailure(353, "닉네임은 한글, 영어, 숫자를 조합한 1~10자리이여야 합니다.")
            return
        }

        Coroutines.main {
            try {
                val authResponse = repository.patchProfile(getUser())

                if (authResponse.isSuccess) {
                    profileListener?.onPatchProfileSuccess()
                    sharedPreferencesManager.saveNickname(_nickname)

//                    if(_profileUrlFromFirebase.isNotEmpty()) {  // 2021.03.17 센드버드 닉네임/프로필 설정 로직 WEKIT 서버로 이동
//                        SendBird.updateCurrentUserInfo(
//                            _nickname,
//                            _profileUrlFromFirebase,
//                            SendBird.UserInfoUpdateHandler {
//                                if (it != null) {
//                                    profileListener?.onPatchProfileFailure(305, "프로필수정에 실패했습니다.")
//                                }
//                            })
//                    }

                    return@main
                } else {
                    profileListener?.onPatchProfileFailure(authResponse.code, authResponse.message)
                }
            } catch (e: ApiException) {
                profileListener?.onPatchProfileFailure(404, e.message!!)
            } catch (e: Exception){
                profileListener?.onPatchProfileFailure(404, e.message!!)
            }
        }
    }

    fun uploadToFirebase() {
        val _nickname = nickname.value.toString()
        val _profileUrl = profileUrl.value.toString()

        if(_nickname.isEmpty()){
            profileListener?.onPatchProfileFailure(303, "닉네임을 입력해주세요.")
            return
        }

        if (profileUrl.value == null) {
            profileListener!!.onPatchProfileFailure(305, "프로필사진을 선택해주세요.")
            return
        }

        val where = if(getBaseUrl() == TEST_URL){ "test" } else { "prod" }

        val storageRef = storage.reference.child("profile")
                                          .child(where)
                                          .child("${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()
        profileUrl.value!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = storageRef.putBytes(data)

        uploadTask.addOnProgressListener {
            profileListener!!.onUploadToFirebaseStarted()
        }
        .addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener { url ->
                profileUrl.postValue(null) // 순서 중요
                profileUrlFromFirebase.postValue(url.toString())
                profileListener!!.onUploadToFirebaseSuccess()
            }
        }

        .addOnFailureListener {
            profileListener!!.onUploadToFirebaseFailure()
        }
    }

    fun deleteUser(){
        val _password = oldPassword.value.toString()

        if(_password.isEmpty()){
            profileListener?.onDeleteUserFailure(303, "비밀번호를 입력해주세요.")
            return
        }

        profileListener?.onDeleteUserStarted()

        Coroutines.main {
            try {
                val authResponse = repository.deleteUser(getUser())

                if (authResponse.isSuccess) {
                    profileListener?.onDeleteUserSuccess()

                    if (mFlagDeleteUser) {
                        sharedPreferencesManager.removeAll()
                        disconnectPushNotification()
                    }

                    return@main
                } else {
                    profileListener?.onDeleteUserFailure(authResponse.code, authResponse.message)
                }
            } catch (e: ApiException) {
                profileListener?.onDeleteUserFailure(404, e.message!!)
            } catch (e: Exception){
                profileListener?.onDeleteUserFailure(404, e.message!!)
            }
        }
    }

    private fun disconnectPushNotification() {
        PushUtil.unregisterPushHandler(object : SendBirdPushHelper.OnPushRequestCompleteListener {
            override fun onComplete(isActive: Boolean, token: String?) {
                Log.e(SharedPreferencesManager.CHECK_TAG,"푸시 등록해제 성공")
            }
            override fun onError(e: SendBirdException) {
                Log.e(SharedPreferencesManager.ERROR_TAG,"푸시 등록해제 실패 $e")
            }
        })
    }
}