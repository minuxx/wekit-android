package com.coconutplace.wekit.ui.notice

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Notice
import com.sendbird.android.constant.StringSet.data
import java.util.*


class NoticeAdapter(private val context: Context): PagingDataAdapter<Notice, NoticeAdapter.ViewHolder>(NoticeComparator) {
//    var items: ArrayList<Notice> = ArrayList() // paging 3에서 사용하지 않는다!
    private val selectedItems = SparseBooleanArray()
    private var prePos = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_notice, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeAdapter.ViewHolder, position: Int) {
        holder.bind(notice = getItem(position)!!, position)
    }

//    override fun getItemCount(): Int = items.size //paing3 lib를 사용할 때 이 녀석을 사용하면 안된다!

//    fun addItems(items: ArrayList<Notice>){
//        this.items.clear()
//        this.items.addAll(items)
//
//        for(i in 0 until items.size){
//            selectedItems.put(i, true)
//        }
//
//        notifyDataSetChanged()
//    }

    companion object {
        private val NoticeComparator = object : DiffUtil.ItemCallback<Notice>() {
            override fun areItemsTheSame(oldItem: Notice, newItem: Notice): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Notice, newItem: Notice): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener{
        private var pos = -1
        val rootView: ConstraintLayout = view.findViewById(R.id.item_notice_root_layout)
        val topBorderView: View = view.findViewById(R.id.item_notice_top_border_view)
        val categoryTv: TextView = view.findViewById(R.id.item_notice_category_tv)
        val titleTv: TextView = view.findViewById(R.id.item_notice_title_tv)
        val dateTv: TextView = view.findViewById(R.id.item_notice_date_tv)
        val contentTv: TextView = view.findViewById(R.id.item_notice_content_tv)
        val dropdownIv: ImageView = view.findViewById(R.id.item_notice_dropdown_iv)

        fun bind(notice: Notice, position: Int){
            this.pos = position

            categoryTv.text = notice.noticeCategory
            titleTv.text = notice.noticeTitle
            dateTv.text = notice.date
            contentTv.text = notice.noticeContent

            changeVisibility(selectedItems.get(pos))

            rootView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when(v){
                rootView -> {
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
           if(!isExpanded){
               topBorderView.visibility = View.GONE
               dropdownIv.setImageResource(R.drawable.icn_dropdown_outline_closed)
               contentTv.visibility = View.GONE
           } else {
               topBorderView.visibility = View.VISIBLE
               dropdownIv.setImageResource(R.drawable.icn_dropdown_outline_open)
               contentTv.visibility = View.VISIBLE
           }
        }
    }
}