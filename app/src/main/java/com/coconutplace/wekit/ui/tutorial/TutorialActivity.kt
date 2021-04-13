package com.coconutplace.wekit.ui.tutorial

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.coconutplace.wekit.R
import com.coconutplace.wekit.databinding.ActivityTutorialBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.main.MainActivity
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_TUTORIAL_SIGNUP

class TutorialActivity : BaseActivity() {
    private lateinit var binding: ActivityTutorialBinding
    private lateinit var mViewPager: ViewPager
    private var mFlag: Int = FLAG_TUTORIAL_SIGNUP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_tutorial)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewPager()
        binding.tutorialNextBtn.setOnClickListener(this)

        if (intent.hasExtra("flag")) {
            mFlag = intent.getIntExtra("flag", FLAG_TUTORIAL_SIGNUP)
        }
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when (v) {
            binding.tutorialNextBtn -> {
                if (getItem(1) > mViewPager.childCount) {
                    if (mFlag == FLAG_TUTORIAL_SIGNUP) {
                        startMainActivity()
                    } else {
                        finish()
                    }
                } else {
                    mViewPager.setCurrentItem(getItem(1), true)
                }
            }
        }
    }

    private fun startMainActivity() {
        binding.tutorialNextBtn.isClickable = false

        val intent = Intent(this@TutorialActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        startActivity(intent)
        finish()
    }

    private fun initViewPager() {
        mViewPager = binding.tutorialViewpager
        mViewPager.adapter = TutorialViewPagerAdapter(supportFragmentManager, this)
        mViewPager.offscreenPageLimit = 1

        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> binding.tutorialStepTv.text = getText(R.string.tutorial_step_01)
                    1 -> binding.tutorialStepTv.text = getText(R.string.tutorial_step_02)
                    2 -> {
                        binding.tutorialStepTv.text = getText(R.string.tutorial_step_03)
                        binding.tutorialNextBtn.text = getText(R.string.tutorial_next)
                    }
                    3 -> {
                        binding.tutorialStepTv.text = getText(R.string.tutorial_step_04)
                        binding.tutorialNextBtn.text = getText(R.string.tutorial_start)
                    }
                }
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
            override fun onPageScrollStateChanged(arg0: Int) {}
        })
    }

    private fun getItem(i: Int): Int {
        return mViewPager.currentItem + i
    }
}