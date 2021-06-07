package com.coconutplace.wekit.ui.select_routine

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.coconutplace.wekit.R
import com.coconutplace.wekit.databinding.ActivitySelectRoutineBinding
import com.coconutplace.wekit.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_select_routine.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectRoutineActivity : BaseActivity() {
    private lateinit var mBinding: ActivitySelectRoutineBinding
    private lateinit var selectRoutineAdapter: SelectRoutineAdapter
    private val mSelectRoutineViewModel: SelectRoutineViewModel by viewModel()
    private lateinit var miracle : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val miracle = intent.getStringExtra("miracle")
        this.miracle = miracle.toString()
        Log.e("miracle Value test", this.miracle)

        setupView()
        setupTextColor()
        initListeners()
        initRecyclerview()
        addRoutines()
        setupInterestAdapter()
    }

    private fun setupView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_routine)
        mBinding.lifecycleOwner = this
        mBinding.viewModel = mSelectRoutineViewModel

        mBinding.selectInterestDoneBtn.requestFocus()
        mBinding.selectInterestDoneBtn.isClickable = false

    }

    private fun initListeners() {
        mBinding.selectChallengeBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupTextColor() {
        val color = getColor(R.color.select_Active)
        val spannable = SpannableString(getString(R.string.selectRoutine_intro02))
        spannable.setSpan(ForegroundColorSpan(color),13,24, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        mBinding.selectChallengeIntro02Tv.setText(spannable, TextView.BufferType.SPANNABLE)
    }

    private fun initRecyclerview() {
        selectInterest_recyclerview.layoutManager = GridLayoutManager(this, 2)
        selectRoutineAdapter = SelectRoutineAdapter(this)
        mBinding.selectInterestRecyclerview.adapter = selectRoutineAdapter
    }

    private fun addRoutines() {
        selectRoutineAdapter.addData()
    }

    private fun setupInterestAdapter() {

        selectRoutineAdapter.setItemClickListener(object :
            SelectRoutineAdapter.OnItemClickListener {
            override fun onClick(position: Int) {
                Log.e("ActivityItemClicked", position.toString())
                if (selectRoutineAdapter.selectedList.isEmpty()) { //선택 항목이 아무것도 없을경우 버튼 비활성화
                    mBinding.selectInterestDoneBtn.setBackgroundResource(R.drawable.bg_select_routine_btn_unactive)
                    mBinding.selectInterestDoneBtn.isClickable = false
                    mSelectRoutineViewModel.getInterest(miracle,selectRoutineAdapter.selectedList)
                } else {
                    mBinding.selectInterestDoneBtn.setBackgroundResource(R.drawable.bg_select_routine_btn)
                    mBinding.selectInterestDoneBtn.isClickable = true
                    mSelectRoutineViewModel.getInterest(miracle,selectRoutineAdapter.selectedList)
                }

            }

        })
    }


}