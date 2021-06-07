package com.coconutplace.wekit.ui.member_gallery

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coconutplace.wekit.data.entities.PhotoPack
import com.coconutplace.wekit.data.remote.gallery.listeners.GalleryListener
import com.coconutplace.wekit.data.repository.gallery.GalleryRepository
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.ERROR_TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class MemberGalleryViewModel(private val repository: GalleryRepository) : ViewModel() {

    private var mUserIdx: Int = -1
    private var mRoomIdx: Int = -1
    private var currentPage = 1
    private var isEnd = false

    private var galleryListener:GalleryListener? = null

    val nickName: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    fun setGalleryListener(galleryListener: GalleryListener){
        this.galleryListener = galleryListener
    }

    fun setIndex(userIdx:Int,roomIdx:Int,nickName:String){
        mUserIdx = userIdx
        mRoomIdx = roomIdx
        this.nickName.postValue(nickName+"님의 사진첩")
    }

    fun setPage(page:Int){
        currentPage = page
    }
    fun setIsDone(isEnd:Boolean){
        this.isEnd = isEnd
    }

    fun getMemberGallery(){
        if(isEnd){
            return
        }
        viewModelScope.launch(Dispatchers.IO){
            try {
                val galleryResponse = repository.getGallery(mUserIdx,mRoomIdx,currentPage)
                currentPage++

                if(galleryResponse.isSuccess){
                    Log.e(CHECK_TAG,"gallery success")
                    val map = galleryResponse.result

                    for (date in map) {
                        if(date.key==null||date.key=="null"||date.value.size == 0){
                            Log.e(CHECK_TAG, "galleryResponse가 빈 값 입니다")
                            currentPage--
                            isEnd = true
                            break
                        }
                        //Log.e(CHECK_TAG, "date : ${date.key}")
                        val urls = ArrayList<String>()
                        for (url in date.value) {
                            //Log.e(CHECK_TAG, "getMemberGallery url : $url")
                            urls.add(url)
                        }
                        galleryListener?.addPhotoPack(PhotoPack(date = date.key,urls = urls))
                    }
                }
                else{
                    Log.e(ERROR_TAG,"gallery failed : ${galleryResponse.message}")
                }
            }catch (e:Exception){
                Log.e(ERROR_TAG,"gallery error $e")
            }
        }
    }


}