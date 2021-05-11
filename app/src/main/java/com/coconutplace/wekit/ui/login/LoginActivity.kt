package com.coconutplace.wekit.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Auth
import com.coconutplace.wekit.data.remote.auth.listeners.LoginListener
import com.coconutplace.wekit.databinding.ActivityLoginBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.channel.BackPressListener
import com.coconutplace.wekit.ui.main.MainActivity
import com.coconutplace.wekit.ui.signup.SignUpActivity
import com.coconutplace.wekit.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.schedule

class LoginActivity : BaseActivity(), LoginListener {
    private lateinit var binding : ActivityLoginBinding
    private val viewModel : LoginViewModel by viewModel()
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_login, null, false)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.loginListener = this

        observeId()
        observePw()

        binding.loginRootLayout.setOnClickListener(this)
        binding.loginSignupTv.setOnClickListener(this)
    }

    override fun onRestart() {
        super.onRestart()
        binding.loginSignupTv.isClickable = true
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v){
            binding.loginRootLayout -> binding.loginRootLayout.hideKeyboard()
            binding.loginSignupTv -> startSignUpActivity()
        }
    }

    private fun startSignUpActivity(){
        binding.loginSignupTv.isClickable = false
        val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun observeId(){
        viewModel.id.observe(this, Observer {
            if(it.isNotEmpty() && !Pattern.matches("^[a-zA-Z]+[a-zA-Z0-9]{5,15}\$", it)){
                binding.loginIdEtLayout.error = getString(R.string.login_id_validation)
            }else{
                binding.loginIdEtLayout.error = null
              }
        })
    }

    private fun observePw(){
        viewModel.pw.observe(this, Observer {
            if(it.isNotEmpty() && !Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&^])[A-Za-z[0-9]\$@\$!%*#?&]{10,16}\$", it)){
                binding.loginPwEtLayout.error = getString(R.string.login_pw_validation)
            }else{
                binding.loginPwEtLayout.error = null
            }
        })
    }

    private fun startMainActivity(){
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        startActivity(intent)
        finish()
    }

    override fun onLoginStarted() {
        binding.loginLoading.show()
        binding.loginLoginBtn.isClickable = false
    }

    override fun onLoginSuccess(auth: Auth) {
        binding.loginLoading.hide()

        startMainActivity()
    }

    override fun onLoginFailure(code: Int, message: String) {
        binding.loginLoading.hide()

        when(code){
            301, 302, 303, 307 -> binding.loginIdEtLayout.error = message
            304, 305, 306, 308 -> binding.loginPwEtLayout.error = message
            else -> showDialog(getString(R.string.network_error))
        }

        binding.loginLoginBtn.isClickable = true
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish()
            return
        }

        doubleBackToExitPressedOnce = true

        Timer().schedule(2000) {
            doubleBackToExitPressedOnce = false
        }
    }
}