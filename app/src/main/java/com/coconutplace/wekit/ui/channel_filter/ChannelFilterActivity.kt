package com.coconutplace.wekit.ui.channel_filter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.ChannelFilter
import com.coconutplace.wekit.databinding.ActivityChannelFilterBinding
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChannelFilterActivity: AppCompatActivity() {
    private lateinit var mBinding: ActivityChannelFilterBinding
    private val mChannelFilterViewModel: ChannelFilterViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()
        setupViewModel()
    }

    override fun onResume() {
        super.onResume()
        if(mChannelFilterViewModel.isOngoing.value == false){
            mBinding.channelFilterBeforeProcessButton.background = ContextCompat.getDrawable(this,R.drawable.bg_channel_filter_checked_button)
            mBinding.channelFilterBeforeProcessButton.setTextColor(ContextCompat.getColor(this,R.color.primary))
            mBinding.channelFilterOngoingProcessButton.background = ContextCompat.getDrawable(this,R.drawable.bg_channel_filter_unchecked_button)
            mBinding.channelFilterOngoingProcessButton.setTextColor(ContextCompat.getColor(this,R.color.gray))
        }
        if(mChannelFilterViewModel.isTwoWeek.value==false){
            mBinding.channelFilterTwoWeekRadioButton.setTextColor(ContextCompat.getColor(this,R.color.gray))
            mBinding.channelFilterFourWeekRadioButton.setTextColor(ContextCompat.getColor(this,R.color.primary))
            mBinding.channelFilterFourWeekRadioButton.isChecked = true
        }
        else{
            mBinding.channelFilterTwoWeekRadioButton.setTextColor(ContextCompat.getColor(this,R.color.primary))
            mBinding.channelFilterFourWeekRadioButton.setTextColor(ContextCompat.getColor(this,R.color.gray))
            mBinding.channelFilterTwoWeekRadioButton.isChecked = true
        }

    }

    private fun setupView(){
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_channel_filter)
        mBinding.lifecycleOwner = this
        mBinding.viewModel = mChannelFilterViewModel

        mBinding.channelFilterBackLayout.setOnClickListener {
            finish()
        }


        //하루 인증 횟수
        val authCnt = "${mChannelFilterViewModel.authCount.value!! + 1} 번"
        Log.e(CHECK_TAG,"하루 인증 횟수 : ${authCnt}")
        mBinding.channelFilterCurrentAuthText.text = authCnt
        //모집 인원
        val memberCnt = "${mChannelFilterViewModel.memberCount.value!!} 명"
        mBinding.channelFilterMemberCountText.text = memberCnt



        setupViewListener()
    }

    private fun setupViewModel(){

//        mChannelFilterViewModel.authCount.observe(mBinding.lifecycleOwner!!, Observer<Int>{
//            val str = "${it+1} 번"
//            mBinding.channelFilterCurrentAuthText.text = str
//        })
    }

    private fun setupViewListener(){

        mBinding.channelFilterSeekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val authCount = "${progress+1} 번"
                mChannelFilterViewModel.authCount.value = progress
                mBinding.channelFilterCurrentAuthText.text = authCount
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        mBinding.channelFilterMemberCountPlusButton.setOnClickListener {
            val memberCount = mChannelFilterViewModel.memberCount.value!!
            if(memberCount<6){
                mChannelFilterViewModel.memberCount.value = memberCount+1
                val str = "${mChannelFilterViewModel.memberCount.value} 명"
                mBinding.channelFilterMemberCountText.text = str
            }
        }
        mBinding.channelFilterMemberCountMinusButton.setOnClickListener {
            val memberCount = mChannelFilterViewModel.memberCount.value!!
            if(memberCount>4){
                mChannelFilterViewModel.memberCount.value = memberCount-1
                val str = "${mChannelFilterViewModel.memberCount.value} 명"
                mBinding.channelFilterMemberCountText.text = str
            }
        }

        mBinding.channelFilterTwoWeekRadioButton.setOnClickListener {
            mChannelFilterViewModel.isTwoWeek.value = true
            mBinding.channelFilterTwoWeekRadioButton.setTextColor(ContextCompat.getColor(this,R.color.primary))
            mBinding.channelFilterFourWeekRadioButton.setTextColor(ContextCompat.getColor(this,R.color.gray))
        }

        mBinding.channelFilterFourWeekRadioButton.setOnClickListener {
            mChannelFilterViewModel.isTwoWeek.value = false
            mBinding.channelFilterTwoWeekRadioButton.setTextColor(ContextCompat.getColor(this,R.color.gray))
            mBinding.channelFilterFourWeekRadioButton.setTextColor(ContextCompat.getColor(this,R.color.primary))
        }

        mBinding.channelFilterOngoingProcessButton.setOnClickListener {
            mBinding.channelFilterOngoingProcessButton.background = ContextCompat.getDrawable(this,R.drawable.bg_channel_filter_checked_button)
            mBinding.channelFilterOngoingProcessButton.setTextColor(ContextCompat.getColor(this,R.color.primary))
            mBinding.channelFilterBeforeProcessButton.background = ContextCompat.getDrawable(this,R.drawable.bg_channel_filter_unchecked_button)
            mBinding.channelFilterBeforeProcessButton.setTextColor(ContextCompat.getColor(this,R.color.gray))
            mChannelFilterViewModel.isOngoing.value = true
        }
        mBinding.channelFilterBeforeProcessButton.setOnClickListener {
            mBinding.channelFilterBeforeProcessButton.background = ContextCompat.getDrawable(this,R.drawable.bg_channel_filter_checked_button)
            mBinding.channelFilterBeforeProcessButton.setTextColor(ContextCompat.getColor(this,R.color.primary))
            mBinding.channelFilterOngoingProcessButton.background = ContextCompat.getDrawable(this,R.drawable.bg_channel_filter_unchecked_button)
            mBinding.channelFilterOngoingProcessButton.setTextColor(ContextCompat.getColor(this,R.color.gray))
            mChannelFilterViewModel.isOngoing.value = false
        }

        mBinding.channelFilterApplyButton.setOnClickListener {
            returnFilteredData()
        }
        mBinding.channelFilterResetButton.setOnClickListener {
            resetFilter()
        }
    }

    private fun returnFilteredData(){
        val authCount = mChannelFilterViewModel.authCount.value!!+1
        val isTwoWeek = mChannelFilterViewModel.isTwoWeek.value!!
        val memberCount = mChannelFilterViewModel.memberCount.value!!
        val isOngoing = mChannelFilterViewModel.isOngoing.value!!

        val filterList = ChannelFilter(authCount,isTwoWeek,memberCount,isOngoing)

        val intent = Intent()
        intent.putExtra("filter",filterList)
        setResult(RESULT_OK,intent)
        finish()
    }

    private fun resetFilter(){
        val intent = Intent()
        setResult(RESULT_CANCELED,intent)
        finish()
    }
}