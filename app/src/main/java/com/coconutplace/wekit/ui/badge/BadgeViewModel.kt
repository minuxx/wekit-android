package com.coconutplace.wekit.ui.badge

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coconutplace.wekit.data.remote.badge.BadgeInfo
import com.coconutplace.wekit.data.repository.badge.BadgeRepository
import com.coconutplace.wekit.utils.SharedPreferencesManager
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.ERROR_TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

class BadgeViewModel(private val repository: BadgeRepository,private val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {

    val liveExistBadgeList: MutableLiveData<ArrayList<BadgeInfo>> by lazy{
        MutableLiveData<ArrayList<BadgeInfo>>().apply {
            postValue(ArrayList<BadgeInfo>())
        }
    }
    val liveNonExistBadgeList: MutableLiveData<ArrayList<BadgeInfo>> by lazy{
        MutableLiveData<ArrayList<BadgeInfo>>().apply {
            postValue(ArrayList<BadgeInfo>())
        }
    }

    fun getNickName():String{
        return sharedPreferencesManager.getNickname()
    }

    fun getBadge(){
        viewModelScope.launch(Dispatchers.IO) {
            try{
                val response = repository.getBadge()
                if(response.isSuccess){
                    Log.e(CHECK_TAG,"getBadge Success")


                    liveExistBadgeList.postValue(response.result!!.existList)
                    liveNonExistBadgeList.postValue(response.result.nonExistList)
                }
                else{
                    Log.e(CHECK_TAG,"getBadge Failed ${response.message}")
                }
            }
            catch (e:Exception){
                Log.e(ERROR_TAG,"getBadge Error $e")
            }
        }
    }
}