package com.coconutplace.wekit.ui.write_diary

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager


class PhotoPagerAdapter: PagerAdapter() {
    private val views: ArrayList<View> = ArrayList()

    override fun getItemPosition(`object`: Any): Int{
        val index = views.indexOf(`object`)
        return if (index == -1){
            -1;
        } else {
            index
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any{
        val v = views[position]
        container.addView(v)
        return v
    }

    fun addView(v: View): Int {
        return addView(v, views.size)
    }

    private fun addView(v: View, position: Int): Int {
        views.add(position, v)
        return position
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(views[position])
    }

    private fun removeView(pager: ViewPager, position: Int): Int {
        pager.adapter = null
        views.removeAt(position)
        pager.adapter = this
        return position
    }

    fun removeView(pager: ViewPager, v: View?): Int {
        return removeView(pager, views.indexOf(v))
    }

    fun getView(position: Int): View? {
        return views[position]
    }

    override fun getCount(): Int {
        return views.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}