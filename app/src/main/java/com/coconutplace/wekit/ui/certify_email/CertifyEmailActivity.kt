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

        observeCertificationNumber()

        binding.certifyEmailRootLayout.setOnClickListener(this)
        binding.certifyEmailBackBtn.setOnClickListener(this)
        binding.certifyEmailSendCertificationNumberTv.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v){
            binding.certifyEmailRootLayout -> binding.certifyEmailRootLayout.hideKeyboard()
            binding.certifyEmailBackBtn -> finish()
            binding.certifyEmailSendCertificationNumberTv -> {
                if(viewModel.flag == FLAG_CERTIFY_EMAIL){
                    viewModel.certifyEmail()
                } else if(viewModel.flag == FLAG_CERTIFY_NUMBER){
                    if(viewModel.receivedCertificationNumber.value == Integer.parseInt(binding.certifyEmailCertificationNumberEt.text.toString())){
                        if(viewModel.nextFlag == FLAG_SIGNUP){
                            viewModel.signUp()
                        }else if(viewModel.nextFlag == FLAG_EDIT_PASSWORD){
                            startEditPasswordActivity()
                        }
                    }else{
                        binding.certifyEmailCertificationNumberEtLayout.error = getString(R.string.certify_email_certification_number_invalid)
                    }
                }
            }
        }
    }

    private fun observeEmail() {
        viewModel.email.observe(this, Observer {
            if (it.isNotEmpty() && !Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.0-]+\\.[a-zA-Z]{2,6}$", it)) {
                binding.certifyEmailEmailEtLayout.error = getString(R.string.signup_email_validation)
                binding.certifyEmailSendCertificationNumberTv.setBackgroundColor(getColor(R.color.certify_email_send_certification_number_btn_inactive))
            } else if(it.isEmpty()) {
                binding.certifyEmailSendCertificationNumberTv.setBackgroundColor(getColor(R.color.certify_email_send_certification_number_btn_inactive))
            } else {
                binding.certifyEmailEmailEtLayout.error = null
                binding.certifyEmailSendCertificationNumberTv.setBackgroundColor(getColor(R.color.certify_email_send_certification_number_btn_active))
            }
        })
    }

    private fun observeCertificationNumber() {
        viewModel.certificationNumber.observe(this, Observer {
            when {
                it.isEmpty() -> {
                    binding.certifyEmailSendCertificationNumberTv.setBackgroundColor(getColor(R.color.certify_email_send_certification_number_btn_inactive))
                }
                it.length == 4 -> {
                    binding.certifyEmailCertificationNumberEtLayout.error = null
                    binding.certifyEmailSendCertificationNumberTv.setBackgroundColor(getColor(R.color.certify_email_send_certification_number_btn_active))
                }
                else -> {
                    binding.certifyEmailCertificationNumberEtLayout.error = getString(R.string.certify_email_certification_number_validation)
                    binding.certifyEmailSendCertificationNumberTv.setBackgroundColor(getColor(R.color.certify_email_send_certification_number_btn_inactive))
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

    private fun startTimer(){
        binding.certifyEmailTimerTv.visibility = View.VISIBLE

        timer(period = 1000, initialDelay = 1000){
            binding.certifyEmailTimerTv.text = secondToTimeString(viewModel.second)

            if(viewModel.second <= 0){
                cancel()
            }

            viewModel.second--
        }
    }

    private fun secondToTimeString(leftSecond: Int): String{
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
        binding.certifyEmailCertificationNumberEtLayout.visibility = View.VISIBLE
        binding.certifyEmailSendCertificationNumberTv.text = getString(R.string.certify_email_certify)
        binding.certifyEmailSendCertificationNumberTv.setBackgroundColor(getColor(R.color.certify_email_send_certification_number_btn_inactive))
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