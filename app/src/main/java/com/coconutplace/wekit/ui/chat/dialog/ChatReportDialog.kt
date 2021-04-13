package com.coconutplace.wekit.ui.chat.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.chat.listeners.DialogListener

class ChatReportDialog(context: Context) {
    private val mContext = context
    private var dialogListener: DialogListener? = null

    fun callFunction(dialogListener: DialogListener) {
        val dig = Dialog(mContext)
        dig.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dig.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dig.setContentView(R.layout.dialog_report_room)

        dig.setCancelable(false)

        val reportReasonSpinner = dig.findViewById<Spinner>(R.id.chat_dialog_report_reason_spinner)
        val reportButton = dig.findViewById<Button>(R.id.chat_dialog_report_btn)
        val cancelButton = dig.findViewById<ImageButton>(R.id.chat_dialog_report_cancel_btn)

        this.dialogListener = dialogListener

        dig.show()

        reportButton.setOnClickListener{
            val reason = reportReasonSpinner.selectedItem.toString()
            dialogListener.getBackReportDialog(reason)
            dig.dismiss()
        }
        cancelButton.setOnClickListener {
            dig.dismiss()
        }
    }
}