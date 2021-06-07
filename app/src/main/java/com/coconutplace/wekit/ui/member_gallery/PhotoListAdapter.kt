package com.coconutplace.wekit.ui.member_gallery

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.PhotoPack
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import java.util.*
import kotlin.collections.ArrayList

class PhotoListAdapter: BaseAdapter() {

    private val mUrls = ArrayList<String>()
    private var mItemClickListener:PhotoPackListAdapter.OnItemClickListener?= null

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        if(convertView!=null){
            return convertView
        }

        val inflater = parent!!.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.item_gallery_photo,parent,false)

        val photo = view.findViewById<ImageView>(R.id.gallery_photo_iv)

        val url = mUrls[position]
        val type: String? = if(url.toLowerCase(Locale.ROOT).contains(".gif")){
            "gif"
        } else{
            null
        }

        photo.setOnClickListener {
            mItemClickListener?.onClick(mUrls[position],type)
        }

        if(position<mUrls.size){
            Log.e(CHECK_TAG,"Gallery getView url : ${mUrls[position]}")
            val myOptions = RequestOptions()
                .dontAnimate()

            Glide.with(parent.context)
                .load(mUrls[position])
                .thumbnail(0.4f)
                .placeholder(R.drawable.img_chat_placeholder)
                .apply(myOptions)
                .centerCrop()
                .into(photo)
        }

        return view
    }

    fun addItem(photoPack: PhotoPack, listener: PhotoPackListAdapter.OnItemClickListener?){
        Log.e(CHECK_TAG,"PhotoPack addITem")
        this.mUrls.addAll(photoPack.urls)
        this.mItemClickListener = listener

        notifyDataSetChanged()
    }

    override fun getItem(position: Int): String {
        return mUrls[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return mUrls.size
    }
}