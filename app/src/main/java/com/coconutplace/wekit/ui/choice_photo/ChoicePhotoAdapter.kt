package com.coconutplace.wekit.ui.choice_photo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Photo
import com.coconutplace.wekit.databinding.ItemChoicePhotoBinding
import com.coconutplace.wekit.utils.GlobalConstant.Companion.ITEM_TYPE_ADD_PHOTO
import java.util.*


class ChoicePhotoAdapter(private val context: Context, private val itemClick: (Int) -> Unit): RecyclerView.Adapter<ChoicePhotoAdapter.ViewHolder>() {
    var items: ArrayList<Photo> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val photoBinding: ItemChoicePhotoBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_choice_photo, parent, false)

        return ViewHolder(photoBinding)
    }

    override fun onBindViewHolder(holder: ChoicePhotoAdapter.ViewHolder, position: Int) {
        items[position].let{
            if(it.type == ITEM_TYPE_ADD_PHOTO){
                holder.convertItemTypeAdd()
            }

            holder.itemView.setOnClickListener{ itemClick(position) }
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int = items.size

    fun addItems(items: ArrayList<Photo>){
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun addItem(item: Photo){
        this.items.add(item)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int){
        this.items.removeAt(position)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemChoicePhotoBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(photo: Photo){
            binding.photo = photo
        }

        fun convertItemTypeAdd(){
            binding.itemChoicePhotoRootLayout.background = getDrawable(context, R.drawable.bg_item_choice_photo_add)
            binding.itemChoicePhotoIv.setImageDrawable(null)
            binding.itemChoicePhotoAddIv.visibility = View.VISIBLE
            binding.itemChoicePhotoCancelIv.visibility = View.GONE
        }
    }
}