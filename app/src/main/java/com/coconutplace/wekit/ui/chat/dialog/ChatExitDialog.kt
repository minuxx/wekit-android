package com.coconutplace.wekit.ui.chat.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.TextView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.chat.listeners.DialogListener

class ChatExitDialog(context: Context) {
    private val mContext = context
    private var dialogListener: DialogListener? = null

    fun callFunction(dialogListener: DialogListener) {
        val dig = Dialog(mContext)
        dig.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dig.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dig.setContentView(R.layout.dialog_exit_room)

        val exitButton = dig.findViewById<TextView>(R.id.chat_dialog_exit_complete_btn)
        val cancelButton = dig.findViewById<TextView>(R.id.chat_dialog_exit_cancel_btn)

        this.dialogListener = dialogListener
        dig.show()

        exitButton.setOnClickListener{
            dialogListener.getBackExitDialog(true)
            dig.dismiss()
        }
        cancelButton.setOnClickListener{
            dialogListener.getBackExitDialog(false)
            dig.dismiss()
        }

    }
}