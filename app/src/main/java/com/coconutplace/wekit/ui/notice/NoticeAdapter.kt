package com.coconutplace.wekit.ui.notice

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Notice
import com.coconutplace.wekit.databinding.ItemNoticeBinding
import java.util.*


class NoticeAdapter(): RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {
    var items: ArrayList<Notice> = ArrayList()
    private val selectedItems = SparseBooleanArray();
    private var prePos = -1;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding : ItemNoticeBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_notice,
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeAdapter.ViewHolder, position: Int) {
        items[position].let{
            holder.bind(it, position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun addItems(items: ArrayList<Notice>){
        this.items.clear()
        this.items.addAll(items)
        for(i in 0 until items.size){
            selectedItems.put(i, true)
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemNoticeBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener{
        private var pos = -1;

        fun bind(notice: Notice, position: Int){
            this.pos = position;

            binding.notice = notice
            changeVisibility(selectedItems.get(pos))

            binding.itemNoticeRootLayout.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when(v){
                binding.itemNoticeRootLayout -> {
                    if (selectedItems.get(pos)) {
                        selectedItems.delete(pos)
                    } else {
                        selectedItems.delete(prePos)
                        selectedItems.put(pos, true)
                    }

                    if (prePos != -1) {
                        notifyItemChanged(prePos)
                    }
                    notifyItemChanged(pos)
                }
            }
        }

        private fun changeVisibility(isExpanded: Boolean) {
           if(isExpanded){
               binding.itemNoticeDropdownIv.setImageResource(R.drawable.icn_dropdown_outline_closed)
               binding.itemNoticeContentTv.visibility = View.GONE
           } else {
               binding.itemNoticeDropdownIv.setImageResource(R.drawable.icn_dropdown_outline_open)
               binding.itemNoticeContentTv.visibility = View.VISIBLE
           }
        }
    }
}