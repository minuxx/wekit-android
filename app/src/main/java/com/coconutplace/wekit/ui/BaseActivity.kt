package com.coconutplace.wekit.ui

import android.view.View
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity: AppCompatActivity(), View.OnClickListener, WekitDialog.WekitDialogClickListener{
    override fun onClick(v: View?) {

    }

    fun showDialog(title: String){
        val dig = WekitDialog(this)
        dig.listener = this
        dig.show(title)
    }

    override fun onOKClicked() {

    }
}