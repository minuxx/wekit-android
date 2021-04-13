package com.coconutplace.wekit.ui.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.databinding.ItemDiaryBinding
import java.util.ArrayList

class DiaryAdapter(private val itemClick: (Diary) -> Unit): RecyclerView.Adapter<DiaryAdapter.ViewHolder>() {
    var items: ArrayList<Diary> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemDiaryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_diary, parent, false)

        return ViewHolder(binding, itemClick)
    }

    override fun onBindViewHolder(holder: DiaryAdapter.ViewHolder, position: Int) {
        items[position].let{
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int = items.size

    fun addItems(items: ArrayList<Diary>){
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemDiaryBinding, val itemClick: (Diary) -> Unit) : RecyclerView.ViewHolder(binding.root){
        fun bind(diary: Diary){
            binding.diary = diary
            itemView.setOnClickListener{ itemClick(diary) }
        }
    }
}