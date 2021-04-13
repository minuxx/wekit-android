package com.coconutplace.wekit.ui.tutorial

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.coconutplace.wekit.R

class TutorialViewPagerAdapter(manager: FragmentManager,
                                private val context: Context) :
FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
    override fun getCount(): Int {
        return NUM_ITEMS
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> TutorialFragment.newInstance(
                R.drawable.img_tutorial_01
            )

            1 -> TutorialFragment.newInstance(
                R.drawable.img_tutorial_02
            )

            2 -> TutorialFragment.newInstance(
                R.drawable.img_tutorial_03
            )

            3 -> TutorialFragment.newInstance(
                R.drawable.img_tutorial_04
            )
            else -> null
        }!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return "<b>Page</b>"
    }

    companion object {
        private const val NUM_ITEMS = 4
    }
}