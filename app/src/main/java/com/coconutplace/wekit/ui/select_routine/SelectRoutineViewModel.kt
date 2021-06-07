package com.coconutplace.wekit.ui.select_routine

import android.util.Log
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.Interest
import com.coconutplace.wekit.data.remote.interest.listeners.InterestListener
import com.coconutplace.wekit.data.repository.interest.InterestRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import java.lang.Exception

class SelectRoutineViewModel(private val repository: InterestRepository) : ViewModel() {

    private lateinit var routineList : ArrayList<Int>
    private lateinit var miracle : String

    var interestListener: InterestListener? = null

    // view로부터 데이터를 전달받음
    fun getInterest(miracle : String, routineList : ArrayList<Int>) : Interest {
        this.routineList = routineList
        this.miracle = miracle
        return Interest(miracle, routineList)
    }

    // 서버에서 인덱스 1부터 시작하므로 커스텀
    private fun addIndex(routineList: ArrayList<Int>){
        for(i in 0 until routineList.size) {
            routineList[i] = routineList[i].inc()
            Log.e("routineListIndex",this.routineList[i].toString()+" ")
        }
    }

    fun submitInterest() { // 관심사 post
        Coroutines.main {
            try {
                addIndex(this.routineList)
                val interestResponse = repository.postInterest(getInterest(miracle,routineList))

                if(interestResponse.isSuccess){
                    Log.e("postInterestSuccess",interestResponse.message)
                } else {
                    Log.e("postInterestFail",interestResponse.message.toString())
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



