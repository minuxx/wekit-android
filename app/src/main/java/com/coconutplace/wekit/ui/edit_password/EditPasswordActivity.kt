package com.coconutplace.wekit.ui.edit_password

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.auth.listeners.EditPasswordListener
import com.coconutplace.wekit.databinding.ActivityEditPasswordBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.utils.hide
import com.coconutplace.wekit.utils.hideKeyboard
import com.coconutplace.wekit.utils.show
import com.coconutplace.wekit.utils.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.regex.Pattern

class EditPasswordActivity : BaseActivity(), EditPasswordListener {
    private lateinit var binding: ActivityEditPasswordBinding
    private val viewModel: EditPasswordViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_password)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.editPasswordListener = this

        observeCurPw()
        observeNewPw()
        observeNewPwCheck()

        binding.editPasswordRootLayout.setOnClickListener(this)
        binding.editPasswordBackBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v){
            binding.editPasswordRootLayout -> binding.editPasswordRootLayout.hideKeyboard()
            binding.editPasswordBackBtn -> finish()
        }
    }

    private fun observeCurPw() {
        viewModel.curPw.observe(this, Observer {
            if (it.isNotEmpty() && !Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&])[A-Za-z[0-9]\$@\$!%*#?&]{10,16}\$", it)) {
                binding.editPasswordCurLayout.error = getString(R.string.password_validation)
            } else {
                binding.editPasswordCurLayout.error = null
            }
        })
    }

    private fun observeNewPw() {
        viewModel.newPw.observe(this, Observer {
            if (it.isNotEmpty() && !Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&])[A-Za-z[0-9]\$@\$!%*#?&]{10,16}\$", it)) {
                binding.editPasswordNewEtLayout.error = getString(R.string.password_validation)
            } else {
                binding.editPasswordNewEtLayout.error = null
            }
        })
    }
    private fun observeNewPwCheck() {
        viewModel.newPwCheck.observe(this, Observer {
            if (it.isNotEmpty() && it != viewModel.newPw.value.toString()) {
                binding.editPasswordNewCheckEtLayout.error = getString(R.string.password_check)
            } else {
                binding.editPasswordNewCheckEtLayout.error = null
            }
        })
    }

    override fun onEditPasswordStarted() {
        binding.editPasswordLoading.show()
        binding.editPasswordCompleteBtn.isClickable = false
    }

    override fun onEditPasswordSuccess() {
        binding.editPasswordLoading.hide()

        finish()
    }

    override fun onEditPasswordFailure(code: Int, message: String) {
        binding.editPasswordLoading.hide()

        when(code){
            303, 308 -> binding.editPasswordCurLayout.error = message
            304, 310 -> binding.editPasswordNewEtLayout.error = message
            305, 309 -> binding.editPasswordNewCheckEtLayout.error = message
            404 -> showDialog(getString(R.string.network_error))
        }

        binding.editPasswordCompleteBtn.isClickable = true
    }
}