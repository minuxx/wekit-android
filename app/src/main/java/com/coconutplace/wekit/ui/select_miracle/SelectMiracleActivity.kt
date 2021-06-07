package com.coconutplace.wekit.ui.select_miracle

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.coconutplace.wekit.R
import com.coconutplace.wekit.databinding.ActivitySelectMiracleBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.select_routine.SelectRoutineActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectMiracleActivity : BaseActivity() {
    private lateinit var mBinding: ActivitySelectMiracleBinding
    private val mSelectMiracleViewModel: SelectMiracleViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()
        initListener()

    }

    private fun setupView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_miracle)
        mBinding.lifecycleOwner = this
        mBinding.viewModel = mSelectMiracleViewModel

    }

    private fun initListener() {
       mBinding.selectMiracleNextBtn.setOnClickListener {
           val intent = Intent(this@SelectMiracleActivity, SelectRoutineActivity::class.java)
           intent.putExtra("miracle",mSelectMiracleViewModel.miracle.value)
           startActivity(intent)
       }

        mBinding.selectMiracleBackBtn.setOnClickListener {
            finish()
        }
    }
}