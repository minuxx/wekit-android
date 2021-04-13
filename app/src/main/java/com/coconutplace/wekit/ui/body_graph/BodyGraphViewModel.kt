package com.coconutplace.wekit.ui.body_graph

import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.BodyGraph
import com.coconutplace.wekit.data.remote.body.BodyResult
import com.coconutplace.wekit.data.remote.body.listeners.BodyGraphListener
import com.coconutplace.wekit.data.repository.body.BodyRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Coroutines
import com.github.mikephil.charting.data.Entry

class BodyGraphViewModel(private val repository: BodyRepository) : ViewModel() {
    var bodyGraphListener: BodyGraphListener? = null

    fun getBodyInfo() {
        bodyGraphListener?.onBodyGraphStarted()

        Coroutines.main {
            try {
                val response = repository.getBodyInfo()

                if(response.isSuccess){
                    bodyGraphListener?.onBodyGraphSuccess(setChartEntry(response.result!!))
                    return@main
                }

                bodyGraphListener?.onBodyGraphFailure(response.code, response.message)
            } catch (e: ApiException) {
                bodyGraphListener?.onBodyGraphFailure(404, e.message!!)
            } catch (e: Exception){
                bodyGraphListener?.onBodyGraphFailure(404, e.message!!)
            }
        }
    }

    private fun setChartEntry(result: BodyResult): BodyGraph {
        val xData = ArrayList<String>()
        val weightData = ArrayList<Entry>()
        val basalMetabolismData = ArrayList<Entry>()
        val bmiData = ArrayList<Entry>()

        var date: String?

        for((index, data) in result.bodyList.withIndex()){
            date = "${data.date.substring(5, 7)}.${data.date.substring(8, 10)}"
            xData.add(date)
            weightData.add(Entry(index.toFloat(), data.weight.toFloat()))
            basalMetabolismData.add(Entry(index.toFloat(), calculateBasalMetabolism(result.gender, result.age, data.weight, data.height)))
            bmiData.add(Entry(index.toFloat(), calculateBmi(data.weight, data.height)))
        }

        val bodyGraph = BodyGraph()

        bodyGraph.xData = xData
        bodyGraph.weightData = weightData
        bodyGraph.basalMetabolismData = basalMetabolismData
        bodyGraph.bmiData = bmiData

        return bodyGraph
    }

    private fun calculateBasalMetabolism(gender: String, age: Int,  weight: Double, height: Double): Float{
        return when(gender) {
            "M" -> (66 + (13.8 * weight) + (5 * height) - (6.8 * age)).toFloat()
            "F" -> (655 + (9.6 * weight) + (1.8 * height) - (4.7 * age)).toFloat()
            else -> 0.toFloat()
        }
    }

    private fun calculateBmi(weight: Double, height: Double): Float{
        return ((weight / (height * height)) * 10000).toFloat()
    }
}