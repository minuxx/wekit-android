package com.coconutplace.wekit.ui.diary

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.remote.diary.listeners.DiaryListener
import com.coconutplace.wekit.data.repository.diary.DiaryRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines

class DiaryViewModel(private val repository: DiaryRepository) : ViewModel()  {
    var diaryListener: DiaryListener? = null
    val diaries = ObservableArrayList<Diary>()

    fun getDiaries(date: String){
        diaryListener?.onDiaryStarted()

        Coroutines.main {
            try {
                val diaryResponse = repository.getDiaries(date, 1)

                if(diaryResponse.isSuccess){
                    diaryResponse.result?.let {
                        diaryListener?.onDiarySuccess(it.diaryList!!)
                        return@main
                    }
                }else{
                    diaryListener?.onDiaryFailure(diaryResponse.code, diaryResponse.message)
                }
            } catch (e: ApiException) {
                diaryListener?.onDiaryFailure(404, e.message!!)
            } catch (e: Exception){
                diaryListener?.onDiaryFailure(404, e.message!!)
            }
        }
    }

    fun getWrittenDates(date: String){
        diaryListener?.onGetWrittenDatesStarted()

        Coroutines.main {
            try {
                val diaryResponse = repository.getWrittenDates(date)

                if(diaryResponse.isSuccess){
                    diaryResponse.result?.let {
                        diaryListener?.onGetWrittenDatesSuccess(it.dateList!!)
                        return@main
                    }
                }else{
                    diaryListener?.onGetWrittenDatesFailure(diaryResponse.code, diaryResponse.message)
                }
            } catch (e: ApiException) {
                diaryListener?.onGetWrittenDatesFailure(404, e.message!!)
            } catch (e: Exception){
                diaryListener?.onGetWrittenDatesFailure(404, e.message!!)
            }
        }
    }
}