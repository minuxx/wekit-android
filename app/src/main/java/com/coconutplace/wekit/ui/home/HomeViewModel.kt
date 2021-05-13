package com.coconutplace.wekit.ui.home

import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.Auth
import com.coconutplace.wekit.data.entities.BodyGraph
import com.coconutplace.wekit.data.entities.Home
import com.coconutplace.wekit.data.remote.home.listeners.HomeListener
import com.coconutplace.wekit.data.repository.home.HomeRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.coconutplace.wekit.utils.SharedPreferencesManager
import com.github.mikephil.charting.data.Entry
import kotlin.math.roundToInt

class HomeViewModel(private val repository: HomeRepository, private val sharedPreferencesManager: SharedPreferencesManager) : ViewModel()  {
    var homeListener: HomeListener? = null

    init {
//        home()
    }

    fun sendFcmToken(fcmToken: String?){
        homeListener?.onSendFcmTokenStarted()

        Coroutines.main {
            try {
                val homeResponse = repository.sendFcmToken(Auth(jwtToken = null, fcmToken = fcmToken))

                if(homeResponse.isSuccess){
                        homeListener?.onSendFcmTokenSuccess()
                        return@main
                }else{
                    homeListener?.onSendFcmTokenFailure(homeResponse.code, homeResponse.message)
                }
            } catch (e: ApiException) {
                homeListener?.onSendFcmTokenFailure(404, e.message!!)
            } catch (e: Exception){
                homeListener?.onSendFcmTokenFailure(404, e.message!!)
            }
        }
    }
    
    fun home(){
        homeListener?.onHomeStarted()

        Coroutines.main {
            try {
                val homeResponse = repository.home()

                if(homeResponse.isSuccess){
                    homeResponse.home?.let{
                        homeListener?.onHomeSuccess(setChartEntry(it))
                        sharedPreferencesManager.saveNickname(homeResponse.home.nickname)
                        return@main
                    }
                }else{
                    homeListener?.onHomeFailure(homeResponse.code, homeResponse.message)
                }
            } catch (e: ApiException) {
                homeListener?.onHomeFailure(404, e.message!!)
            } catch (e: Exception){
                homeListener?.onHomeFailure(404, e.message!!)
            }
        }
    }

    private fun setChartEntry(home: Home): Home{
        home.bodyGraph = BodyGraph()
        val xData = ArrayList<String>()
        val weightData = ArrayList<Entry>()
        val basalMetabolismData = ArrayList<Entry>()
        val bmiData = ArrayList<Entry>()

        var date: String?

        for((index, data) in home.graphInfo.withIndex()){
            date = "${data.date.substring(5, 7)}.${data.date.substring(8, 10)}"
            xData.add(date)
            weightData.add(Entry(index.toFloat(), data.weight.toFloat()))
            basalMetabolismData.add(Entry(index.toFloat(), calculateBasalMetabolism(home.gender, home.age, data.weight, data.height)))
            bmiData.add(Entry(index.toFloat(), calculateBmi(data.weight, data.height)))
        }

        home.bodyGraph!!.xData = xData
        home.bodyGraph!!.weightData = weightData
        home.bodyGraph!!.basalMetabolismData = basalMetabolismData
        home.bodyGraph!!.bmiData = bmiData

        return home
    }

    private fun calculateBasalMetabolism(gender: String, age: Int, weight: Double, height: Double): Float{
        return when(gender) {
            "M" -> (66 + (13.8 * weight) + (5 * height) - (6.8 * age)).toFloat()
            "F" -> (655 + (9.6 * weight) + (1.8 * height) - (4.7 * age)).toFloat()
            else -> 0.toFloat()
        }
    }

    private fun calculateBmi(weight: Double, height: Double): Float {
        return ((weight / (height * height)) * 10000).roundToInt().toFloat()
    }
}