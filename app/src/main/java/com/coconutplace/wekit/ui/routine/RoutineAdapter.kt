package com.coconutplace.wekit.ui.routine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Routine
import com.coconutplace.wekit.databinding.ItemRoutineBinding

class RoutineAdapter(private val itemClick: (View, Routine) -> Unit) : RecyclerView.Adapter<RoutineAdapter.ViewHolder>() {
    private val items = ArrayList<Routine>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemRoutineBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_routine, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items[position].let{ routine ->
            holder.itemView.setOnClickListener{ itemClick(holder.itemView, routine) }
            holder.bind(routine)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItems(items: ArrayList<Routine>){
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemRoutineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(routine: Routine){
            binding.routine = routine
        }
    }
}