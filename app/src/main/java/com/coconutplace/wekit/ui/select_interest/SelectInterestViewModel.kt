package com.coconutplace.wekit.ui.select_interest

import android.util.Log
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.Interest
import com.coconutplace.wekit.data.entities.Notice
import com.coconutplace.wekit.data.entities.PhotoPack
import com.coconutplace.wekit.data.remote.gallery.listeners.GalleryListener
import com.coconutplace.wekit.data.remote.home.listeners.HomeListener
import com.coconutplace.wekit.data.remote.interest.listeners.InterestListener
import com.coconutplace.wekit.data.remote.notice.NoticeListener
import com.coconutplace.wekit.data.repository.gallery.GalleryRepository
import com.coconutplace.wekit.data.repository.home.HomeRepository
import com.coconutplace.wekit.data.repository.interest.InterestRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.coconutplace.wekit.utils.SharedPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class SelectInterestViewModel(private val repository: InterestRepository) : ViewModel() {

    var interestListener: InterestListener? = null

    private fun getInterest(): Interest = Interest(miracle = "M",  routine= "클린한 식단") // temp interest set


    fun submitInterest() { // 관심사 post
        Coroutines.main {
            try {
                val interestResponse = repository.postInterest(getInterest())

                if(interestResponse.isSuccess){
                    Log.e("postInterestSuccess",interestResponse.message)
                } else {
                    interestListener?.onInterestFailure(interestResponse.code, interestResponse.message)
                }
            } catch (e: ApiException) {
                interestListener?.onInterestFailure(404, e.message!!)
            } catch (e: Exception) {
                interestListener?.onInterestFailure(404, e.message!!)
            }
        }

    }

}



