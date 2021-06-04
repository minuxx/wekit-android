package com.coconutplace.wekit.ui.select_interest

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.coconutplace.wekit.R
import com.coconutplace.wekit.databinding.ActivitySelectInterestBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.sendbird.android.UserMessage
import kotlinx.android.synthetic.main.activity_member_gallery.*
import kotlinx.android.synthetic.main.activity_select_interest.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class SelectInterestActivity : BaseActivity() {
    private lateinit var mBinding: ActivitySelectInterestBinding
    private lateinit var selectInterestAdapter: SelectInterestAdapter
    private val mSelectInterestViewModel: SelectInterestViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()
        initRecyclerview()
        tempInterest()
        setupInterestAdapter()
    }

    private fun setupView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_interest)
        mBinding.lifecycleOwner = this
        mBinding.viewModel = mSelectInterestViewModel

        mBinding.selectInterestDoneBtn.requestFocus()
        mBinding.selectInterestDoneBtn.isClickable = false

    }

    private fun initRecyclerview() {
        selectInterest_recyclerview.layoutManager = GridLayoutManager(this, 2)
        selectInterestAdapter = SelectInterestAdapter(this)
        mBinding.selectInterestRecyclerview.adapter = selectInterestAdapter
    }

    private fun tempInterest() {
        selectInterestAdapter.addData()

    }

    private fun setupInterestAdapter() {

        selectInterestAdapter.setItemClickListener(object :
            SelectInterestAdapter.OnItemClickListener {
            override fun onClick(position: Int) {
                Log.e("ActivityItemClicked", position.toString())
                if (selectInterestAdapter.selectedList.isEmpty()) { //선택 항목이 아무것도 없을경우 버튼 비활성화
                    mBinding.selectInterestDoneBtn.setBackgroundResource(R.drawable.bg_select_interest_btn_unactive)
                    mBinding.selectInterestDoneBtn.isClickable = false
                } else {
                    mBinding.selectInterestDoneBtn.setBackgroundResource(R.drawable.bg_select_interest_btn)
                    mBinding.selectInterestDoneBtn.isClickable = true
                }

            }

        })
    }


}