package com.coconutplace.wekit.ui.chat.dialog

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.UserInfo
import com.coconutplace.wekit.data.remote.chat.listeners.DialogListener
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG

class ChatExpelDialog(context: Context,nickname:String?) {
    private val mContext = context
    private var dialogListener: DialogListener? = null
    private val mNickname = nickname

    fun callFunction(dialogListener: DialogListener, memberList: ArrayList<UserInfo>) {
        val dig = Dialog(mContext)
        dig.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dig.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dig.setContentView(R.layout.dialog_expel_member)

        dig.setCancelable(false)

        val expelMemberSpinner = dig.findViewById<Spinner>(R.id.chat_dialog_expel_member_spinner)
        val expelReasonSpinner = dig.findViewById<Spinner>(R.id.chat_dialog_expel_reason_spinner)
        val expelButton = dig.findViewById<Button>(R.id.chat_dialog_expel_btn)
        val cancelButton = dig.findViewById<ImageButton>(R.id.chat_dialog_expel_cancel_btn)

        this.dialogListener = dialogListener

        val spinnerAdapter: ArrayAdapter<String> = ArrayAdapter<String>(mContext,android.R.layout.simple_spinner_dropdown_item)

        expelMemberSpinner.adapter = spinnerAdapter
        Log.e(CHECK_TAG,"나의 닉네임 : $mNickname")
        for(member in memberList){
            if(member.nickname!=mNickname){
                spinnerAdapter.add(member.nickname)
                Log.e(CHECK_TAG,"추가 : ${member.nickname}")
            }
        }
        spinnerAdapter.notifyDataSetChanged()

        dig.show()

        expelButton.setOnClickListener{
            val member:String = if(expelMemberSpinner.selectedItem==null){
                ""
            }
            else{
                expelMemberSpinner.selectedItem.toString()
            }
            val reason = expelReasonSpinner.selectedItem.toString()
            dialogListener.getBackExpelDialog(member,reason)
            dig.dismiss()
        }
        cancelButton.setOnClickListener {
            dig.dismiss()
        }
    }
}