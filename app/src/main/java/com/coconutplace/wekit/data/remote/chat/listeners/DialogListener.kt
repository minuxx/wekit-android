package com.coconutplace.wekit.data.remote.chat.listeners

interface DialogListener {
    fun getBackImgTypeDialog(type: Int)
    fun getBackActionTypeDialog(type: Int)
    fun getBackExpelDialog(member:String, reason:String)
    fun getBackReportDialog(reason: String)
    fun getBackExitDialog(exitFlag: Boolean)
}