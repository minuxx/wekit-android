package com.coconutplace.wekit.ui.routine

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.interest.listeners.InterestListener
import com.coconutplace.wekit.databinding.ActivityRoutineBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.tutorial.TutorialActivity
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_TUTORIAL_SIGNUP
import com.coconutplace.wekit.utils.GlobalConstant.Companion.MAX_ROUTINE
import com.coconutplace.wekit.utils.GlobalConstant.Companion.MIRACLE_EMPTY
import com.coconutplace.wekit.utils.hide
import com.coconutplace.wekit.utils.show
import org.koin.androidx.viewmodel.ext.android.viewModel

class RoutineActivity : BaseActivity(), InterestListener {
    private lateinit var binding: ActivityRoutineBinding
    private lateinit var adapter: RoutineAdapter
    private val viewModel: RoutineViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_routine)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.interestListener = this

        intent.getStringExtra("miracle")?.let{
            viewModel.miracle = it
        }

        if(viewModel.miracle == MIRACLE_EMPTY){
            finish()
        }

        setSubtitleTextColor()
        initRecyclerview()
    }


    private fun setSubtitleTextColor() {
        val color = getColor(R.color.routine_done_active)
        val spannable = SpannableString(getString(R.string.routine_subtitle))
        spannable.setSpan(ForegroundColorSpan(color),13,24, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.routineSubtitleTv.setText(spannable, TextView.BufferType.SPANNABLE)
    }

    private fun initRecyclerview() {
        val gridLayoutManager = GridLayoutManager(applicationContext, 2)
        gridLayoutManager.orientation = GridLayoutManager.VERTICAL


        binding.routineRecyclerview.layoutManager = gridLayoutManager

        val spanCount = 2
        val spacing = 50
        val includeEdge = true
        binding.routineRecyclerview.addItemDecoration(GridSpacingItemDecoration(spanCount,spacing, includeEdge))

        adapter = RoutineAdapter(itemClick = { view, routine ->
            if(viewModel.selectedRoutines.indexOf(routine.id) == -1){
                if(viewModel.selectedRoutines.size < MAX_ROUTINE){
                    viewModel.selectedRoutines.add(routine.id)
                    view.setBackgroundResource(R.drawable.bg_routine_item_selected)

                    binding.routineDoneBtn.setBackgroundResource(R.drawable.bg_routine_done_active)
                    binding.routineDoneBtn.isClickable = true
                }else{
                    showDialog(getString(R.string.routine_max))
                }
            }else{
                viewModel.selectedRoutines.removeAt(viewModel.selectedRoutines.indexOf(routine.id))
                view.setBackgroundResource(R.drawable.bg_routine_item_unselected)

                if(viewModel.selectedRoutines.size == 0) {
                    binding.routineDoneBtn.setBackgroundResource(R.drawable.bg_routine_done_inactive)
                    binding.routineDoneBtn.isClickable = false
                }
            }
        })

        binding.routineRecyclerview.adapter = adapter
        adapter.addItems(viewModel.routines)
    }


    private fun onStartTutorialActivity() {
        val intent = Intent(this@RoutineActivity, TutorialActivity::class.java)
        intent.putExtra("flag", FLAG_TUTORIAL_SIGNUP)
        startActivity(intent)
        finish()
    }

    override fun onInterestStarted() {
        binding.routineLoading.show()

    }

    override fun onInterestSuccess() {
        binding.routineLoading.hide()
        onStartTutorialActivity()
    }

    override fun onInterestFailure(code: Int, message: String) {
        binding.routineLoading.hide()

        binding.routineDoneBtn.isClickable = true

//        if(adapter..isEmpty()) {
//            showDialog(getString(R.string.routine_select_interest))
//            return
//        }
//
        when(code) {
            303, 305, 306 -> showDialog(message)
            else -> showDialog(getString(R.string.network_error))
        }

    }
}