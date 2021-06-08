package com.coconutplace.wekit.ui.chat

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

class ChatViewModel(private val repository: ChatRepository, private val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {
    private val channelHandlerId = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT"

    private var chatListener:ChatListener? = null

    private var _channel: GroupChannel? = null //현재 채팅방의 채널 객체 데이터
    private var _channelUrl: String? = null //현재 채팅방의 채널URL
    private var _roomIdx:Int = 0 //WEKIT서버의 채팅방 인덱스 번호
    private var _messageList: ArrayList<BaseMessage> = ArrayList() //채팅방의 메세지 정보
    private var _isMessageLoading = false //메세지 전송중인지 flag
    private val _memberInfoList:ArrayList<UserInfo> = ArrayList() //채팅방 멤버 리스트
    private var _operatorUserIndex:Int = 0  //WEKIT서버에서 알려주는 방장 유저 인덱스(SendBird서버의 operator는 모든 사람이 operator임!)
    private var _nickname:String? = null
    private var _pushNotificationOn: Boolean? = null

    var isChallengeable = false //챌린지 시작 가능한지

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

    private val liveOperator: MutableLiveData<String> by lazy{
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

    fun setChatListener(chatListener: ChatListener){ //Activity에 이벤트 전달 인터페이스
        this.chatListener = chatListener
    }

    fun getMemberInfoList():ArrayList<UserInfo>{
        return _memberInfoList
    }

    fun getNickName():String{
        return sharedPreferencesManager.getNickname()
    }

    fun setPushFlag(on:Boolean){
        sharedPreferencesManager.savePushNotificationFlag(on)
    }

    fun getPushFlag():Boolean{
        return sharedPreferencesManager.getPushNotificationFLag()
    }

    fun getRoomInfo() { //drawer열 때 방 정보 불러오기
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
                        isChallengeable = isHostFlag
                    }
                    Log.e(CHECK_TAG,"$_nickname =?= ${liveOperator.value}")

                    if(roomInfo.isStart=="Y"){
                        isHostFlag = false
                    }

                    chatListener?.showStartChallengeButton(isHostFlag)//방장이고 2주방일때 챌린지 시작 버튼 보이게함

                    if (_channel!!.myRole == Member.Role.OPERATOR) {
                        _channel!!.addOperators(operatorArray) { e ->
                            if (e != null) { //방장이 나갔을때 새로운 방장이 추방 권한을 가질 수 있게 모두가 operator가 되어야함.
                                //operator 지정은 방장밖에 못함
                                Log.e(ERROR_TAG, "맴버들을 operator로 지정하는데 실패하였습니다.$e")
                            } else {
                                Log.e(CHECK_TAG, "맴버들을 operator로 지정하였습니다")
                            }
                        }
                    }
                }
                else{
                    Log.e(ERROR_TAG,"getRoomInfo fail ${roomInfoResponse.message}")
                    //chatListener?.makeSnackBar(roomInfoResponse.message)
                    when(roomInfoResponse.code.toInt()){
                        301,302 -> _showDialog.postValue(Event("계정에 문제가 발생하였습니다"))
                        303,304 -> _showDialog.postValue(Event("채팅방을 불러올 수 없습니다"))
                        305 -> _showDialog.postValue(Event("방에 소속되지 않은 사용자입니다"))
                        500 -> _showDialog.postValue(Event("서버에 문제가 발생하였습니다"))
                        else -> _showDialog.postValue(Event("알 수 없는 에러입니다"))
                    }
                    //_showDialog.postValue(Event(roomInfoResponse.message))
                }

            } catch (e: Exception) {
                Log.e(ERROR_TAG,"getRoomInfo error $e")
                _showDialog.postValue(Event("서버와의 통신에 실패하였습니다"))
            }
        }
    }

    fun init(url: String, roomIdx:Int) { //channelURL로 채널 정보 받기, roomIdx 세팅하기
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
    }

    private fun addRecentMsg(msg: BaseMessage){ //최근 메세지 추가하기
        _messageList.add(0,msg)
        chatListener?.addRecentMessage(msg)
    }

    private fun addOldMsg(msgList: List<BaseMessage>){ //이전 메세지 추가하기
        _messageList.addAll(msgList)
        chatListener?.addOldMsg(msgList)
    }

    fun addSendBirdHandler() { //센드버드 핸들러 등록(메세지)
        SendBird.addChannelHandler(channelHandlerId, object : ChannelHandler() {
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

    fun sendMsg(msg: String?) { //채팅방에 텍스트 메세지 보내기
        _channel!!.sendUserMessage(msg, SendUserMessageHandler { userMessage, e ->
            if (e != null) {
                // Error!
                Log.e(ERROR_TAG,"message send fail : $e")

                when (e.code) {
                    800200 -> {
                        _showDialog.postValue(Event("네트워크가 원활하지 않습니다."))
                    }
                    900020 -> {
                        _showDialog.postValue(Event("채팅방에 속해있지 않습니다"))
                    }
                    900100 -> {
                        _showDialog.postValue(Event("채팅방에서 추방당하셨습니다"))
                    }
                    900041 -> {
                        _showDialog.postValue(Event("채팅방이 삭제되어 대화가 금지되었습니다"))
                    }
                }
                return@SendUserMessageHandler
            }
            else{
                addRecentMsg(userMessage) //텍스트 보내기 성공시 list에 추가
            }
        })
    }

    fun refresh() {
        when {
            _messageList.size!=0 -> { //viewModel에 messageList가 이미 있을 때 어댑터에 추가
                //liveMessageList.postValue(mMessageList)
                chatListener?.addOldMsg(_messageList)
            }
            _channel == null -> { //체널 정보가 없을 때 url로 채널 객체 받아와서 메세지 20개 받기
                GroupChannel.getChannel(_channelUrl, GroupChannelGetHandler { groupChannel, e ->
                    if (e != null) {
                        // Error!
                        e.printStackTrace()
                        return@GroupChannelGetHandler
                    }
                    _channel = groupChannel
                    loadLatestMessages(DUMMY_MESSAGE_COUNT) { _, _ ->
                        //mChatAdapter.markAllMessagesAsRead();
                        chatListener?.onSendMessageSuccess()
                    }
                })
            }
            else -> { //체널 정보가 있을 때 메세지 20개 받기
                _channel!!.refresh(GroupChannelRefreshHandler { e ->
                    if (e != null) {
                        // Error!
                        e.printStackTrace()
                        return@GroupChannelRefreshHandler
                    }
                    loadLatestMessages(DUMMY_MESSAGE_COUNT) { _, _ ->
                        //mChatAdapter.markAllMessagesAsRead();
                        chatListener?.onSendMessageSuccess()
                    }
                })
            }
        }
    }

    //최근 메세지 limit개 만큼 받기
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

    //현재 가지고 있는 메시지중 가장 이전 메세지를 기준으로 이전 메세지 limit개 받기
    fun loadPreviousMessages(limit: Int, handler: GetMessagesHandler?) {
        Log.e(CHECK_TAG,"loadPreviousMessages")

        if (_channel == null ||_isMessageLoading) {
            return
        }

        var oldestMessageCreatedAt = Long.MAX_VALUE
        Log.e(CHECK_TAG,"mMessageList.size : ${_messageList.size}")
        if (_messageList.size > 0) {
            oldestMessageCreatedAt = _messageList[_messageList.size - 1].createdAt
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

    //채팅방에 파일 보내기
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
                    800200 ->{
                        _showDialog.postValue(Event("네트워크가 원활하지 않습니다"))
                    }
                    900020 -> {
                        _showDialog.postValue(Event("채팅방에 속해있지 않습니다"))
                    }
                    900100 -> {
                        _showDialog.postValue(Event("채팅방에서 추방당하였습니다"))
                    }
                    900041 -> {
                        _showDialog.postValue(Event("채팅방이 삭제되어 대화가 금지되었습니다"))
                    }
                }
                return@SendFileMessageHandler
            }
            else{
                isLoading.postValue(false)
                addRecentMsg((fileMessage))
            }
        })
    }

    //인증 사진 보내기
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
                        _showDialog.postValue(Event("채팅방에 속해있지 않습니다"))
                    }
                    900100 -> {
                        _showDialog.postValue(Event("채팅방에서 추방당하였습니다"))
                    }
                    900041 -> {
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

    //채팅방 나가기(WEKIT서버에서 나가기 성공 시 -> SendBird서버에서 나가기)
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
                    _channel?.leave { e ->
                        if (e != null) {
                            //ERROR
                            Log.e(ERROR_TAG, "SendBird leaveChannel error ${_channel?.name}...")
                        } else {
                            Log.e(CHECK_TAG, "SendBird leaveChannel success -> ${_channel?.name} ")
                            chatListener?.onExitSuccess()
                        }
                    }
                } else {
                    Log.e(ERROR_TAG, "api leaveChannel failed message:${leaveChannelResponse.message}")
                    _showDialog.postValue(Event(leaveChannelResponse.message))
                }
            }catch (e:Exception){
                Log.e(ERROR_TAG,"api leaveChannel Error $e")
                _showDialog.postValue(Event("퇴장에 실패하였습니다"))
            }
        }
    }

    //방 신고하기
    fun reportChannel(reason:String){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.e(CHECK_TAG,"report reason : $reason")
                val param = ChatExtentions(_roomIdx,reason,null)
                val reportChannelResponse = repository.reportChannel(param)
                if(reportChannelResponse.isSuccess){
                    Log.e(CHECK_TAG, "api reportChannel success")
                    _showDialog.postValue(Event("정상적으로 신고가 접수되었습니다"))
                }
                else{
                    Log.e(ERROR_TAG, "api reportChannel failed message:${reportChannelResponse.message}")
                    _showDialog.postValue(Event(reportChannelResponse.message))
                }
            }catch (e:Exception){
                Log.e(ERROR_TAG,"api reportChannel Error $e")
                _showDialog.postValue(Event("신고에 실패하였습니다"))
            }
        }
    }

    //채팅방에서 맴버 추방하기
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
                    
                    _channel!!.addOperators(operatorArray) { e1->
                        if (e1 != null) {
                            Log.e(ERROR_TAG,"$id 가 SendBird channel operator가 되는데에 실패하였습니다:$e1")
                        } else {
                            //Log.e(CHECK_TAG,"$id 가 SendBird channel operator가 되었습니다.")
                            //Log.e(CHECK_TAG,"MyRole : "+_channel!!.myRole.toString()+"=?="+Member.Role.OPERATOR)

                            if (_channel!!.myRole == Member.Role.OPERATOR) {
                                _channel!!.banUserWithUserId(banUserId, "-", Int.MAX_VALUE) { e2 ->
                                    if (e2 != null) {
                                        Log.e(ERROR_TAG,"ban User Fail : $e2")
                                        // Handle error.
                                    } else {
                                        Log.e(CHECK_TAG, "ban User Success")
                                        _showDialog.postValue(Event("정상적으로 추방되었습니다"))
                                    }
                                }
                            }
                            else{
                                Log.e(ERROR_TAG,"Not Operator of channel")
                            }
                        }
                    }
                    getRoomInfo()
                }
                else{
                    Log.e(ERROR_TAG, "api expelMember failed message:${expelMemberResponse.message}")
                    _showDialog.postValue(Event(expelMemberResponse.message))
                }
            }catch (e:Exception){
                Log.e(ERROR_TAG,"api expelMember Error $e")
                _showDialog.postValue(Event("추방에 실패하였습니다"))
            }
        }
    }

    //챌린지 가능한지 확인하고 가능하면 인증사진 보내기
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
                    _showDialog.postValue(Event(response.message))
                }

            }catch (e: Exception){
                Log.e(ERROR_TAG,"checkChallenge api error $e")
                _showDialog.postValue(Event("챌린지 확인에 실패햐였습니다"))
            }
        }
    }

    //챌린지 시작하기
    fun startChallenge(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val param = ChatExtentions(_roomIdx,null,null)
                val startChallengeResponse = repository.startChallenge(param)
                if(startChallengeResponse.isSuccess){
                    //Log.e(CHECK_TAG, "api startChallenge success")
                    val result = startChallengeResponse.result
                    if(result!=null){
                        val title = result.badgeName!!
                        val url = result.badgeImageUrl!!
                        val explain = result.badgeDescription!!
                        val backColor = result.backgroundColor!!
                        chatListener?.onBadgeResponse(title,url,explain,backColor)
                    }
                    else{
                        _showDialog.postValue(Event("이제 챌린지가 시작됩니다"))
                    }

                }
                else{
                    Log.e(ERROR_TAG, "api startChallenge failed message:${startChallengeResponse.message}")
                    _showDialog.postValue(Event(startChallengeResponse.message))
                }
            }
            catch (e:Exception){
                Log.e(ERROR_TAG,"api startChallenge Error $e")
                _showDialog.postValue(Event("챌린지 시작에 실패하였습니다"))
            }
        }
    }

    //디바이스 내 이미지 파일 Uri로부터 파일 정보 받아오기
    private fun getFileInfo(context: Context, uri: Uri?): Hashtable<String, Any?>? {
        val cursor = context.contentResolver.query(uri!!, null, null, null, null)
        try {
            val mime = context.contentResolver.getType(uri)
            val file = File(
                //Environment.getExternalStorageDirectory().absolutePath
                context.applicationContext.filesDir,
                "sendbird"
            )
            val inputPFD = context.contentResolver.openFileDescriptor(uri, "r")
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