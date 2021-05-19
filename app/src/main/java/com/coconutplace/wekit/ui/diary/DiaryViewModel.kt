package com.coconutplace.wekit.ui.diary

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.remote.diary.listeners.DiaryListener
import com.coconutplace.wekit.data.repository.diary.DiaryRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.prolificinteractive.materialcalendarview.CalendarDay

class DiaryViewModel(private val repository: DiaryRepository) : ViewModel()  {
//    private lateinit var diaryList: LiveData<PagedList<Diary>>
    var diaryListener: DiaryListener? = null
    val diaries = ObservableArrayList<Diary>()
    val writtenDates = ObservableArrayList<CalendarDay>()
    var previousDay: CalendarDay = CalendarDay.today()

    fun getDiaries(date: String){
        diaryListener?.onDiaryStarted()

        Coroutines.main {
            try {
                val diaryResponse = repository.getDiaries(date, 1)

                if(diaryResponse.isSuccess){
                    diaryResponse.result?.let {
//                        diaryList = it.diaryList
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
                        setCalendarDayList(it.dateList!!)
                        diaryListener?.onGetWrittenDatesSuccess()
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

    private fun setCalendarDayList(writtenDates: ArrayList<String>){
        this.writtenDates.clear()

        for(date in writtenDates){
            this.writtenDates.add((CalendarDay.from(date.substring(0, 4).toInt(), date.substring(5, 7).toInt(), date.substring(8, 10).toInt())))
        }
    }
}