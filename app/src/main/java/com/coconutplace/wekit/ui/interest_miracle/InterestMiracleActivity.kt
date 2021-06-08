package com.coconutplace.wekit.ui.interest_miracle

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.coconutplace.wekit.R
import com.coconutplace.wekit.databinding.ActivityInterestMiracleBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.interest_routine.InterestRoutineActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class InterestMiracleActivity : BaseActivity() {
    private lateinit var mBinding: ActivityInterestMiracleBinding
    private val mInterestMiracleViewModel: InterestMiracleViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()
        initListener()

    }

    private fun setupView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_interest_miracle)
        mBinding.lifecycleOwner = this
        mBinding.viewModel = mInterestMiracleViewModel

    }

    private fun initListener() {
       mBinding.selectMiracleNextBtn.setOnClickListener {
           val intent = Intent(this@InterestMiracleActivity, InterestRoutineActivity::class.java)
           intent.putExtra("miracle",mInterestMiracleViewModel.miracle.value)
           startActivity(intent)
       }

        mBinding.selectMiracleBackBtn.setOnClickListener {
            finish()
        }
    }
}