package com.coconutplace.wekit.ui.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.entities.Notice
import com.coconutplace.wekit.databinding.ItemDiaryBinding
import java.util.ArrayList

class DiaryAdapter(private val itemClick: (Diary) -> Unit): PagingDataAdapter<Diary, DiaryAdapter.ViewHolder>(DiaryComparator) {
//    var items: ArrayList<Diary> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemDiaryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_diary, parent, false)

        return ViewHolder(binding, itemClick)
    }

    override fun onBindViewHolder(holder: DiaryAdapter.ViewHolder, position: Int) {
        holder.bind(diary = getItem(position)!!)
    }

//    override fun getItemCount(): Int = items.size

    companion object {
        private val DiaryComparator = object : DiffUtil.ItemCallback<Diary>() {
            override fun areItemsTheSame(oldItem: Diary, newItem: Diary): Boolean {
                return oldItem.diaryIdx == newItem.diaryIdx
            }

            override fun areContentsTheSame(oldItem: Diary, newItem: Diary): Boolean {
                return oldItem == newItem
            }
        }
    }

//    fun addItems(items: ArrayList<Diary>){
//        this.items.clear()
//        this.items.addAll(items)
//        notifyDataSetChanged()
//    }

    inner class ViewHolder(val binding: ItemDiaryBinding, val itemClick: (Diary) -> Unit) : RecyclerView.ViewHolder(binding.root){
        fun bind(diary: Diary){
            binding.diary = diary
            itemView.setOnClickListener{ itemClick(diary) }
        }
    }
}