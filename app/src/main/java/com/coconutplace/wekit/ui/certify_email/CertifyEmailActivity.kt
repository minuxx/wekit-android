package com.coconutplace.wekit.ui.certify_email

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.auth.listeners.CertifyEmailListener
import com.coconutplace.wekit.data.remote.auth.listeners.SignUpListener
import com.coconutplace.wekit.databinding.ActivityCertifyEmailBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.edit_password.EditPasswordActivity
import com.coconutplace.wekit.ui.poll.PollActivity
import com.coconutplace.wekit.ui.signup.SignUpViewModel
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_CERTIFY_EMAIL
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_CERTIFY_NUMBER
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_EDIT_PASSWORD
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_SIGNUP
import com.coconutplace.wekit.utils.SharedPreferencesManager
import com.coconutplace.wekit.utils.hide
import com.coconutplace.wekit.utils.hideKeyboard
import com.coconutplace.wekit.utils.show
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.timer

class CertifyEmailActivity : BaseActivity(), CertifyEmailListener, SignUpListener {
    private lateinit var binding: ActivityCertifyEmailBinding
    private val viewModel: SignUpViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_certify_email)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        viewModel.certifyEmailListener = this
        viewModel.signUpListener = this

        SharedPreferencesManager(this).getUser()?.let{
            viewModel.receivedUser = it
            viewModel.nextFlag = FLAG_SIGNUP
            viewModel.email.postValue(it.email)
            binding.certifyEmailGuide01Tv.text= getString(R.string.certify_email_guide_signup)
            binding.certifyEmailEmailEt.setText(it.email)
        }

        SharedPreferencesManager(this).getEmail()?.let{
            binding.certifyEmailEmailEt.setText(it)
            viewModel.email.postValue(it)
        }

        observeCertificationNumber()

        binding.certifyEmailRootLayout.setOnClickListener(this)
        binding.certifyEmailBackBtn.setOnClickListener(this)
        binding.certifyEmailSendCertificationNumberTv.setOnClickListener(this)
        binding.certifyEmailSendAgainTv.setOnClickListener(this)
    }

    override fun onPause() {
        super.onPause()

        viewModel.timer?.let{
            it.cancel()
            binding.certifyEmailTimerTv.visibility = View.INVISIBLE
            binding.certifyEmailCertificationNumberEtLayout.error = null
        }
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v){
            binding.certifyEmailRootLayout -> binding.certifyEmailRootLayout.hideKeyboard()
            binding.certifyEmailBackBtn -> finish()
            binding.certifyEmailSendCertificationNumberTv -> clickCompleteButton(viewModel.flag)
            binding.certifyEmailSendAgainTv -> {
                binding.certifyEmailRootLayout.hideKeyboard()
                viewModel.certifyEmail()
            }
        }
    }

    private fun observeEmail() {
        viewModel.email.observe(this, Observer {
            if (it.isNotEmpty() && !Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.0-]+\\.[a-zA-Z]{2,6}$", it)) {
                binding.certifyEmailEmailEtLayout.error = getString(R.string.signup_email_validation)
            } else {
                binding.certifyEmailEmailEtLayout.error = null
            }
        })
    }

    private fun observeCertificationNumber() {
        viewModel.certificationNumber.observe(this, Observer {
            when (it.length) {
                0, 4 -> {
                    binding.certifyEmailCertificationNumberEtLayout.error = null
                }
                else -> {
                    binding.certifyEmailCertificationNumberEtLayout.error = getString(R.string.certify_email_certification_number_validation)
                }
            }
        })
    }

    override fun onOKClicked() {

    }

    private fun startPollActivity(){
        SharedPreferencesManager(this).removeUser()

        val intent = Intent(this, PollActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        finish()
    }

    private fun startEditPasswordActivity() {
        val intent = Intent(this@CertifyEmailActivity, EditPasswordActivity::class.java)

        startActivity(intent)
        finish()
    }

    private fun clickCompleteButton(flag : Int){
        binding.certifyEmailRootLayout.hideKeyboard()

        if(flag == FLAG_CERTIFY_EMAIL){ // 인증번호 보내는
            viewModel.certifyEmail()
        } else if(flag == FLAG_CERTIFY_NUMBER){ // 인증번호 검사
            if(viewModel.second > 0) { // 3분 이내
                val number = binding.certifyEmailCertificationNumberEt.text.toString()

                if(number.length == 4 && (viewModel.receivedCertificationNumber.value == Integer.parseInt(number))){ // 인증번호 일치
                    if(viewModel.nextFlag == FLAG_SIGNUP){ // 회원가입 플로우
                        viewModel.signUp()
                    }else if(viewModel.nextFlag == FLAG_EDIT_PASSWORD){ // 비밀번호 수정 플로우
                        startEditPasswordActivity()
                    }
                }else{ // 인증번호 불일치
                    binding.certifyEmailCertificationNumberEtLayout.error = getString(R.string.certify_email_certification_number_invalid)
                }
            }else{ //3분 초과
                binding.certifyEmailCertificationNumberEtLayout.error = getString(R.string.certify_email_exceeded_time)
            }
        }
    }

    private fun startTimer(){
        binding.certifyEmailTimerTv.visibility = View.VISIBLE
        viewModel.second = 180

        viewModel.timer?.let{
            it.cancel()
        }

        viewModel.timer = timer(period = 1000, initialDelay = 1000){
            if(viewModel.second <= 0){
                cancel()
            }

            runOnUiThread {
                binding.certifyEmailTimerTv.text = secondToTimeString(viewModel.second)
            }

            viewModel.second--
        }
    }

    private fun secondToTimeString(leftSecond: Int): String{
        if(leftSecond < 0){
            return "00:00"
        }

        var time = "0${leftSecond / 60}:"
        var second = leftSecond % 60

        if(second < 10){
            time = time + "0" + second
        }else{
            time += second.toString()
        }

        return time
    }

    override fun onCertifyEmailStarted() {
        binding.certifyEmailLoading.show()
    }

    override fun onCertifyEmailSuccess(certificationNumber: Int) {
        binding.certifyEmailLoading.hide()

        viewModel.flag = FLAG_CERTIFY_NUMBER
        binding.certifyEmailSendCertificationNumberTv.text = getString(R.string.certify_email_certify)
        viewModel.receivedCertificationNumber.postValue(certificationNumber)

        startTimer()
    }

    override fun onCertifyEmailFailure(code: Int, message: String) {
        binding.certifyEmailLoading.hide()
    }

    override fun onSignUpStarted() {
        binding.certifyEmailLoading.show()
        binding.certifyEmailLoading.isClickable = false
    }

    override fun onSignUpSuccess(message: String) {
        binding.certifyEmailLoading.hide()

        startPollActivity()
    }

    override fun onSignUpFailure(code: Int, message: String) {
        binding.certifyEmailLoading.hide()

    }
}