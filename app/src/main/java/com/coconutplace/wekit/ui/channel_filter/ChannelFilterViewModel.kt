package com.coconutplace.wekit.ui.channel_filter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChannelFilterViewModel : ViewModel() {

    val authCount: MutableLiveData<Int> by lazy{
        MutableLiveData<Int>().apply {
            value = 0
        }
    }
    val memberCount:MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply {
            value = 4
        }
    }
    val isOngoing:MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            value = false
        }
    }

    val isTwoWeek:MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            value = true
        }
    }

}