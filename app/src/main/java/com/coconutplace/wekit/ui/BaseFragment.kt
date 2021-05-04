package com.coconutplace.wekit.ui

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment

open class BaseFragment: Fragment(), View.OnClickListener, WekitV1Dialog.WekitDialogClickListener {
    override fun onClick(v: View?) {

    }

    fun showDialog(title: String, context: Context){
        val dig = WekitV1Dialog(context)
        dig.listener = this
        dig.show(title)
    }

    override fun onOKClicked() {

    }
}