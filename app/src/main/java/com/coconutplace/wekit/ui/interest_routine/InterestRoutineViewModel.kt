package com.coconutplace.wekit.ui.interest_routine

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.Interest
import com.coconutplace.wekit.data.remote.interest.listeners.InterestListener
import com.coconutplace.wekit.data.repository.interest.InterestRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import java.lang.Exception

class InterestRoutineViewModel(private val repository: InterestRepository) : ViewModel() {

    private lateinit var routineList : ArrayList<Int>
    private lateinit var miracle : String

    var interestListener: InterestListener? = null

    // MVVM 구조 고민필요
    fun getMiracle(miracle: String){
        this.miracle = miracle
    }
    fun getRoutine(routineList: ArrayList<Int>){
        this.routineList = routineList
    }

    // 서버에서 인덱스 1부터 시작하므로 커스텀
    private fun addIndex(routineList: ArrayList<Int>){
        for(i in 0 until routineList.size) {
            routineList[i] = routineList[i].inc()
            Log.e("routineListIndex",this.routineList[i].toString()+" ")
        }
    }

    fun submitInterest() { // 관심사 post

        interestListener?.onInterestStarted()

        Coroutines.main {
            try {
                addIndex(this.routineList)
                val interestResponse = repository.postInterest(Interest(miracle, routineList))

                if(interestResponse.isSuccess){
                    interestListener?.onInterestSuccess()
                    Log.e("postInterestSuccess",interestResponse.message)
                } else {
                    Log.e("postInterestFail",interestResponse.message)
                    interestListener?.onInterestFailure(interestResponse.code, interestResponse.message)
                }
            } catch (e: ApiException) {
                Log.e("postInterestFail",e.message.toString())
                interestListener?.onInterestFailure(404, e.message!!)
            } catch (e: Exception) {
                Log.e("postInterestFail",e.message.toString())
                interestListener?.onInterestFailure(404, e.message!!)
            }
        }

    }

}



