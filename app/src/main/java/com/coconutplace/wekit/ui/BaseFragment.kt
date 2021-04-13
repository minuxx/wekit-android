package com.coconutplace.wekit.ui

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment

open class BaseFragment: Fragment(), View.OnClickListener, WekitDialog.WekitDialogClickListener {
    override fun onClick(v: View?) {

    }

    fun showDialog(title: String, context: Context){
        val dig = WekitDialog(context)
        dig.listener = this
        dig.show(title)
    }

    override fun onOKClicked() {

    }
}