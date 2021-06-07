package com.coconutplace.wekit.ui.opensource

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.License
import com.coconutplace.wekit.data.entities.Notice
import com.coconutplace.wekit.databinding.ItemNoticeBinding
import java.util.*
import kotlin.collections.ArrayList


class OpensourceAdapter(val context: Context): RecyclerView.Adapter<OpensourceAdapter.ViewHolder>() {
    var items: ArrayList<License> = ArrayList()
    private val selectedItems = SparseBooleanArray()
    private var prePos = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_license, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: OpensourceAdapter.ViewHolder, position: Int) {
        items[position].let{
            holder.bind(it, position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun addItems(items: ArrayList<License>){
        this.items.clear()
        this.items.addAll(items)
        for(i in 0 until items.size){
            selectedItems.put(i, true)
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        private var pos = -1;
        private val mLayoutRoot: ConstraintLayout = itemView.findViewById(R.id.item_license_root_layout)
        private val mClLicense: ConstraintLayout = itemView.findViewById(R.id.item_license_cl)
        private val mTvName: TextView = itemView.findViewById(R.id.item_license_name_tv)
        private val mTvUrl: TextView = itemView.findViewById(R.id.item_license_library_url_tv)
        private val mTvCopyright: TextView = itemView.findViewById(R.id.item_license_copyright_tv)
        private val mTvBy: TextView = itemView.findViewById(R.id.item_license_by_tv)
        private val mIvDropdown: ImageView = itemView.findViewById(R.id.item_license_dropdown_iv)

        fun bind(license: License, position: Int){
            this.pos = position
            mTvName.text = license.name
            mTvUrl.text = license.url
            mTvCopyright.text = license.copyright
            mTvBy.text = license.by

            changeVisibility(selectedItems.get(pos))

            mLayoutRoot.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when(v){
                mLayoutRoot -> {
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
               mIvDropdown.setImageResource(R.drawable.icn_dropdown_outline_closed)
               mClLicense.visibility = View.GONE
           } else {
               mIvDropdown.setImageResource(R.drawable.icn_dropdown_outline_open)
               mClLicense.visibility = View.VISIBLE
           }
        }
    }
}