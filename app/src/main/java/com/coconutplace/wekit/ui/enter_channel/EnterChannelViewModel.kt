package com.coconutplace.wekit.ui.enter_channel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.ChatRoom
import com.coconutplace.wekit.data.remote.channel.listeners.EnterChannelListener
import com.coconutplace.wekit.data.repository.channel.ChannelRepository
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.ERROR_TAG
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EnterChannelViewModel(private val repository: ChannelRepository) : ViewModel() {

    var enterChannelListener: EnterChannelListener?= null
    private lateinit var channelUrl:String
    private var roomIndex:Int = 0
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

    val duration: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply{
            postValue("")
        }
    }
    val currentMember: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply{
            postValue("")
        }
    }
    val authCount: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply{
            postValue("")
        }
    }

    val isStarted: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun setRoomInfo(roomInfo: ChatRoom){

        channelUrl = roomInfo.chatUrl!!
        roomIndex = roomInfo.roomIdx

        when (roomInfo.roomTerm) {
            "2주방" -> {
                duration.postValue("[2주 챌린지]")
            }
            "한달방" -> {
                duration.postValue("[4주 챌린지]")
            }
            else -> {
                duration.postValue("")
            }
        }

        name.postValue(roomInfo.roomName)
        explain.postValue(roomInfo.chatDescription)
        currentMember.postValue("현재 ${roomInfo.maxLimit}명 중 ${roomInfo.currentNum}명 참여")
        authCount.postValue("하루 ${roomInfo.certificationCount}끼 인증")
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
        CoroutineScope(Dispatchers.IO).launch{
            try{
                val chatRoom = ChatRoom(roomIndex,null,null,null,null,null,null,null,null,null,null)
                val enterChannelResponse = repository.enterChannel(chatRoom)
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
        GroupChannel.getChannel(channelUrl, GroupChannel.GroupChannelGetHandler { groupChannel, e ->
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
                enterChannelListener?.callChatActivity(channelUrl,roomIndex)
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
                        enterChannelListener?.callChatActivity(channelUrl,roomIndex)
                    }
                }
            }
        })
    }


}