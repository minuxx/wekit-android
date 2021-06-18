package com.coconutplace.wekit.ui.miracle

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.coconutplace.wekit.R
import com.coconutplace.wekit.databinding.ActivityMiracleBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.routine.RoutineActivity
import com.coconutplace.wekit.ui.login.LoginActivity
import com.coconutplace.wekit.utils.GlobalConstant.Companion.MIRACLE_EMPTY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.MIRACLE_MORNING
import com.coconutplace.wekit.utils.GlobalConstant.Companion.MIRACLE_NIGHT
import com.coconutplace.wekit.utils.GlobalConstant.Companion.MIRACLE_ROUTINE
import org.koin.androidx.viewmodel.ext.android.viewModel

class MiracleActivity : BaseActivity() {
    private lateinit var binding: ActivityMiracleBinding
    private val viewModel: MiracleViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_miracle)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.miracleMorningCv.setOnClickListener(this)
        binding.miracleNightCv.setOnClickListener(this)
        binding.miracleRoutineCv.setOnClickListener(this)

        binding.miracleNextBtn.setOnClickListener(this)

        observeInterest()
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when(v){
            binding.miracleBackBtn -> startLoginActivity()
            binding.miracleMorningCv -> setInterest(MIRACLE_MORNING)
            binding.miracleNightCv -> setInterest(MIRACLE_NIGHT)
            binding.miracleRoutineCv -> setInterest(MIRACLE_ROUTINE)
            binding.miracleNextBtn -> startRoutineActivity()
        }
    }

    private fun setInterest(interest: String){
        viewModel.interest.postValue(interest)
    }

    private fun observeInterest(){
        viewModel.interest.observe(this, Observer {
            when(it){
                MIRACLE_EMPTY -> resetBackground()
                MIRACLE_MORNING -> activateItem(binding.miracleMorningCl)
                MIRACLE_NIGHT -> activateItem(binding.miracleNightCl)
                MIRACLE_ROUTINE -> activateItem(binding.miracleRoutineCl)
            }
        })
    }

    private fun resetBackground(){
        binding.miracleMorningCl.setBackgroundResource(0)
        binding.miracleNightCl.setBackgroundResource(0)
        binding.miracleRoutineCl.setBackgroundResource(0)
    }

    private fun activateItem(view: ConstraintLayout){
        resetBackground()
        view.setBackgroundResource(R.drawable.bg_miracle_selected)
    }

    private fun startRoutineActivity(){
        val interest = viewModel.interest.value.toString()

        if(interest == MIRACLE_EMPTY){
            showDialog(getString(R.string.miracle_select))
            return
        }

        val intent = Intent(this@MiracleActivity, RoutineActivity::class.java)
        intent.putExtra("miracle", interest)
        startActivity(intent)
    }


    private fun startLoginActivity(){
        val intent = Intent(this@MiracleActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        startActivity(intent)
        finish()
    }
}