package com.coconutplace.wekit.ui.chat.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.net.Uri
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.coconutplace.wekit.R
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou

class ChatBadgeDialog(context: Context) {
    private val mContext = context

    fun callFunction(title:String, url:String, explain:String,backColor:String) {
        val dig = Dialog(mContext)
        dig.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dig.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dig.setContentView(R.layout.dialog_badge)

        dig.setCancelable(false)

        val backgroundDrawable = ContextCompat.getDrawable(mContext,R.drawable.bg_badge_dialog)

        val badgeTitle = dig.findViewById<TextView>(R.id.chat_dialog_badge_title)
        val badgeImg = dig.findViewById<ImageView>(R.id.chat_dialog_badge_img)
        val badgeExplain = dig.findViewById<TextView>(R.id.chat_dialog_badge_explain_text)
        val cancelButton = dig.findViewById<ImageButton>(R.id.chat_dialog_badge_cancel_img)
        val badgeLayout = dig.findViewById<ConstraintLayout>(R.id.chat_dialog_badge_layout)

        badgeTitle.text = title
        badgeExplain.text = explain

        backgroundDrawable?.colorFilter = LightingColorFilter(0,Color.parseColor(backColor))
        badgeLayout.background = backgroundDrawable

        if(backColor=="#F1F1F1"){
            badgeTitle.setTextColor(ContextCompat.getColor(mContext,R.color.gray))
        }

        if(url.contains(".svg")){
            GlideToVectorYou
                .justLoadImage(mContext as Activity,Uri.parse(url),badgeImg)
        }
        else{
            Glide.with(mContext).load(url).into(badgeImg)
        }

        dig.show()

        cancelButton.setOnClickListener{
            dig.dismiss()
        }

    }
}