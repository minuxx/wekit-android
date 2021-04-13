package com.coconutplace.wekit.ui.enter_channel

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.ChatRoom
import com.coconutplace.wekit.data.remote.channel.listeners.EnterChannelListener
import com.coconutplace.wekit.databinding.ActivityEnterChannelBinding
import com.coconutplace.wekit.ui.chat.ChatActivity
import com.coconutplace.wekit.utils.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class EnterChannelActivity : AppCompatActivity(), EnterChannelListener {

    private lateinit var mBinding: ActivityEnterChannelBinding
    private val mEnterChannelViewModel: EnterChannelViewModel by viewModel()
    private lateinit var roomInfo:ChatRoom

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        roomInfo = intent.getSerializableExtra("roomInfo") as ChatRoom

        mEnterChannelViewModel.enterFlag = intent.getBooleanExtra("enterFlag",false)
        roomInfo.maxLimit
        roomInfo.currentNum
        if(roomInfo.maxLimit!!<=roomInfo.currentNum!!){
            mEnterChannelViewModel.fullMemberFlag = true
        }

        setupView()
        setupViewModel()

    }

    private fun setupView(){
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_enter_channel)
        mBinding.lifecycleOwner = this
        mBinding.mEnterChannelViewModel = mEnterChannelViewModel

        mBinding.enterChannelBackBtn.setOnClickListener{
            finish()
        }
        mBinding.enterChannelBackBtnFrameLayout.setOnClickListener{
            finish()
        }
        showTags()
    }

    private fun showTags(){

        val tagStringList: MutableList<String> = arrayListOf()
        val colors: MutableList<IntArray> = arrayListOf()
        //int[] color = {TagBackgroundColor, TagBorderColor, TagTextColor, TagSelectedBackgroundColor}
        val color = intArrayOf(Color.TRANSPARENT, Color.WHITE, Color.WHITE, Color.TRANSPARENT)

        roomInfo.tagList?.let{
            for(tag in it){
                if(tag==""){
                    continue
                }
                colors.add(color)
                tagStringList.add("#$tag")
            }
        }
        mBinding.createChannelTagContainerLayout.setTags(tagStringList,colors)
    }

    private fun setupViewModel(){
        mEnterChannelViewModel.setRoomInfo(roomInfo)
        mEnterChannelViewModel.enterChannelListener = this
    }

    override fun callChatActivity(channelUrl: String, roomIdx:Int) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("channelUrl", channelUrl)
        intent.putExtra("roomIdx",roomIdx)
        startActivity(intent)
        finish()
    }

    override fun makeSnackBar(str: String) {
        mBinding.enterChannelRootLayout.snackbar(str)
    }
}