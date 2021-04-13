package com.coconutplace.wekit.ui.set

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Auth
import com.coconutplace.wekit.data.remote.auth.listeners.SetListener
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.badge.BadgeActivity
import com.coconutplace.wekit.ui.login.LoginActivity
import com.coconutplace.wekit.ui.notice.NoticeActivity
import com.coconutplace.wekit.ui.opensource.OpensourceActivity
import com.coconutplace.wekit.ui.profile.ProfileActivity
import com.coconutplace.wekit.ui.tutorial.TutorialActivity
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_TUTORIAL_SET
import com.coconutplace.wekit.utils.GlobalConstant.Companion.PROFILE_URL
import com.coconutplace.wekit.utils.hide
import com.coconutplace.wekit.utils.show
import com.coconutplace.wekit.utils.snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.plusfriend.PlusFriendService
import com.kakao.util.exception.KakaoException
import kotlinx.android.synthetic.main.activity_set.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SetActivity : BaseActivity(), SetListener{
    private val viewModel: SetViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set)

        viewModel.setListener = this
        set_back_btn.setOnClickListener(this)
        set_profile_iv.setOnClickListener(this)
        set_notice_tv.setOnClickListener(this)
        set_badge_tv.setOnClickListener(this)
        set_inquiry_tv.setOnClickListener(this)
        set_tnc_tv.setOnClickListener(this)
        set_opensource_tv.setOnClickListener(this)
        set_tutorial_tv.setOnClickListener(this)

        set_logout_tv.setOnClickListener(this)

        viewModel.getProfile()
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.getProfile()
        set_profile_iv.isClickable = true
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when(v){
            set_back_btn -> finish()
            set_notice_tv -> startNoticeActivity()
            set_profile_iv -> startProfileActivity()
            set_inquiry_tv -> openKakao()
            set_opensource_tv -> startOpensourceActivity()
            set_logout_tv -> {
                viewModel.mFlagLogout = true
                viewModel.sendFcmToken(null)
            }
            set_tutorial_tv -> startTutorialActivity()

            set_badge_tv -> startBadgeActivity()

            set_tnc_tv -> set_root_layout.snackbar(getString(R.string.guide_update))
        }
    }

    private fun openKakao(){
        try{
            PlusFriendService.getInstance().chat(this, getString(R.string.kakao_channel_id))
        }catch (e : KakaoException){
            e.printStackTrace()
            set_root_layout.snackbar(getString(R.string.network_error))
        }
    }

    private fun setAlarmListener(){
        set_alarm_st.setOnCheckedChangeListener{ v, isChecked ->
            if(!isChecked){
                viewModel.sendFcmToken(null)
            }else{
                val fcmTask = FirebaseMessaging.getInstance().token
                    .addOnSuccessListener {
                        viewModel.sendFcmToken(it)
                    }
                    .addOnFailureListener {
                        onSendFcmTokenFailure(404, getString(R.string.network_error))
                    }
            }
        }
    }

    private fun startLoginActivity(){
        val intent = Intent(this@SetActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
        finish()
    }

    private fun startNoticeActivity(){
        val intent = Intent(this@SetActivity, NoticeActivity::class.java)

        startActivity(intent)
    }

    private fun startOpensourceActivity(){
        val intent = Intent(this@SetActivity, OpensourceActivity::class.java)

        startActivity(intent)
    }

    private fun startProfileActivity(){
        set_profile_iv.isClickable = false
        val intent = Intent(this@SetActivity, ProfileActivity::class.java)
        intent.putExtra(PROFILE_URL, viewModel.profileUrl.value.toString())

        startActivity(intent)
    }

    private fun startTutorialActivity(){
        set_profile_iv.isClickable = false
        val intent = Intent(this@SetActivity, TutorialActivity::class.java)
        intent.putExtra("flag", FLAG_TUTORIAL_SET)

        startActivity(intent)
    }

    private fun startBadgeActivity(){
        set_profile_iv.isClickable = false
        val intent = Intent(this@SetActivity, BadgeActivity::class.java)

        startActivity(intent)
    }

    override fun onGetProfileStarted() {
        set_loading.show()
    }

    override fun onGetProfileSuccess(auth: Auth) {
        set_loading.hide()

        set_nickname_tv.text = auth.nickname
        Glide.with(this).load(auth.profileImg)
             .circleCrop()
             .placeholder(R.drawable.character_big_basic)
             .error(R.drawable.character_big_basic)
             .into(set_profile_iv)
        set_alarm_st.isChecked = auth.isNotice == 1
        setAlarmListener()

        viewModel.profileUrl.postValue(auth.profileImg)
    }

    override fun onGetProfileFailure(code: Int, message: String) {
        set_loading.hide()

        when(code){
            404 -> set_root_layout.snackbar(getString(R.string.network_error))
        }
    }

    override fun onSendFcmTokenStarted() {
        set_loading.show()
    }

    override fun onSendFcmTokenSuccess() {
        set_loading.hide()

        if(viewModel.mFlagLogout){
            startLoginActivity()
        }
    }

    override fun onSendFcmTokenFailure(code: Int, message: String) {
        set_loading.hide()

        set_root_layout.snackbar(message)
    }
}