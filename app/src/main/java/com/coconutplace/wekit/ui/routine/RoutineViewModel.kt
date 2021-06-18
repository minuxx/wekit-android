package com.coconutplace.wekit.ui.routine

import android.util.Log
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Routine
import com.coconutplace.wekit.data.remote.interest.listeners.InterestListener
import com.coconutplace.wekit.data.repository.interest.InterestRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.coconutplace.wekit.utils.GlobalConstant.Companion.MIRACLE_EMPTY

class RoutineViewModel(private val repository: InterestRepository) : ViewModel() {
    val routines : ArrayList<Routine> = ArrayList()
    val selectedRoutines : ArrayList<Int> = ArrayList()
    var miracle : String = MIRACLE_EMPTY
    var interestListener: InterestListener? = null

    init {
        setRoutines()
    }

    private fun setRoutines(){
        routines.add(Routine(1, R.drawable.ic_water,"물 마시기"))
        routines.add(Routine(2, R.drawable.icn_food,"클린한 식단"))
        routines.add(Routine(3, R.drawable.icn_exercise,"꾸준한 운동"))
        routines.add(Routine(4, R.drawable.icn_reading,"독서"))
        routines.add(Routine(5, R.drawable.icn_language,"외국어"))
        routines.add(Routine(6, R.drawable.icn_test,"고시, 시험"))
        routines.add(Routine(7, R.drawable.icn_diet,"다이어트"))
        routines.add(Routine(8, R.drawable.icn_thinking,"회고"))
        routines.add(Routine(9, R.drawable.icn_writing,"필사"))
        routines.add(Routine(10, R.drawable.icn_plan,"하루 계획"))
    }

    fun submitInterest() { // 관심사 post
        interestListener?.onInterestStarted()

        Coroutines.main {
            try {
//                val interestResponse = repository.postInterest()
//
//                if(interestResponse.isSuccess){
//                    interestListener?.onInterestSuccess()
//                    Log.e("postInterestSuccess",interestResponse.message)
//                } else {
//                    Log.e("postInterestFail",interestResponse.message)
//                    interestListener?.onInterestFailure(interestResponse.code, interestResponse.message)
//                }
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



