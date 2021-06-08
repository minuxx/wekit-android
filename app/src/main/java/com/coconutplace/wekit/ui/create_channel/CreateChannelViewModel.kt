package com.coconutplace.wekit.ui.create_channel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.CreateChannelInfo
import com.coconutplace.wekit.data.remote.channel.listeners.CreateChannelListener
import com.coconutplace.wekit.data.repository.channel.ChannelRepository
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.ERROR_TAG
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannel.GroupChannelDeleteHandler
import com.sendbird.android.GroupChannelParams
import com.sendbird.android.SendBird
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreateChannelViewModel(private val repository: ChannelRepository) : ViewModel(){
    var createChannelListener:CreateChannelListener?= null
    val tagStringList: MutableList<String> = arrayListOf()
    var durationLong = false// false 면 2주, true 면 1달
    var createFlag = false

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

    fun createGroupChannel(count:Int, maxLimit:Int) {
        val channelName = name.value
        val operator: MutableList<String> = ArrayList()
        val me = SendBird.getCurrentUser().userId
        operator.add(me)

        val params = GroupChannelParams()
            .setPublic(true)
            .setEphemeral(false)
            .setDistinct(false)
            .setSuper(false)
            .setName(channelName) //.addUserIds(new List<String>())
            .setOperatorUserIds(operator)

        GroupChannel.createChannel(params,
            GroupChannel.GroupChannelCreateHandler { groupChannel, e1 ->
                if (e1 != null) {
                    //Error
                    Log.e(ERROR_TAG, "createChannel fail $e1")
                    createChannelListener?.onCreateChannelFailure()
                    return@GroupChannelCreateHandler
                }
                Log.e(CHECK_TAG, "create success")

                //서버 api 통신
                val roomName = name.value //방이름
                val chatExplain = explain.value //방설명
                val chatRoomImg = "https://firebasestorage.googleapis.com/v0/b/wekit-a56e6.appspot.com/o/my-chat-img.jpg?alt=media&token=8c87007d-3639-4818-9576-7e727380ec1d" //방 이미지
                val chatUrl = groupChannel.url //방Url
                //maxLimit //arg0 최대인원
                val roomType="식단" //방 종류
                val roomTerm = if(durationLong){ //방 종류
                    "한달방"
                }else{
                    "2주방"
                }
                var tag = "" //태그

                val tagSize = tagStringList.size
                for(i in 0 until tagSize-1){
                    tag += tagStringList[i].substring(1)+"|"
                }
                if(tagSize>0)
                    tag+=tagStringList[tagSize-1].substring(1)
//                Log.e(CHECK_TAG," 중간점검-> \n roomName : $roomName, chatExplain : $chatExplain, chatRoomImg : $chatRoomImg, chatUrl : $chatUrl,\n"
//                        + "maxLimit : $maxLimit, roomType : $roomType, roomTerm : $roomTerm, count : $count, 최종 TAG : $tag")

                val tempChannelInfo = CreateChannelInfo(roomName!!,chatExplain!!,chatRoomImg,chatUrl,maxLimit,roomType,roomTerm,count,tag)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = repository.createChannel(tempChannelInfo)
                        if(response.isSuccess){
                            Log.e(CHECK_TAG,"create success")
                            if(response.result!=null){
                                val badgeTitle = response.result.badgeTitle!!
                                val badgeUrl = response.result.badgeUrl!!
                                val badgeExplain = response.result.badgeExplain!!
                                val backgroundColor = response.result.backgroundColor!!
                                createChannelListener?.onCreateChannelSuccess(badgeTitle,badgeUrl,badgeExplain,backgroundColor)
                            }
                            else{
                                createChannelListener?.onCreateChannelSuccess()
                            }
                        }
                        else{
                            Log.e(ERROR_TAG, "서버 api channel create failed, code : "+response.code+", message : "+response.message)
                            createChannelListener?.makeSnackBar(response.message)
                            createChannelListener?.onCreateChannelFailure()
                            // 센드버드 서버의 체널 삭제
                            groupChannel.delete(GroupChannelDeleteHandler { e2 ->
                                if (e2 != null) {
                                    //Error
                                    Log.e(ERROR_TAG, "groupChannel created but cannot delete $e2")
                                    return@GroupChannelDeleteHandler
                                }
                                Log.e(CHECK_TAG, "canceled groupChannel creation")
                            })
                        }

                    } catch (e3: Exception) {
                        Log.e(ERROR_TAG, "서버api channelcreate error $e3")
                        createChannelListener?.onCreateChannelFailure()
                        // 센드버드 서버의 체널 삭제
                        groupChannel.delete(GroupChannelDeleteHandler { e4 ->
                            if (e4 != null) {
                                //Error
                                Log.e(ERROR_TAG, "groupChannel created but cannot delete $e4")
                                return@GroupChannelDeleteHandler
                            }
                            Log.e(CHECK_TAG, "canceled groupChannel creation")
                        })
                    }
                }
            })
    }

//    fun showTagList(){
//        Log.e(CHECK_TAG,"tempButton Clicked, size : "+tagStringList.size)
//        for(i in 0 until tagStringList.size){
//            Log.e(CHECK_TAG,"text : ${tagStringList[i]}")
//        }
//    }
}