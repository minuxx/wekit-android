package com.coconutplace.wekit.ui.write_diary

import android.net.Uri
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

class WriteDiaryViewModel(private val repository: DiaryRepository) : ViewModel() {
    var writeDiaryListener: WriteDiaryListener? = null
    val photosFromChoicePhoto = ObservableArrayList<Photo>()
    private val imgUrlListFromFirebase: ArrayList<String> = ArrayList()
    private lateinit var date: String
    private val storage = Firebase.storage(FIREBASE_STORAGE_URL)
    private var triedUpload = 0
    private var roomIdx:Int = 0

    val timezone: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply {
            postValue(0)
        }
    }

    val satisfaction: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply {
            postValue(0)
        }
    }

    val memo: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    fun setRoomIdx(idx:Int){
        this.roomIdx = idx
    }

    fun setDate(date: String) {
        this.date = date
    }

//    fun addImgUri(uri: Uri) {
//        imgUrlListFromChoicePhoto.add(uri)
//    }

    fun addPhotos(photos: ArrayList<Photo>){
        this.photosFromChoicePhoto.clear()
        this.photosFromChoicePhoto.addAll(photos)
    }

    fun getPhotoCount(): Int{
        return this.photosFromChoicePhoto.size
    }

    fun getUriCount(): Int{
        return photosFromChoicePhoto.size
    }

    fun getImgList(): ArrayList<String> {
        return imgUrlListFromFirebase
    }

    fun getTriedUploadCount(): Int{
        return triedUpload
    }

    fun clearTiredUpload(){
        triedUpload = 0
    }

    fun uploadToFirebase() {
        if (satisfaction.value!!.toInt() == 0) {
            writeDiaryListener!!.onPostDiaryFailure(308, "음식만족도를 선택해주세요.")
            return
        }

        if (timezone.value!!.toInt() == 0) {
            writeDiaryListener!!.onPostDiaryFailure(310, "식사시간대를 선택해주세요.")
            return
        }

        if(photosFromChoicePhoto.size == 0){
            writeDiaryListener!!.onPostDiaryFailure(306, "사진을 선택해주세요.")
            return
        }

        for(photo in photosFromChoicePhoto){
            val storageRef = storage.reference.child("certification-diary/")

            val uploadTask = storageRef.putFile(Uri.parse(photo.imgUrl))

            uploadTask.addOnProgressListener {
                writeDiaryListener!!.onUploadToFirebaseStarted()
            }
                .addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener { url ->
                        imgUrlListFromFirebase.add(url.toString())
                        triedUpload++
                        writeDiaryListener!!.onUploadToFirebaseSuccess()
                    }
                }
                .addOnFailureListener{
                    triedUpload++
                    writeDiaryListener!!.onUploadToFirebaseFailure()
                }
        }

//        if(imgBitmapList.size == 0){
//            writeDiaryListener!!.onPostDiaryFailure(306, "사진을 선택해주세요.")
//            return
//        }
//
//        for(bitmap in imgBitmapList){
//            val storageRef = storage.reference.child("certification-diary/")
//                .child("${UUID.randomUUID()}.jpg")
//
//            val baos = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//            val data = baos.toByteArray()
//            val uploadTask = storageRef.putBytes(data)
//
//            uploadTask.addOnProgressListener {
//                writeDiaryListener!!.onUploadToFirebaseStarted()
//            }
//                .addOnSuccessListener {
//                    it.storage.downloadUrl.addOnSuccessListener { url ->
//                        imgUrlList.add(url.toString())
//                        triedUpload++
//                        writeDiaryListener!!.onUploadToFirebaseSuccess()
//
//                    }
//                }
//                .addOnFailureListener{
//                    triedUpload++
//                    writeDiaryListener!!.onUploadToFirebaseFailure()
//                }
//        }
    }

    fun postDiary() {
        writeDiaryListener?.onPostDiaryStarted()

        if(imgUrlListFromFirebase.size == 0){
            writeDiaryListener!!.onPostDiaryFailure(404, "네트워크 연결이 원활하지 않습니다.")
            return
        }

        val diary = Diary(
            roomIdx = roomIdx,
            diaryIdx = null,
            date = date,
            timezone = timezone.value!!.toInt(),
            time = null,
            satisfaction = satisfaction.value!!.toInt(),
            imageList = imgUrlListFromFirebase,
            thumbnailUrl = null,
            memo = memo.value.toString()
        )

        Coroutines.main {
            try {
                val response = repository.postDiary(diary)

                if (response.isSuccess) {
                    if(response.result==null){
                        writeDiaryListener!!.onPostDiarySuccess(response.message)
                    }
                    else{
                        val badgeTitle = response.result.badgeTitle!!
                        val badgeUrl = response.result.badgeUrl!!
                        val badgeExplain = response.result.badgeExplain!!
                        val backgroundColor = response.result.backgroundColor!!
                        writeDiaryListener!!.onPostDiarySuccess(response.message, badgeTitle,badgeUrl,badgeExplain,backgroundColor)
                    }

                } else {
                    writeDiaryListener!!.onPostDiaryFailure(response.code, response.message)
                }
            } catch (e: ApiException) {
                writeDiaryListener!!.onPostDiaryFailure(404, e.message!!)
            } catch (e: Exception) {
                writeDiaryListener!!.onPostDiaryFailure(404, e.message!!)
            }
        }
    }

    fun getDiary(diaryIdx: Int){
        writeDiaryListener!!.onGetDiaryStarted()

        Coroutines.main {
            try {
                val response = repository.getDiary(diaryIdx)

                if (response.isSuccess) {
                    writeDiaryListener!!.onGetDiarySuccess(response.result!!.diaryInfo!!)
                } else {
                    writeDiaryListener!!.onGetDiaryFailure(response.code, response.message)
                }
            } catch (e: ApiException) {
                writeDiaryListener!!.onGetDiaryFailure(404, e.message!!)
            } catch (e: Exception) {
                writeDiaryListener!!.onGetDiaryFailure(404, e.message!!)
            }
        }
    }
}