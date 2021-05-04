package com.coconutplace.wekit.ui

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.coconutplace.wekit.R

class WekitV2Dialog(val context: Context, private var flag: Int) {
    private val dlg = Dialog(context)   //부모 액티비티의 context 가 들어감
    private lateinit var mTvTitle : TextView
    private lateinit var btnOK : TextView
    private lateinit var btnCancel : TextView
    var listener : WekitV2DialogClickListener? = null

    fun show(title : String) {
        dlg.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(R.layout.dialog_wekit_v2)
        dlg.setCancelable(false)

        mTvTitle = dlg.findViewById(R.id.dialog_wekit_v2_title_tv)
        mTvTitle.text = title

        btnOK  = dlg.findViewById(R.id.dialog_wekit_v2_check_tv)
        btnOK.setOnClickListener {
            listener!!.onOKClicked(this.flag)

            dlg.dismiss()
        }

        btnCancel = dlg.findViewById(R.id.dialog_wekit_v2_cancel_tv)
        btnCancel.setOnClickListener {
            dlg.dismiss()
        }

        dlg.show()
    }

    interface WekitV2DialogClickListener {
        fun onOKClicked(flag: Int)
    }
}