package com.coconutplace.wekit.ui.badge

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.badge.BadgeInfo
import com.coconutplace.wekit.databinding.ActivityBadgeBinding
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import org.koin.androidx.viewmodel.ext.android.viewModel

class BadgeActivity: AppCompatActivity() {
    private lateinit var mBinding: ActivityBadgeBinding
    private val mViewModel: BadgeViewModel by viewModel()
    private lateinit var myBadgeAdapter: BadgeAdapter
    private lateinit var challengeBadgeAdapter: BadgeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()
        setupViewModel()

        mViewModel.getBadge()
    }

    private fun setupView(){
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_badge)
        mBinding.lifecycleOwner = this
        mBinding.viewModel = mViewModel

        myBadgeAdapter = BadgeAdapter(this)
        mBinding.badgeMyBadgeGridView.adapter = myBadgeAdapter

        challengeBadgeAdapter = BadgeAdapter(this)
        mBinding.badgeChallengeBadgeGridView.adapter = challengeBadgeAdapter

        val myBadgeString = "${mViewModel.getNickName()}님의 배지"
        val challengeBadgeString = "도전 중인 배지"
        mBinding.badgeMyBadgeText.text = myBadgeString
        mBinding.badgeChallengeBadgeText.text = challengeBadgeString

        mBinding.badgeBackButtonFrameLayout.setOnClickListener {
            finish()
        }

    }

    private fun setupViewModel(){
        mViewModel.liveExistBadgeList.observe(this, {
            myBadgeAdapter.clear()
            if(it.size==0){
                mBinding.badgeNoBadgeLayout.visibility = View.VISIBLE
            }
            else{
                mBinding.badgeNoBadgeLayout.visibility = View.GONE
            }

            for(badge in it){
                myBadgeAdapter.addItem(badge)
            }
            myBadgeAdapter.notifyDataSetChanged()

            val myBadgeCount = "총 ${it.size}개"
            mBinding.badgeMyBadgeCountText.text = myBadgeCount
        })

        mViewModel.liveNonExistBadgeList.observe(this,{

            challengeBadgeAdapter.clear()
            for(badge in it){
                challengeBadgeAdapter.addItem(badge)
            }
            challengeBadgeAdapter.notifyDataSetChanged()

            val challengeBadgeCount = "${it.size}개의 배지가 ${mViewModel.getNickName()}님의 도전을 기다리고 있어요!"
            mBinding.badgeChallengeBadgeCountText.text = challengeBadgeCount
        })


    }
}