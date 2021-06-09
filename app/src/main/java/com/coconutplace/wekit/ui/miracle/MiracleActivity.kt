package com.coconutplace.wekit.ui.miracle

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.coconutplace.wekit.R
import com.coconutplace.wekit.databinding.ActivityMiracleBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.interest_routine.InterestRoutineActivity
import com.coconutplace.wekit.utils.GlobalConstant.Companion.MIRACLE_EMPTY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.MIRACLE_MORNING
import com.coconutplace.wekit.utils.GlobalConstant.Companion.MIRACLE_NIGHT
import com.coconutplace.wekit.utils.GlobalConstant.Companion.MIRACLE_ROUTINE
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.regex.Pattern

class MiracleActivity : BaseActivity() {
    private lateinit var binding: ActivityMiracleBinding
    private val viewModel: MiracleViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_miracle)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        initListener()

        binding.miracleMorningCv.setOnClickListener(this)
        binding.miracleNightCv.setOnClickListener(this)
        binding.miracleRoutineCv.setOnClickListener(this)
        binding.miracleNextBtn.setOnClickListener(this)

        observeInterest()
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when(v){
            binding.miracleMorningCv -> setInterest(MIRACLE_MORNING)
            binding.miracleNightCv -> setInterest(MIRACLE_NIGHT)
            binding.miracleRoutineCv -> setInterest(MIRACLE_ROUTINE)
            binding.miracleNextBtn ->
        }
    }

    private fun setInterest(interest: String){
        viewModel.interest.postValue(interest)
    }

    private fun observeInterest(){
        viewModel.interest.observe(this, Observer {
            when(it){
                MIRACLE_EMPTY -> binding.miracleMorningCv.setBackgroundResource(R.drawable.bg_miracle_item)
                MIRACLE_MORNING -> activateItem(binding.miracleMorningCv)
                MIRACLE_NIGHT -> activateItem(binding.miracleNightCv)
                MIRACLE_ROUTINE -> activateItem(binding.miracleRoutineCv)
            }
        })
    }

    private fun resetBackground(){
        binding.miracleMorningCv.setBackgroundResource(0)
        binding.miracleNightCv.setBackgroundResource(0)
        binding.miracleRoutineCv.setBackgroundResource(0)
    }

    private fun activateItem(view: CardView){
        resetBackground()
        view.setBackgroundResource(R.drawable.bg_miracle_item)
    }

    private fun startRoutineActivity(){
        val interest = viewModel.interest.value.toString()

        if(interest.isEmpty()){
            return
        }

        val intent = Intent(this@MiracleActivity, InterestRoutineActivity::class.java)
        intent.putExtra("miracle", interest)
        startActivity(intent)
    }

    private fun initListener() {
       binding.selectMiracleNextBtn.setOnClickListener {

       }

        binding.selectMiracleBackBtn.setOnClickListener {
            finish()
        }
    }
}