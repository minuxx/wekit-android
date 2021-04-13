package com.coconutplace.wekit.ui.input_body

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.body.listeners.InputBodyListener
import com.coconutplace.wekit.databinding.ActivityInputBodyBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.utils.hide
import com.coconutplace.wekit.utils.hideKeyboard
import com.coconutplace.wekit.utils.show
import com.coconutplace.wekit.utils.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.regex.Pattern

class InputBodyActivity : BaseActivity(), InputBodyListener {
    private lateinit var binding: ActivityInputBodyBinding
    private val viewModel: InputBodyViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_input_body)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.inputBodyListener = this

        observeHeight()
        observeWeight()
        observeTargetWeight()

        binding.inputBodyRootLayout.setOnClickListener(this)
        binding.inputBodyCompleteBtn.setOnClickListener(this)
        binding.inputBodyBackBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v){
            binding.inputBodyRootLayout -> binding.inputBodyRootLayout.hideKeyboard()
            binding.inputBodyBackBtn -> finish()
            binding.inputBodyRootLayout -> binding.inputBodyRootLayout.hideKeyboard()
        }
    }

    private fun observeHeight() {
        viewModel.height.observe(this, Observer {
            if (it.isNotEmpty() && !Pattern.matches("(^\\d+\$)|(^\\d+.\\d{1,2}\$)", it)) {
                binding.inputBodyHeightEtLayout.error = getString(R.string.input_body_warning)
            } else {
                binding.inputBodyHeightEtLayout.error = null
            }
        })
    }

    private fun observeWeight() {
        viewModel.weight.observe(this, Observer {
            if (it.isNotEmpty() && !Pattern.matches("(^\\d+\$)|(^\\d+.\\d{1,2}\$)", it)) {
                binding.inputBodyWeightEtLayout.error = getString(R.string.input_body_warning)
            } else {
                binding.inputBodyWeightEtLayout.error = null
            }
        })
    }

    private fun observeTargetWeight() {
        viewModel.targetWeight.observe(this, Observer {
            if (it.isNotEmpty() && !Pattern.matches("(^\\d+\$)|(^\\d+.\\d{1,2}\$)", it)) {
                binding.inputBodyTargetWeightEtLayout.error = getString(R.string.input_body_warning)
            } else {
                binding.inputBodyTargetWeightEtLayout.error = null
            }
        })
    }

    override fun onInputBodyStarted() {
        binding.inputBodyLoading.show()

        binding.inputBodyCompleteBtn.isClickable = false
    }

    override fun onInputBodySuccess(message: String) {
        binding.inputBodyLoading.hide()

        finish()
    }

    override fun onInputBodyFailure(code: Int, message: String) {
        binding.inputBodyLoading.hide()

        when(code){
            303 -> binding.inputBodyHeightEtLayout.error = message
            305 -> binding.inputBodyWeightEtLayout.error = message
            404 -> binding.inputBodyTargetWeightEtLayout.snackbar(getString(R.string.network_error))
        }

        binding.inputBodyCompleteBtn.isClickable = true
    }
}