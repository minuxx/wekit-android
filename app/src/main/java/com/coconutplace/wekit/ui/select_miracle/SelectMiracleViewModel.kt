package com.coconutplace.wekit.ui.select_miracle

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SelectMiracleViewModel() : ViewModel() {

    companion object {
        const val MIRACLE_INIT = "INIT"
    }

    var miracle = MutableLiveData<String>()
    var isNext = MutableLiveData<Boolean>()

    init {
        miracle.value = MIRACLE_INIT
        isNext.value = false
    }

    fun miracleMorning() {
        this.miracle.value = "M"
        isNext.value = true
    }

    fun miracleNight() {
        this.miracle.value = "N"
        isNext.value = true
    }

    fun varietyRoutine() {
        this.miracle.value = "G"
        isNext.value = true
    }

    fun onNext() {

    }
}