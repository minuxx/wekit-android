package com.coconutplace.wekit.ui.chat.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.TextView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.chat.listeners.DialogListener

class ChatActionTypeDialog(context: Context) {
    private val mContext = context
    private var dialogListener: DialogListener?= null

    fun callFunction(dialogListener: DialogListener){
        val dig = Dialog(mContext)
        dig.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dig.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dig.setContentView(R.layout.dialog_action_type)


        this.dialogListener = dialogListener
        dig.show()

        val reportButton = dig.findViewById<TextView>(R.id.chat_dialog_report_type_btn)
        val expelButton = dig.findViewById<TextView>(R.id.chat_dialog_expel_type_btn)

        reportButton.setOnClickListener{
            dialogListener.getBackActionTypeDialog(1)
            dig.dismiss()
        }
        expelButton.setOnClickListener{
            dialogListener.getBackActionTypeDialog(2)
            dig.dismiss()
        }

    }
}