package com.coconutplace.wekit.ui.main

//import com.coconutplace.wekit.utils.GlobalConstant.Companion.APP_ID

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.coconutplace.wekit.BuildConfig
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Auth
import com.coconutplace.wekit.data.remote.auth.listeners.MainListener
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.channel.BackPressListener
import com.coconutplace.wekit.ui.channel.ChannelFragment
import com.coconutplace.wekit.ui.diary.DiaryFragment
import com.coconutplace.wekit.ui.home.HomeFragment
import com.coconutplace.wekit.utils.GlobalConstant
import com.coconutplace.wekit.utils.SharedPreferencesManager
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sendbird.android.SendBird
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : BaseActivity(), MainListener{
    private val tabNames = listOf("투데이", "혜택존", "랭킹")
    private val unselectedTabIcons = listOf(R.drawable.icn_home_normal, R.drawable.icn_chat_normal, R.drawable.icn_diary_normal)
    private val selectedTabIcons = listOf(R.drawable.icn_home_selected, R.drawable.icn_chat_selected, R.drawable.icn_diary_selected)
    private val viewModel: MainViewModel by viewModel()
    lateinit var navController:NavController
    lateinit var navHostFragment: NavHostFragment
    private var mFlag = 0;
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.mainListener = this

        val channelUrl = intent.getStringExtra("groupChannelUrl")

//        if(channelUrl == null){
//            initNavigation()
//        }else{
//            initNavigationWithPush()
//        }

//        initTab()
        initNavigation()
        initSendBird(channelUrl)
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.getVersion()
    }

    private fun initNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.main_bottom_nav)
        bottomNavigationView.itemIconTintList = null
        val viewPager: ViewPager2 = findViewById(R.id.main_viewpager)
        val pagerAdapter = MainPagerAdapter(this)
        pagerAdapter.addFragment(HomeFragment())
        pagerAdapter.addFragment(ChannelFragment())
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


//    private fun initTab(){
//        val tabLayout: TabLayout = findViewById(R.id.main_tab)
//
//        val pagerAdapter = MainPagerAdapter(this)
//        pagerAdapter.addFragment(HomeFragment())
//        pagerAdapter.addFragment(ChannelFragment())
//        pagerAdapter.addFragment(DiaryFragment())
//
//        val viewPager: ViewPager2 = findViewById(R.id.main_viewpager)
//
//        viewPager.adapter = pagerAdapter
//        viewPager.offscreenPageLimit = 2
//        viewPager.isUserInputEnabled = false
//
//        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
////            tab.text = tabNames[position]
////            tab.setIcon(unselectedTabIcons[position])
//        }.attach()
//
//        tabLayout.getTabAt(0).setIcon(selectedTabIcons[0])
//        tabLayout.getTabAt(0).setIcon(selectedTabIcons[0])
//        tabLayout.getTabAt(0).setIcon(selectedTabIcons[0])
//
//        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                tab!!.setIcon(selectedTabIcons[tab.position])
//                viewPager.currentItem = tab.position
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {
//
//            }
//
//            override fun onTabReselected(tab: TabLayout.Tab?) {
//
//            }
//        })
//    }

//    private fun initNavigation() {
//        navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.main_bottom_nav)
////        NavigationUI.setupWithNavController(bottomNavigationView, findNavController(R.id.main_nav_host))
//        navController = navHostFragment.navController
//
//        bottomNavigationView.setupWithNavController(navController)
//        bottomNavigationView.itemIconTintList = null
//
//        val graph = navController.navInflater.inflate(R.navigation.nav_main)
//        graph.startDestination = R.id.homeFragment
//        navHostFragment.navController.graph = graph
//    }
//
//    //푸시알람을 클릭해서 SplashActivity를 거쳤을 때의 navigation 세팅(channelFragment부터 시작)
//    private fun initNavigationWithPush(){
//        navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.main_bottom_nav)
//        navController = navHostFragment.navController
//
//        bottomNavigationView.setupWithNavController(navController)
//        bottomNavigationView.itemIconTintList = null
//
//        val graph = navController.navInflater.inflate(R.navigation.nav_main)
//        graph.startDestination = R.id.channelFragment
//        navHostFragment.navController.graph = graph
//    }

    private fun initSendBird(channelUrl: String?) {
        SendBird.init(getString(R.string.sendbird_app_key), this)
        val id = SharedPreferencesManager(this).getClientID()
        SendBird.connect(id) { _, e ->
            if (e != null) {
                Log.e(CHECK_TAG, "connection failed 센드버드 연결 실패 $e")

                return@connect
            }
            if(channelUrl!=null){
                moveToChatActivity(channelUrl)
            }

//            val nickname = SharedPreferencesManager(this).getNickname()
//            SendBird.updateCurrentUserInfo(nickname, null) {
//                if (it != null) {
//                    Log.e(CHECK_TAG,"sendbird init error: $it")
//                    //ERROR
//                } else{
//                    if(channelUrl!=null){
//                        moveToChatActivity(channelUrl)
//                    }
//                }
//            }
        }
    }

    private fun moveToChatActivity(channelUrl: String){
        val channelFragment = navHostFragment.childFragmentManager.fragments[0] as ChannelFragment
        channelFragment.startChatWithPush(channelUrl)
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
        val fragment: Fragment? = navHostFragment.childFragmentManager.fragments[0] //현재 보는 fragment
        if (fragment is BackPressListener) {
            if (fragment.onBackPressed()) {//channel fragment 종료해도 됨
                super.onBackPressed()
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