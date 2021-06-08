package com.coconutplace.wekit.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.coconutplace.wekit.R
import com.coconutplace.wekit.databinding.ActivityTutorialBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.login.LoginActivity
import com.coconutplace.wekit.ui.main.MainActivity
import com.coconutplace.wekit.ui.splash.SplashViewModel
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_TUTORIAL_SIGNUP
import kotlinx.android.synthetic.main.activity_onboarding.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnBoardingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        initViewPager()
        onboarding_back_btn.setOnClickListener(this)
        onboarding_skip_tv.setOnClickListener(this)
        onboarding_next_tv.setOnClickListener(this)
        onboarding_start_btn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when (v) {
            onboarding_skip_tv -> startLoginActivity()
            onboarding_next_tv -> {
                if (getItem(1) > onboarding_viewpager.childCount) {
                    startLoginActivity()
                } else {
                    onboarding_viewpager.setCurrentItem(getItem(1), true)
                }
            }
            onboarding_back_btn -> {
                if(getItem(1) > 1){
                    onboarding_viewpager.setCurrentItem(getItem(-1), true)
                }
            }
            onboarding_start_btn -> startLoginActivity()
        }
    }

    private fun startLoginActivity(){
        val intent = Intent(this@OnBoardingActivity, LoginActivity::class.java)

        startActivity(intent)
        finish()
    }

    private fun initViewPager() {
        onboarding_viewpager.adapter = OnBoardingViewPagerAdapter(supportFragmentManager, this)
        onboarding_viewpager.offscreenPageLimit = 1

        onboarding_viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        onboarding_back_btn.visibility = View.INVISIBLE
                        onboarding_title_tv.text = getText(R.string.onboarding_title_01)
                        onboarding_subtitle_tv.text = getText(R.string.onboarding_subtitle_01)
                        handlingVisibilityOfViews(false)
                    }
                    1 -> {
                        onboarding_back_btn.visibility = View.VISIBLE
                        onboarding_title_tv.text = getText(R.string.onboarding_title_02)
                        onboarding_subtitle_tv.text = getText(R.string.onboarding_subtitle_02)
                        handlingVisibilityOfViews(false)
                    }
                    2 -> {
                        onboarding_back_btn.visibility = View.VISIBLE
                        onboarding_title_tv.text = getText(R.string.onboarding_title_03)
                        onboarding_subtitle_tv.text = getText(R.string.onboarding_subtitle_03)
                        handlingVisibilityOfViews(true)
                    }
                }
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
            override fun onPageScrollStateChanged(arg0: Int) {}
        })
    }

    private fun handlingVisibilityOfViews(isLastPage : Boolean){
        if(isLastPage){
            onboarding_skip_tv.visibility = View.GONE
            onboarding_next_tv.visibility = View.GONE
            onboarding_indicator.visibility = View.GONE
            onboarding_start_btn.visibility = View.VISIBLE
        }else{
            onboarding_skip_tv.visibility = View.VISIBLE
            onboarding_next_tv.visibility = View.VISIBLE
            onboarding_indicator.visibility = View.VISIBLE
            onboarding_start_btn.visibility = View.GONE
        }
    }

    private fun getItem(i: Int): Int {
        return onboarding_viewpager.currentItem + i
    }
}