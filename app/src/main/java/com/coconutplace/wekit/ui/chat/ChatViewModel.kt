package com.coconutplace.wekit.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coconutplace.wekit.data.entities.ChatExtentions
import com.coconutplace.wekit.data.entities.RoomInfo
import com.coconutplace.wekit.data.entities.UserInfo
import com.coconutplace.wekit.data.remote.chat.listeners.ChatListener
import com.coconutplace.wekit.data.repository.chat.ChatRepository
import com.coconutplace.wekit.utils.Event
import com.coconutplace.wekit.utils.GlobalConstant.Companion.DUMMY_MESSAGE_COUNT
import com.coconutplace.wekit.utils.SharedPreferencesManager
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.ERROR_TAG
import com.sendbird.android.*
import com.sendbird.android.BaseChannel.GetMessagesHandler
import com.sendbird.android.BaseChannel.SendUserMessageHandler
import com.sendbird.android.GroupChannel.GroupChannelGetHandler
import com.sendbird.android.GroupChannel.GroupChannelRefreshHandler
import com.sendbird.android.SendBird.ChannelHandler
import com.sendbird.android.SendBird.UserInfoUpdateHandler
import com.sendbird.android.handlers.AddOperatorsHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

class ChatViewModel(private val repository: ChatRepository, private val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {
    private val CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT"

    private var chatListener:ChatListener? = null

    private var _channel: GroupChannel? = null
    private var _channelUrl: String? = null
    private var _roomIdx:Int = 0
    private var _messageList: ArrayList<BaseMessage> = ArrayList()
    private var _isMessageLoading = false
    private val _memberInfoList:ArrayList<UserInfo> = ArrayList()
    private var _operatorUserIndex:Int = 0
    private var _nickname:String? = null
    private var _pushNotificationOn: Boolean? = null

    private var _showDialog = MutableLiveData<Event<String>>()
    val showDialog: LiveData<Event<String>>
        get() = _showDialog

    val isInitialized: MutableLiveData<Boolean> by lazy{
        MutableLiveData<Boolean>().apply {
            postValue(false)
        }
    }
    val liveCurrentDay: MutableLiveData<String> by lazy{
        MutableLiveData<String>()
    }

    val liveTotalAuthCount: MutableLiveData<String> by lazy{
        MutableLiveData<String>()
    }

    val liveOperator: MutableLiveData<String> by lazy{
        MutableLiveData<String>()
    }
    val liveMemberListInfo: MutableLiveData<ArrayList<UserInfo>> by lazy{
        MutableLiveData<ArrayList<UserInfo>>()
    }
    val liveStartDate:MutableLiveData<String> by lazy{
        MutableLiveData<String>()
    }
    val isLoading:MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getRoomIdx():Int{
        return _roomIdx
    }

    fun setChatListener(chatListener: ChatListener){
        this.chatListener = chatListener
    }

    fun getMemberInfoList():ArrayList<UserInfo>{
        return _memberInfoList
    }

    fun getNickName():String{
        return sharedPreferencesManager.getNickname()
    }

    fun getPushNotificationOn(): Boolean{
        return _pushNotificationOn!!
    }

    private fun setNickname(nickname:String){
        SendBird.updateCurrentUserInfo(nickname,null,
            UserInfoUpdateHandler { e ->
                if (e != null) {
                    // Error!
                    Log.e(ERROR_TAG,"닉네임 세팅실패")
                    return@UserInfoUpdateHandler
                }
                else{
                    Log.e(CHECK_TAG,"닉네임 세팅완료")
                }
            })
    }

    fun setPushFlag(on:Boolean){
        sharedPreferencesManager.savePushNotificationFlag(on)
    }

    fun getPushFlag():Boolean{
        return sharedPreferencesManager.getPushNotificationFLag()
    }

    fun getRoomInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val roomInfoResponse = repository.getRoomInfo(_roomIdx)
                if(roomInfoResponse.isSuccess){
                    Log.e(CHECK_TAG, "api roomInfo success")
                    val roomInfo: RoomInfo = roomInfoResponse.result!!
                    liveCurrentDay.postValue("우리 인증한지 ${roomInfo.day}일")
                    liveTotalAuthCount.postValue("총 인증횟수 : ${roomInfo.totalAuthenticCount}회 (하루 ${roomInfo.certificationCount}회 * ${roomInfo.totalDay}일)")
                    Log.e(CHECK_TAG,"totalMEmber : ${roomInfo.totalMember} =?= ${roomInfo.userInfo!!.size}")
                    _pushNotificationOn = roomInfo.isNotice=="Y"

                    val startDate = roomInfo.startDate
                    if(startDate==null||startDate==""||startDate=="null"){
                        liveStartDate.postValue("아직 챌린지가 시작되지 않았습니다")
                    }
                    else{
                        liveStartDate.postValue("우리방 인증기간 : ${roomInfo.startDate} - ${roomInfo.endDate}")
                    }

                    val operatorArray:ArrayList<String> = ArrayList()
                    _memberInfoList.clear()
                    var isHostFlag = false
                    roomInfo.userInfo.let {
                        for(member in it!!){
                            operatorArray.add(member.id!!)
                            _memberInfoList.add(member)
                            if(member.type=="host"){
                                liveOperator.postValue("방장 : ${member.nickname}")
                                _operatorUserIndex = member.userIdx
                                if(member.nickname == _nickname){//자신이 방장이면
                                    isHostFlag = true
                                }
                            }
                            Log.e(CHECK_TAG,"member id : ${member.id}")
                        }
                        liveMemberListInfo.postValue(_memberInfoList)
                    }
                    Log.e(CHECK_TAG,"$_nickname =?= ${liveOperator.value}")

                    if(roomInfo.isStart=="Y"){
                        isHostFlag = false
                    }

                    chatListener?.showStartChallengeButton(isHostFlag)//방장이고 2주방일때 챌린지 시작 버튼 보이게함

                    if (_channel!!.myRole == Member.Role.OPERATOR) {
                        _channel!!.addOperators(operatorArray, AddOperatorsHandler { e ->
                            if (e != null) { //방장이 나갔을때 새로운 방장이 추방 권한을 가질 수 있게 모두가 operator가 되어야함.
                                //operator 지정은 방장밖에 못함
                                Log.e(ERROR_TAG, "맴버들을 operator로 지정하는데 실패하였습니다.$e")
                            } else {
                                Log.e(CHECK_TAG, "맴버들을 operator로 지정하였습니다")
                            }
                        })
                    }
                }
                else{
                    Log.e(ERROR_TAG,"getRoomInfo fail ${roomInfoResponse.message}")
                    //chatListener?.makeSnackBar(roomInfoResponse.message)
                    _showDialog.postValue(Event(roomInfoResponse.message))
                }

            } catch (e: Exception) {
                Log.e(ERROR_TAG,"getRoomInfo error $e")
                //chatListener?.makeSnackBar("서버와의 통신에 실패하였습니다")
                //chatListener?.makePopup("서버와의 통신에 실패하였습니다")
                _showDialog.postValue(Event("서버와의 통신에 실패하였습니다"))

            }
        }
    }

    fun init(url: String, roomIdx:Int) {
        Log.e(CHECK_TAG,"현재 방의 sendbird url : $url, roomIdx : $roomIdx")
        _channelUrl = url
        _roomIdx = roomIdx

        GroupChannel.getChannel(_channelUrl, GroupChannelGetHandler { groupChannel, e ->
            if (e != null) {
                // Error!
                e.printStackTrace()
                Log.e(ERROR_TAG,"접근할 수 없는 SendBird Channel Url입니다")
                return@GroupChannelGetHandler
            }
            _channel = groupChannel
        })
        isInitialized.postValue(true)
        _nickname = sharedPreferencesManager.getNickname()
        //setNickname(mNickname!!)
    }

    private fun addRecentMsg(msg: BaseMessage){
        _messageList.add(0,msg)
        chatListener?.addRecentMessage(msg)
    }

    private fun addOldMsg(msgList: List<BaseMessage>){
        _messageList.addAll(msgList)
        chatListener?.addOldMsg(msgList)
    }

    fun addSendBirdHandler() {
        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, object : ChannelHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel, baseMessage: BaseMessage) {
                if (baseChannel.url == _channelUrl) {
                    addRecentMsg(baseMessage)
                }
            }

            override fun onMessageDeleted(baseChannel: BaseChannel, msgId: Long) {
                super.onMessageDeleted(baseChannel, msgId)
            }

            override fun onMessageUpdated(channel: BaseChannel, message: BaseMessage) {
                super.onMessageUpdated(channel, message)
            }

            override fun onReadReceiptUpdated(channel: GroupChannel) {}
            override fun onTypingStatusUpdated(channel: GroupChannel) {}
            override fun onDeliveryReceiptUpdated(channel: GroupChannel) {}
        })
    }

    fun sendMsg(msg: String?) {
        _channel!!.sendUserMessage(msg, SendUserMessageHandler { userMessage, e ->
            if (e != null) {
                // Error!
                Log.e(ERROR_TAG,"message send fail : $e")

                when (e.code) {
                    900020 -> {
                        //chatListener?.makeSnackBar("채팅방에 속해있지 않습니다")
                        _showDialog.postValue(Event("채팅방에 속해있지 않습니다"))
                    }
                    900100 -> {
                        //chatListener?.makeSnackBar("채팅방에서 추방당하였습니다")
                        _showDialog.postValue(Event("채팅방에서 추방당하셨습니다"))
                    }
                    900041 -> {
                        //chatListener?.makeSnackBar("채팅방이 삭제되어 대화가 금지되었습니다.")
                        _showDialog.postValue(Event("채팅방이 삭제되어 대화가 금지되었습니다"))
                    }
                }
                //Toast.makeText(this, "message send fail", Toast.LENGTH_SHORT).show();
                return@SendUserMessageHandler
            }
            else{
                addRecentMsg(userMessage)
            }
        })
    }

    fun refresh() {
        if (_messageList.size!=0) {
            //liveMessageList.postValue(mMessageList)
            chatListener?.addOldMsg(_messageList)
        }
        else if (_channel == null) {
            GroupChannel.getChannel(_channelUrl, GroupChannelGetHandler { groupChannel, e ->
                if (e != null) {
                    // Error!
                    e.printStackTrace()
                    return@GroupChannelGetHandler
                }
                _channel = groupChannel
                loadLatestMessages(DUMMY_MESSAGE_COUNT, GetMessagesHandler { list, e ->
                        //mChatAdapter.markAllMessagesAsRead();
                    chatListener?.onSendMessageSuccess()
                })
            })
        } else {
            _channel!!.refresh(GroupChannelRefreshHandler { e ->
                if (e != null) {
                    // Error!
                    e.printStackTrace()
                    return@GroupChannelRefreshHandler
                }
                loadLatestMessages(DUMMY_MESSAGE_COUNT, GetMessagesHandler { list, e ->
                        //mChatAdapter.markAllMessagesAsRead();
                    chatListener?.onSendMessageSuccess()
                })
            })
        }
    }

    private fun loadLatestMessages(limit: Int, handler: GetMessagesHandler?) {
        Log.e(CHECK_TAG,"loadLatestMessages")
        if (_channel == null) {
            return
        }
        _channel!!.getPreviousMessagesByTimestamp(Long.MAX_VALUE, true, limit, true,
            BaseChannel.MessageTypeFilter.ALL, null, GetMessagesHandler { list, e ->
                handler?.onResult(list, e)
                if (e != null) {
                    e.printStackTrace()
                    return@GetMessagesHandler
                }
                if (list.size <= 0) {
                    return@GetMessagesHandler
                }

                //list에는 메세지가 0번인덱스~n-1번 인덱스까지 최신순부터있음
                addOldMsg(list)
                if (!isInitialized.value!!) {
                    isInitialized.postValue(true)
                }
            }
        )
    }

    fun loadPreviousMessages(limit: Int, handler: GetMessagesHandler?) {
        Log.e(CHECK_TAG,"loadPreviousMessages")

        if (_channel == null ||_isMessageLoading) {
            return
        }

        var oldestMessageCreatedAt = Long.MAX_VALUE
        Log.e(CHECK_TAG,"mMessageList.size : ${_messageList.size}")
        if (_messageList.size > 0) {
            oldestMessageCreatedAt = _messageList[_messageList.size - 1].createdAt
            Log.e(CHECK_TAG,"mMessageList.size>0 true")
        }
        _isMessageLoading = true
        _channel!!.getPreviousMessagesByTimestamp(oldestMessageCreatedAt, false, limit, true,
            BaseChannel.MessageTypeFilter.ALL, null, GetMessagesHandler { list, e ->
                handler?.onResult(list, e)
                _isMessageLoading = false
                if (e != null) {
                    e.printStackTrace()
                    return@GetMessagesHandler
                }
                if (list.size != 0)
                    addOldMsg(list)
            }
        )
    }

    fun sendFile(uri:Uri, context:Context){

        if(_channel==null){
            return
        }

        val info: Hashtable<String, Any?>? = getFileInfo(context, uri)
        if (info == null || info.isEmpty) {
            Toast.makeText(context, "Extracting file information failed.", Toast.LENGTH_LONG).show()
            return
        }
        val name: String? = if (info.containsKey("name")) {
            info["name"] as String?
        } else {
            "Sendbird File"
        }
        val path = info["path"] as String
        val file = File(path)
        //val mime = info["mime"] as String?
        val size = info["size"] as Int
        if (path == "") {
            Toast.makeText(context, "저장소에서 파일을 찾지 못했습니다.", Toast.LENGTH_LONG).show()
            return
        }

        isLoading.postValue(true)

        val params = FileMessageParams()
            .setFile(file)
            .setFileName(name)
            .setFileSize(size)
            //.setMimeType(mime)
        //.setFile(mFile)
        //.setThumbnailSizes(thumbnailSizes)

        _channel!!.sendFileMessage(params, BaseChannel.SendFileMessageHandler{ fileMessage, e->
            if (e != null) {
                isLoading.postValue(false)
                Log.e(ERROR_TAG,"send file error ${e.code} : ${e.message}")
                when (e.code) {
                    900020 -> {
                        //chatListener?.makeSnackBar("채팅방에 속해있지 않습니다")
                        _showDialog.postValue(Event("채팅방에 속해있지 않습니다"))
                    }
                    900100 -> {
                        //chatListener?.makeSnackBar("채팅방에서 추방당하였습니다")
                        _showDialog.postValue(Event("채팅방에서 추방당하였습니다"))
                    }
                    900041 -> {
                        //chatListener?.makeSnackBar("채팅방이 삭제되어 대화가 금지되었습니다.")
                        _showDialog.postValue(Event("채팅방이 삭제되어 대화가 금지되었습니다"))
                    }
                }
                return@SendFileMessageHandler
            }
            else{
                isLoading.postValue(false)
                addRecentMsg((fileMessage))
                //Log.e(CHECK_TAG,"thumbnail url : ${fileMessage.thumbnails.size} : "+fileMessage.thumbnails[0].url)
            }
        })
    }

    fun sendAuthFile(file:File,name:String){

        Log.e(CHECK_TAG,"start sending auth img for sendbird chatroom")

        val size = file.length().toInt()

        val params = FileMessageParams()
            .setFile(file)
            .setFileName(name)
            .setFileSize(size)
            //.setMimeType(mime)

        _channel!!.sendFileMessage(params, BaseChannel.SendFileMessageHandler{ fileMessage, e->
            if (e != null) {
                isLoading.postValue(false)
                Log.e(ERROR_TAG,"send Auth file error ${e.code} : ${e.message}")
                when(e.code) {
                    900020 -> {
                        //chatListener?.makeSnackBar("채팅방에 속해있지 않습니다")
                        _showDialog.postValue(Event("채팅방에 속해있지 않습니다"))
                    }
                    900100 -> {
                        //chatListener?.makeSnackBar("채팅방에서 추방당하였습니다")
                        _showDialog.postValue(Event("채팅방에서 추방당하였습니다"))
                    }
                    900041 -> {
                        //chatListener?.makeSnackBar("채팅방이 삭제되어 대화가 금지되었습니다.")
                        _showDialog.postValue(Event("채팅방이 삭제되어 대화가 금지되었습니다"))
                    }
                }
                file.delete()
                return@SendFileMessageHandler
            }
            else{
                isLoading.postValue(false)
                addRecentMsg((fileMessage))
                file.delete()
            }
        })
    }

    fun exitChannel() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.e(CHECK_TAG,"RoomIdx : $_roomIdx")
                val param = ChatExtentions(_roomIdx,null,null)
                val leaveChannelResponse = repository.leaveChannel(param)
                if (leaveChannelResponse.isSuccess) {
                    Log.e(CHECK_TAG, "api leaveChannel success")

                    if (leaveChannelResponse.result != null) {
                        Log.e(CHECK_TAG, "방장 나감 -> 새로운 방장 : ${leaveChannelResponse.result}")
                        //방장 넘겨줘야함
                    }
                    //SENDBIRD 체널도 나가야함
                    _channel?.leave(GroupChannel.GroupChannelLeaveHandler{ e ->
                        if(e!=null){
                            //ERROR
                            Log.e(ERROR_TAG,"SendBird leaveChannel error ${_channel?.name}...")
                        }
                        else{
                            Log.e(CHECK_TAG,"SendBird leaveChannel success -> ${_channel?.name} ")
                            chatListener?.onExitSuccess()
                        }
                    })
                    //chatListener?.onExitSuccess()
                } else {
                    Log.e(ERROR_TAG, "api leaveChannel failed message:${leaveChannelResponse.message}")
                    //chatListener?.makeSnackBar(leaveChannelResponse.message)
                    _showDialog.postValue(Event(leaveChannelResponse.message))
                }
            }catch (e:Exception){
                Log.e(ERROR_TAG,"api leaveChannel Error $e")
                //chatListener?.makeSnackBar("퇴장에 실패하였습니다")
                _showDialog.postValue(Event("퇴장에 실패하였습니다"))
            }
        }
    }

    fun reportChannel(reason:String){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.e(CHECK_TAG,"report reason : $reason")
                val param = ChatExtentions(_roomIdx,reason,null)
                val reportChannelResponse = repository.reportChannel(param)
                if(reportChannelResponse.isSuccess){
                    Log.e(CHECK_TAG, "api reportChannel success")
                    //chatListener?.makeSnackBar("정상적으로 신고가 접수되었습니다")
                    _showDialog.postValue(Event("정상적으로 신고가 접수되었습니다"))
                }
                else{
                    Log.e(ERROR_TAG, "api reportChannel failed message:${reportChannelResponse.message}")
                    //chatListener?.makeSnackBar(reportChannelResponse.message)
                    _showDialog.postValue(Event(reportChannelResponse.message))
                }
            }catch (e:Exception){
                Log.e(ERROR_TAG,"api reportChannel Error $e")
                //chatListener?.makeSnackBar("신고에 실패하였습니다")
                _showDialog.postValue(Event("신고에 실패하였습니다"))
            }
        }
    }

    fun expelMember(banMember: String, reason: String){

        var banUserIdx = 0
        var banUserId:String? = ""
        for(member in _memberInfoList){
            if(member.nickname==banMember){
                banUserIdx = member.userIdx
                banUserId = member.id
                break
            }
        }

        if(banUserIdx==0){
            Log.e(ERROR_TAG,"추방하려는 유저를 찾을 수 없습니다.")
            //chatListener?.makeSnackBar("추방하려는 유저를 찾을 수 없습니다.")
            _showDialog.postValue(Event("추방하려는 유저를 찾을 수 없습니다"))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.e(CHECK_TAG, "expel member : $banMember, reason : $reason")
                val param = ChatExtentions(_roomIdx,reason,banUserIdx)
                val expelMemberResponse = repository.expelMember(param)
                if(expelMemberResponse.isSuccess){
                    Log.e(CHECK_TAG, "api expelMember success")

                    val operatorArray:ArrayList<String> = ArrayList()
                    val id = sharedPreferencesManager.getClientID()!!
                    operatorArray.add(id)


                    _channel!!.addOperators(operatorArray, AddOperatorsHandler { e1->
                        if (e1 != null) {
                            Log.e(ERROR_TAG,"$id 가 SendBird channel operator가 되는데에 실패하였습니다:$e1")
                        } else {
                            Log.e(CHECK_TAG,"$id 가 SendBird channel operator가 되었습니다.")

                            Log.e(CHECK_TAG,"MyRole : "+_channel!!.myRole.toString()+"=?="+Member.Role.OPERATOR)

                            if (_channel!!.myRole == Member.Role.OPERATOR) {
                                _channel!!.banUserWithUserId(banUserId, "-", Int.MAX_VALUE) { e2 ->
                                    if (e2 != null) {
                                        Log.e(ERROR_TAG,"ban User Fail : $e2")
                                        // Handle error.
                                    } else {
                                        //chatListener?.makeSnackBar("정상적으로 추방되었습니다")
                                        //chatListener?.makePopup("정상적으로 추방되었습니다")
                                        Log.e(CHECK_TAG, "ban User Success")
                                        _showDialog.postValue(Event("정상적으로 추방되었습니다"))
                                    }
                                }
                            }
                            else{
                                Log.e(ERROR_TAG,"Not Operator of channel")
                            }

                        }

                    })
                    getRoomInfo()
                }
                else{
                    Log.e(ERROR_TAG, "api expelMember failed message:${expelMemberResponse.message}")
                    //chatListener?.makeSnackBar(expelMemberResponse.message)
                    //chatListener?.makePopup(expelMemberResponse.message)
                    _showDialog.postValue(Event(expelMemberResponse.message))
                }
            }catch (e:Exception){
                Log.e(ERROR_TAG,"api expelMember Error $e")
                //chatListener?.makeSnackBar("추방에 실패하였습니다")
                _showDialog.postValue(Event("추방에 실패하였습니다"))
            }
        }
    }

    fun checkChallenge(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val param = ChatExtentions(_roomIdx,null,null)
                val response = repository.checkChallenge(param)
                if(response.isSuccess){
                    chatListener?.startDiary()
                }
                else{
                    Log.e(CHECK_TAG,"challenge is not available now ${response.message}")
                    //chatListener?.makeSnackBar(response.message)
                    //chatListener?.makePopup(response.message)
                    _showDialog.postValue(Event(response.message))
                }

            }catch (e: Exception){
                Log.e(ERROR_TAG,"checkChallenge api error $e")
                //chatListener?.makeSnackBar("챌린지 확인에 실패하였습니다")
                _showDialog.postValue(Event("챌린지 확인에 실패햐였습니다"))
            }
        }
    }

    fun startChallenge(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val param = ChatExtentions(_roomIdx,null,null)
                val startChallengeResponse = repository.startChallenge(param)
                if(startChallengeResponse.isSuccess){
                    Log.e(CHECK_TAG, "api startChallenge success")
                    val result = startChallengeResponse.result
                    if(result!=null){
                        val title = result.badgeName!!
                        val url = result.badgeImageUrl!!
                        val explain = result.badgeDescription!!
                        val backColor = result.backgroundColor!!
                        chatListener?.onBadgeResponse(title,url,explain,backColor)
                    }
                    else{
                        //chatListener?.makePopup("이제 챌린지가 시작됩니다")
                        _showDialog.postValue(Event("이제 챌린지가 시작됩니다"))
                    }

                }
                else{
                    Log.e(ERROR_TAG, "api startChallenge failed message:${startChallengeResponse.message}")
                    //chatListener?.makeSnackBar(startChallengeResponse.message)
                    //chatListener?.makePopup(startChallengeResponse.message)
                    _showDialog.postValue(Event(startChallengeResponse.message))
                }
            }
            catch (e:Exception){
                Log.e(ERROR_TAG,"api startChallenge Error $e")
                //chatListener?.makeSnackBar("챌린지 시작에 실패하였습니다")
                _showDialog.postValue(Event("챌린지 시작에 실패하였습니다"))
            }
        }
    }

    private fun getFileInfo(context: Context, uri: Uri?): Hashtable<String, Any?>? {
        val cursor = context.contentResolver.query(uri!!, null, null, null, null)
        try {
            val mime = context.contentResolver.getType(uri)
            val file = File(
                //Environment.getExternalStorageDirectory().absolutePath
                context.applicationContext.filesDir,
                "sendbird"
            )
            val inputPFD =
                context.contentResolver.openFileDescriptor(uri, "r")
            var fd: FileDescriptor? = null
            if (inputPFD != null) {
                fd = inputPFD.fileDescriptor
            }
            val inputStream = FileInputStream(fd)
            file.createNewFile()
            val outputStream = FileOutputStream(file)
            var read: Int
            val bytes = ByteArray(1024)
            while (inputStream.read(bytes).also { read = it } != -1) {
                outputStream.write(bytes, 0, read)
            }
            if (cursor != null) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                val value = Hashtable<String, Any?>()
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(nameIndex)
                    val size = cursor.getLong(sizeIndex).toInt()
                    value["path"] = file.path
                    value["size"] = size
                    value["mime"] = mime
                    value["name"] = name
                }
                return value
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(e.localizedMessage, "File not found.")
            return null
        } finally {
            cursor?.close()
        }
        return null
    }

}