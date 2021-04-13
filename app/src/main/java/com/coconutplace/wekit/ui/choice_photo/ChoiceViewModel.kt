package com.coconutplace.wekit.ui.choice_photo

import android.graphics.Bitmap
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.entities.Photo
import com.coconutplace.wekit.data.remote.diary.listeners.WriteDiaryListener
import com.coconutplace.wekit.data.repository.diary.DiaryRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FIREBASE_STORAGE_URL
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class ChoiceViewModel(private val repository: DiaryRepository) : ViewModel() {
    val photos = ObservableArrayList<Photo>()
    private val imgUrlList: ArrayList<String> = ArrayList()
    private val imgBitmapList: ArrayList<Bitmap> = ArrayList()

    fun addImgBitmap(bitmap: Bitmap) {
        imgBitmapList.add(bitmap)
    }

    fun getImgList(): ArrayList<String> {
        return imgUrlList
    }

    fun getBitmapCount(): Int{
        return imgBitmapList.size
    }
}