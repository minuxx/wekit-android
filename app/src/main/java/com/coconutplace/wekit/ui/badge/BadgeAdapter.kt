package com.coconutplace.wekit.ui.badge

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.badge.BadgeInfo
import com.coconutplace.wekit.ui.chat.dialog.ChatBadgeDialog
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou


class BadgeAdapter(context: Context) : BaseAdapter() {

    private val mContext = context
    private val badgeList = ArrayList<BadgeInfo>()
    private val inf :LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        if(convertView!=null){
            return convertView
        }
        val view:View = inf.inflate(R.layout.item_badge,parent,false)
        val badgeImg = view.findViewById<ImageView>(R.id.badge_iv)
        val backgroundCardView = view.findViewById<CardView>(R.id.badge_cardview)
        val url = badgeList[position].badgeImageUrl
        val backColor = badgeList[position].backgroundColor

        if(position<badgeList.size){
            Log.e(CHECK_TAG,"Badge getView url : ${badgeList[position]}")

            if(url.contains(".svg")){
                GlideToVectorYou
                    .justLoadImage(mContext as Activity,Uri.parse(url),badgeImg)
            }
            else{
                Glide.with(mContext).load(url).into(badgeImg)
            }
            backgroundCardView.setCardBackgroundColor(Color.parseColor(backColor))
            badgeImg.setOnClickListener {
                val badgeDialog = ChatBadgeDialog(mContext)
                badgeDialog.callFunction(badgeList[position].badgeName,url,"",backColor)
            }
        }

        return view
    }
    fun clear(){
        badgeList.clear()
    }

    fun addItem(badge:BadgeInfo){
        badgeList.add(badge)
        Log.e(CHECK_TAG,badge.badgeName)
    }

    override fun getItem(position: Int): Any {
        return badgeList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return badgeList.size
    }
}