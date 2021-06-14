package com.coconutplace.wekit.ui.routine

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.interest.listeners.InterestListener
import com.coconutplace.wekit.databinding.ActivityInterestRoutineBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.tutorial.TutorialActivity
import com.coconutplace.wekit.utils.GlobalConstant
import com.coconutplace.wekit.utils.hide
import com.coconutplace.wekit.utils.show
import kotlinx.android.synthetic.main.activity_interest_routine.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class InterestRoutineActivity : BaseActivity(), InterestListener {
    private lateinit var mBinding: ActivityInterestRoutineBinding
    private lateinit var interestRoutineAdapter: InterestRoutineAdapter
    private val mInterestRoutineViewModel: InterestRoutineViewModel by viewModel()
    private lateinit var miracle : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setupValue()
        setupView()
        setupTextColor()
        initListeners()
        initRecyclerview()
        addRoutines()
        setupInterestAdapter()
    }

    private fun setupValue() {
        val miracle = intent.getStringExtra("miracle")
        this.miracle = miracle.toString()
        mInterestRoutineViewModel.getMiracle(this.miracle)
        Log.e("miracle Value test", this.miracle)
    }

    private fun setupView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_interest_routine)
        mBinding.lifecycleOwner = this
        mBinding.viewModel = mInterestRoutineViewModel

        mInterestRoutineViewModel.interestListener = this

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
        interestRoutineAdapter = InterestRoutineAdapter(this)
        mBinding.selectInterestRecyclerview.adapter = interestRoutineAdapter
    }

    private fun addRoutines() {
        interestRoutineAdapter.addData()
    }

    private fun setupInterestAdapter() {

        interestRoutineAdapter.setItemClickListener(object :
            InterestRoutineAdapter.OnItemClickListener {
            override fun onClick(position: Int) {
                Log.e("ActivityItemClicked", position.toString())
                if(interestRoutineAdapter.isMax) {
                    showDialog(getString(R.string.selectRoutine_error02))
                    return
                }
                if (interestRoutineAdapter.selectedList.isEmpty()) { //선택 항목이 아무것도 없을경우 버튼 비활성화
                    mBinding.selectInterestDoneBtn.setBackgroundResource(R.drawable.bg_interest_routine_btn_unactive)
                    mBinding.selectInterestDoneBtn.isClickable = false
                    mInterestRoutineViewModel.getRoutine(interestRoutineAdapter.selectedList)
                } else {
                    mBinding.selectInterestDoneBtn.setBackgroundResource(R.drawable.bg_interest_routine_btn)
                    mBinding.selectInterestDoneBtn.isClickable = true
                    mInterestRoutineViewModel.getRoutine(interestRoutineAdapter.selectedList)
                }

            }

        })
    }

    private fun onStartTutorialActivity() {
        val intent = Intent(this@InterestRoutineActivity, TutorialActivity::class.java)
        intent.putExtra("flag", GlobalConstant.FLAG_TUTORIAL_SIGNUP)
        startActivity(intent)
        finish()
    }

    override fun onInterestStarted() {
        mBinding.routineLoading.show()
        mBinding.selectInterestDoneBtn.isClickable = false
    }

    override fun onInterestSuccess() {
        mBinding.routineLoading.hide()
        onStartTutorialActivity()
    }

    override fun onInterestFailure(code: Int, message: String) {
        mBinding.routineLoading.hide()
        mBinding.selectInterestDoneBtn.isClickable = true
        if(interestRoutineAdapter.selectedList.isEmpty()) {
            showDialog(getString(R.string.selectRoutine_error))
            return
        }
        when(code) {
            303, 305, 306 -> showDialog(message)
            else -> showDialog(getString(R.string.network_error))
        }

    }


}