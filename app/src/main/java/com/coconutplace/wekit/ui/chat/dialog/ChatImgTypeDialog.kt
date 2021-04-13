package com.coconutplace.wekit.ui.chat.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.chat.listeners.DialogListener

class ChatImgTypeDialog(context: Context) {

    private val mContext = context
    private var dialogListener: DialogListener ?= null

    fun callFunction(dialogListener:DialogListener){
        val dig = Dialog(mContext)
        dig.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dig.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dig.setContentView(R.layout.dialog_img_type)


        this.dialogListener = dialogListener
        dig.show()

        val authLayout = dig.findViewById<ConstraintLayout>(R.id.chat_dialog_auth_layout)
        val normalLayout = dig.findViewById<ConstraintLayout>(R.id.chat_dialog_normal_layout)


        authLayout.setOnClickListener{
            dig.dismiss()
            dialogListener.getBackImgTypeDialog(1)
        }
        normalLayout.setOnClickListener{
            dig.dismiss()
            dialogListener.getBackImgTypeDialog(2)
        }

    }
}