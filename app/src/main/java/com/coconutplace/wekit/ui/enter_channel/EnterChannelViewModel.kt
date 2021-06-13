package com.coconutplace.wekit.ui.enter_channel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coconutplace.wekit.data.entities.ChatRoom
import com.coconutplace.wekit.data.remote.channel.listeners.EnterChannelListener
import com.coconutplace.wekit.data.repository.channel.ChannelRepository
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.ERROR_TAG
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EnterChannelViewModel(private val repository: ChannelRepository) : ViewModel() {

    var enterChannelListener: EnterChannelListener?= null
    private lateinit var curRoomInfo: ChatRoom

    var enterFlag:Boolean = false
    var fullMemberFlag:Boolean = false

    val name: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val explain: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply {
            postValue("")
        }
    }

    val authenticTime: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply{
            postValue("")
        }
    }
    val currentMember: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply{
            postValue("")
        }
    }
    val miracleType: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply{
            postValue("")
        }
    }

    val isStarted: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun setRoomInfo(roomInfo: ChatRoom){

        curRoomInfo = roomInfo

        val authSession = roomInfo.authenticTime!!.substring(0,2).toInt()
        val authTime = if(authSession<=12){
            "[AM $authSession 인증방]"
        } else{
            "[PM ${authSession-12} 인증방]"
        }
        authenticTime.postValue(authTime)


        name.postValue(roomInfo.roomName)
        explain.postValue(roomInfo.chatDescription)
        currentMember.postValue("현재 ${roomInfo.maxLimit}명 중 ${roomInfo.currentNum}명 참여")
        when(roomInfo.miracle){
            "M" -> miracleType.postValue("미라클 모닝")
            "N" -> miracleType.postValue("미라클 나잇")
        }

        if(roomInfo.isStart=="Y"){
            isStarted.postValue("진행 중")
        }
        else{
            isStarted.postValue("대기 중")
        }
    }

    fun enterChannel(){
        if(!enterFlag){
            enterChannelListener?.makeSnackBar("이미 소속된 채팅방이 있습니다")
            return
        }
        if(fullMemberFlag){
            enterChannelListener?.makeSnackBar("이미 방이 최대인원입니다")
            return
        }
        viewModelScope.launch(Dispatchers.IO){
            try{
                val enterChannelResponse = repository.enterChannel(curRoomInfo)
                if(enterChannelResponse.isSuccess){
                    Log.e(CHECK_TAG,"enter success")
                    //enterChannelListener?.callChatActivity(channelUrl,roomIndex)
                    enterSendBirdChannel()
                }
                else{
                    Log.e(ERROR_TAG,"enter channel failed")
                    enterChannelListener?.makeSnackBar(enterChannelResponse.message)
                }

            }catch (e:Exception){
                Log.e(ERROR_TAG,"enter channel error: $e")
                enterChannelListener?.makeSnackBar("입장에 실패하였습니다")
            }
        }
    }

    private fun enterSendBirdChannel(){
        GroupChannel.getChannel(curRoomInfo.chatUrl, GroupChannel.GroupChannelGetHandler { groupChannel, e ->
            if (e != null) {
                // Error!
                enterChannelListener?.makeSnackBar("존재하지 않는 링크입니다")
                Log.e(ERROR_TAG, "존재하지 않는 센드버드 Url입니다")
                return@GroupChannelGetHandler
            }

            val memberList = groupChannel.members
            var isMember = false
            for (me in memberList) {
                if (me.userId == SendBird.getCurrentUser().userId) {
                    isMember = true
                    break
                }
            }
            if (isMember) {
                Log.e(CHECK_TAG,"already member, enter success")
                enterChannelListener?.callChatActivity(curRoomInfo.chatUrl!!,curRoomInfo.roomIdx)
            }
            else {
                Log.e(CHECK_TAG, "is not member")
                groupChannel.join { e2 ->
                    if (e2 != null) {
                        Log.e(ERROR_TAG,"SendBird cannot Join Error : $e")
                        if(e2.code==400750){
                            enterChannelListener?.makeSnackBar("추방당한 채팅방입니다")
                        }
                    }
                    else{
                        enterChannelListener?.callChatActivity(curRoomInfo.chatUrl!!,curRoomInfo.roomIdx)
                    }
                }
            }
        })
    }


}