package com.coconutplace.wekit.ui.body_graph

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.coconutplace.wekit.data.entities.BodyGraph
import com.coconutplace.wekit.utils.GlobalConstant

class BodyGraphAdapter(
    fa: FragmentActivity,
    private val recentBodyGraph: BodyGraph,
    private val totalBodyGraph: BodyGraph
) : FragmentStateAdapter(fa) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RecentBodyGraphFragment(recentBodyGraph)
            1 -> TotalBodyGraphFragment(totalBodyGraph)
            else -> RecentBodyGraphFragment(recentBodyGraph)
        }
    }

    override fun getItemCount(): Int = GlobalConstant.BODY_GRAPH_FRAGMENT_CNT
}