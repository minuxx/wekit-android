package com.coconutplace.wekit.ui.poll

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.databinding.DataBindingUtil
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.auth.listeners.PollListener
import com.coconutplace.wekit.databinding.ActivityPollBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.tutorial.TutorialActivity
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_TUTORIAL_SIGNUP
import com.coconutplace.wekit.utils.hide
import com.coconutplace.wekit.utils.hideKeyboard
import com.coconutplace.wekit.utils.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.regex.Pattern

class PollActivity: BaseActivity(), PollListener {
    private lateinit var binding: ActivityPollBinding
    private val viewModel: PollViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_poll)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.pollListener = this

        observeHeight()
        observeWeight()
        observeTargetWeight()

        binding.pollRootLayout.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v){
            binding.pollRootLayout -> binding.pollRootLayout.hideKeyboard()
        }
    }


    private fun observeHeight() {
        viewModel.weight.observe(this, Observer {
            if (it.isNotEmpty() && !Pattern.matches("(^\\d+\$)|(^\\d+.\\d{1,2}\$)", it)) {
                binding.pollHeightEtLayout.error = getString(R.string.input_body_warning)
            } else {
                binding.pollHeightEtLayout.error = null
            }
        })
    }

    private fun observeWeight() {
        viewModel.weight.observe(this, Observer {
            if (it.isNotEmpty() && !Pattern.matches("(^\\d+\$)|(^\\d+.\\d{1,2}\$)", it)) {
                binding.pollWeightEtLayout.error = getString(R.string.input_body_warning)
            } else {
                binding.pollWeightEtLayout.error = null
            }
        })
    }

    private fun observeTargetWeight() {
        viewModel.targetWeight.observe(this, Observer {
            if (it.isNotEmpty() && !Pattern.matches("(^\\d+\$)|(^\\d+.\\d{1,2}\$)", it)) {
                binding.pollTargetWeightEtLayout.error = getString(R.string.input_body_warning)
            } else {
                binding.pollTargetWeightEtLayout.error = null
            }
        })
    }

    override fun onPollStarted() {
        binding.pollLoading.hide()
        binding.pollCompleteBtn.isClickable = false
    }

    override fun onPollSuccess(message: String) {
        binding.pollLoading.hide()

        val intent = Intent(this@PollActivity, TutorialActivity::class.java)
        intent.putExtra("flag", FLAG_TUTORIAL_SIGNUP)
        startActivity(intent)
        finish()
    }

    override fun onPollFailure(code: Int, message: String) {
        binding.pollLoading.hide()

        when(code){
            303, 353 -> binding.pollHeightEtLayout.error = message
            305, 355 -> binding.pollWeightEtLayout.error = message
            356 -> binding.pollTargetWeightEtLayout.error = message
            404 -> binding.pollRootLayout.snackbar(getString(R.string.network_error))
            else -> binding.pollRootLayout.snackbar(getString(R.string.network_error))
        }

        binding.pollCompleteBtn.isClickable = true
    }
}