package com.coconutplace.wekit.ui.miracle

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.utils.GlobalConstant.Companion.MIRACLE_EMPTY

class MiracleViewModel() : ViewModel() {
    var interest = MutableLiveData<String>()

    init {
        this.interest.value = MIRACLE_EMPTY
    }
}