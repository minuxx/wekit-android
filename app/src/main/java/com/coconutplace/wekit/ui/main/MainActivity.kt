package com.coconutplace.wekit.ui.main

//import com.coconutplace.wekit.utils.GlobalConstant.Companion.APP_ID

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.coconutplace.wekit.BuildConfig
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Auth
import com.coconutplace.wekit.data.remote.auth.listeners.MainListener
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.channel.ChannelFragment
import com.coconutplace.wekit.ui.diary.DiaryFragment
import com.coconutplace.wekit.ui.home.HomeFragment
import com.coconutplace.wekit.utils.GlobalConstant
import com.coconutplace.wekit.utils.SharedPreferencesManager
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sendbird.android.SendBird
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : BaseActivity(), MainListener{
    private val viewModel: MainViewModel by viewModel()
    private var mFlag = 0;
    private var doubleBackToExitPressedOnce = false
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var channelFragment: ChannelFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.mainListener = this

        val channelUrl = intent.getStringExtra("groupChannelUrl")

        initNavigation()
        channelUrl?.let {
            viewPager.registerOnPageChangeCallback(PageChangeCallback())
            viewPager.currentItem = 1
        }

        initSendBird(channelUrl)
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.getVersion()
    }

    private fun initNavigation() {
        bottomNavigationView = findViewById(R.id.main_bottom_nav)
        bottomNavigationView.itemIconTintList = null

        viewPager = findViewById(R.id.main_viewpager)
        viewPager.isUserInputEnabled = false

        channelFragment = ChannelFragment()

        val pagerAdapter = MainPagerAdapter(this)
        pagerAdapter.addFragment(HomeFragment())
        pagerAdapter.addFragment(channelFragment)
        pagerAdapter.addFragment(DiaryFragment())
        viewPager.adapter = pagerAdapter

        bottomNavigationView.setOnNavigationItemSelectedListener { navSelector(viewPager, it) }
    }

    private fun navSelector(viewPager: ViewPager2, item: MenuItem) : Boolean{
        val checked = item.setChecked(true)

        when(checked.itemId){
            R.id.homeFragment -> {
                viewPager.currentItem = 0
                return true
            }
            R.id.channelFragment -> {
                viewPager.currentItem = 1
                return true
            }
            R.id.diaryFragment ->{
                viewPager.currentItem = 2
                return true
            }
        }

        return false
    }

    private inner class PageChangeCallback: ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            bottomNavigationView.selectedItemId = when (position) {
                0 -> R.id.homeFragment
                1 -> R.id.channelFragment
                2 -> R.id.diaryFragment
                else -> error("no such position: $position")
            }
        }
    }

    private fun initSendBird(channelUrl: String?) {
        val initFlag = SendBird.init(getString(R.string.sendbird_app_key), applicationContext)
        Log.e(CHECK_TAG,"initFlag : $initFlag")
        val id = SharedPreferencesManager(this).getClientID()
        SendBird.connect(id) { _, e ->
            if (e != null) {
                Log.e(CHECK_TAG, "connection failed 센드버드 연결 실패 id:$id, error:$e")
                return@connect
            }
            if(channelUrl!=null){
                //moveToChatActivity(channelUrl)
                viewModel.setPushUrl(channelUrl)
            }
        }
    }

    fun getChannelUrlWithPush(): String?{
        val pushUrl = viewModel.getPushUrl()
        viewModel.setPushUrl(null)
        return pushUrl
    }

    override fun onStarted() {

    }

    override fun onGetVersionSuccess(auth: Auth) {
        val version = BuildConfig.VERSION_NAME

        if(!auth.isAvail){
            mFlag = GlobalConstant.FLAG_SERVER_CHECK
            showDialog(getString(R.string.dialog_title_server_check))
            return
        }

        if(version != auth.androidVersion){
            mFlag = GlobalConstant.FLAG_VERSION_UPDATE
            showDialog(getString(R.string.dialog_title_version_update))
            return
        }
    }

    override fun onGetVersionFailure(code: Int, message: String) {
        when(code){
            404 -> {
                mFlag = GlobalConstant.FLAG_NETWORK_ERROR
                showDialog(getString(R.string.network_error))
            }
        }
    }

    override fun onOKClicked() {
        when(mFlag){
            GlobalConstant.FLAG_SERVER_CHECK,
            GlobalConstant.FLAG_NETWORK_ERROR -> {
                finish()
            }

            GlobalConstant.FLAG_VERSION_UPDATE -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("market://details?id=$packageName")
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        Log.e(CHECK_TAG,"viewPager currrentItem ${viewPager.currentItem}")
        if (viewPager.currentItem == 1) {
            if (channelFragment.onBackPressed()) {//channel fragment 종료해도 됨
                if (doubleBackToExitPressedOnce) {
                    finish()
                    return
                }
                doubleBackToExitPressedOnce = true
                Timer().schedule(2000) {
                    doubleBackToExitPressedOnce = false
                }
            } else {
                //channel fragment 종료하지 않고 내부 처리함
            }
        } else {
            //back key를 연속으로 두 번 눌러야 앱 종료
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
}