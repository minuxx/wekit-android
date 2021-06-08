package com.coconutplace.wekit.ui.onboarding

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.coconutplace.wekit.R

class OnBoardingViewPagerAdapter(manager: FragmentManager,
                                 private val context: Context) :
FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
    override fun getCount(): Int {
        return NUM_ITEMS
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> OnBoardingFragment.newInstance(
                R.drawable.img_tutorial_01
            )

            1 -> OnBoardingFragment.newInstance(
                R.drawable.img_tutorial_02
            )

            2 -> OnBoardingFragment.newInstance(
                R.drawable.img_tutorial_03
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