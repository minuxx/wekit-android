package com.coconutplace.wekit.ui.member_gallery

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.PhotoPack
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG

class PhotoPackListAdapter : RecyclerView.Adapter<PhotoPackListAdapter.GalleryViewHolder>(){

    private val photoPackList = ArrayList<PhotoPack>()
    private var mItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener{
        fun onClick(url:String, type:String?)
    }

    fun setItemClickListener(listener: OnItemClickListener){
        mItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery,parent,false)
        return GalleryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return photoPackList.size
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.onBind(photoPackList[position],mItemClickListener)
    }

    fun addLast(photoPack: PhotoPack){
        if(photoPackList.size>0){
            val lastPack = photoPackList.last()
            if(lastPack.date == photoPack.date){
                val combinedPhotoPack =  photoPackList.removeLast()
                combinedPhotoPack.urls.addAll(photoPack.urls)
                photoPackList.add(combinedPhotoPack)
                Log.e(CHECK_TAG,"Combined PhotoPack added")
            }
            else{
                photoPackList.add(photoPack)
                Log.e(CHECK_TAG,"New PhotoPack added")
            }
        }
        else{
            photoPackList.add(photoPack)
            Log.e(CHECK_TAG,"New PhotoPack added")
        }
        notifyDataSetChanged()
    }

//    private fun deleteLast(){
//        photoPackList.removeLast()
//    }

    inner class GalleryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        private val dateTextView = itemView.findViewById<TextView>(R.id.gallery_date_tv)
        private val photoGridView = itemView.findViewById<GridView>(R.id.gallery_gridview)

        fun onBind(photoPack:PhotoPack,listener:OnItemClickListener?){
            Log.e(CHECK_TAG,"PhotoPack onBind")
            val formattedDate = photoPack.date.replace('-','.')
            dateTextView.text = formattedDate
            val gridListAdapter = PhotoListAdapter()
            gridListAdapter.addItem(photoPack,listener)
            photoGridView.adapter = gridListAdapter
        }
    }

}