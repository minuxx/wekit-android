package com.coconutplace.wekit.ui.channel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coconutplace.wekit.data.entities.ChannelFilter
import com.coconutplace.wekit.data.entities.ChatExtentions
import com.coconutplace.wekit.data.entities.ChatRoom
import com.coconutplace.wekit.data.remote.channel.ChannelResponse
import com.coconutplace.wekit.data.remote.channel.listeners.ChannelListener
import com.coconutplace.wekit.data.repository.channel.ChannelRepository
import com.coconutplace.wekit.utils.ApiException
import com.coconutplace.wekit.utils.Event
import com.coconutplace.wekit.utils.SharedPreferencesManager
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.ERROR_TAG
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannelListQuery
import com.sendbird.android.SendBird
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ChannelViewModel(private val repository: ChannelRepository, private val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {

    private var channelListener: ChannelListener? = null

    private var pageForRoomList = 1
    private lateinit var roomList: ArrayList<ChatRoom>
    private var myRoomCount = 0
    private var myRoomUrl:String? = null
    private lateinit var myChannelResponse:ChannelResponse //내가 속한 채팅방 리스트 정보
    private var isEntering = false
    //var status:Int = 1
    private var _filter: ChannelFilter ?= null
    private var searchKeyWord: String?=null

    private val _dialogEvent = MutableLiveData<Event<Any>>()
    val dialogEvent: LiveData<Event<Any>>
        get() = _dialogEvent

    fun getMyRoomCount():Int{
        return myRoomCount
    }

    fun getID():String?{
        return sharedPreferencesManager.getClientID()
    }

    fun setChannelListener(channelListener: ChannelListener?) {
        this.channelListener = channelListener
    }

    // 1:최근 채널리스트 보기 2:모든 채널리스트 보기 3: 필터링된 채널리스트 보기 4: 검색 결과 채널리스트 보기
    val liveStatus: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply {
            value = 1
        }
    }

    val liveRoomList: MutableLiveData<ArrayList<ChatRoom>> by lazy{
        MutableLiveData<ArrayList<ChatRoom>>().apply {
            postValue(ArrayList<ChatRoom>())
        }
    }

    val liveMyChatRoomName : MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply { postValue("") }
    }
    val liveMyChatMemberCount: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply{ postValue("") }
    }
    val liveMyChatRoomExplain: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply{ postValue("") }
    }
    val liveMyChatRoomDuration: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply{ postValue("")}
    }
    val liveMyChatImgUrl: MutableLiveData<String> by lazy{
        MutableLiveData<String>()
    }

    fun setChannelUrlWithPush(channelUrl:String){

        if(myRoomUrl!=null){
            enterRoom()
        }
        else{
            CoroutineScope(Dispatchers.IO).launch {
                try{
                    val myChannelResponse = repository.getMyChannel()
                    if(myChannelResponse.isSuccess){
                        myRoomCount = myChannelResponse.result!!.chatList!!.size
                        if(myRoomCount>0){
                            val myFirstRoom = myChannelResponse.result.chatList!![0]
                            myRoomUrl = myFirstRoom.chatUrl
                            enterRoom()
                        }
                        else{//속한 채팅방 없을 때
                            Log.e(ERROR_TAG,"센드버드엔 $channelUrl 속해있지만 WEKIT서버엔 속해있지 않습니다.")
                        }
                    }
                    else{
                        Log.e(ERROR_TAG,"myChannel failed ${myChannelResponse.message}")
                    }
                }catch (e:Exception){
                    Log.e(ERROR_TAG,"myChannel Error : $e")
                }
            }
        }
    }

    fun init() {
        roomList = ArrayList()

    }

    fun refresh(){
        when(liveStatus.value){
            1->refreshRecentChannelList()
            2->refreshAllChannelList()
            3->refreshFilteredChannelList()
            4->refreshSearchChannelList()
        }
    }

    fun setFilter(filter:ChannelFilter){
        _filter = filter
    }
    fun setSearchKeyWord(str: String){
        searchKeyWord = str
    }

    fun loadNextRoomList(){
        when(liveStatus.value){
            1->loadNextRecentRoomList()
            2->loadNextAllRoomList()
            3->loadNextFilteredRoomList()
            4->loadNextSearchChannelList()
        }
    }

    private fun refreshRecentChannelList() {
        Log.e(CHECK_TAG,"jwt:"+sharedPreferencesManager.getJwtToken()!!)
        pageForRoomList = 1
        roomList.clear()

        //내가 속한 채팅방 리스트 받기
        CoroutineScope(Dispatchers.IO).launch {
            try{
                myChannelResponse = repository.getMyChannel()
                if(myChannelResponse.isSuccess){
                    Log.e(CHECK_TAG,"myChannel success")

                    myRoomCount = myChannelResponse.result!!.chatList!!.size
                    Log.e(CHECK_TAG,"myRoomCount : $myRoomCount")
                    if(myRoomCount>0){
                        val myFirstRoom = myChannelResponse.result!!.chatList!![0]

                        liveMyChatRoomName.postValue(myFirstRoom.roomName) //방 이름
                        liveMyChatMemberCount.postValue("${myFirstRoom.currentNum}/${myFirstRoom.maxLimit}") //현재 인원/최대 인원
                        liveMyChatRoomExplain.postValue(myFirstRoom.chatDescription)//방 설명
                        liveMyChatRoomDuration.postValue(myFirstRoom.roomTerm)//방 기간
                        if(myFirstRoom.roomTerm=="2주방"){
                            liveMyChatRoomDuration.postValue("2주 도전방")
                        }
                        else if(myFirstRoom.roomTerm=="한달방"){
                            liveMyChatRoomDuration.postValue("한달 도전방")
                        }
                        liveMyChatImgUrl.postValue(myFirstRoom.chatRoomImg)//내가 속한 채팅방 이미지 Url
                        myRoomUrl = myFirstRoom.chatUrl
                        channelListener?.showCardView(true)

                    }
                    else{//속한 채팅방 없을 때
                        liveMyChatRoomExplain.postValue("")
                        liveMyChatMemberCount.postValue("")
                        liveMyChatRoomDuration.postValue("")
                        liveMyChatRoomName.postValue("")
                        liveMyChatImgUrl.postValue("none")
                        channelListener?.showCardView(false)
                    }
                }
                else{
                    Log.e(ERROR_TAG,"myChannel failed : ${myChannelResponse.message}")
                    _dialogEvent.postValue(Event(404))
                }
            }
            catch (e:Exception){
                Log.e(ERROR_TAG,"myChannel Error : $e")
                _dialogEvent.postValue(Event(404))
            }
        }

        //채팅방 리스트 받기
        CoroutineScope(Dispatchers.IO).launch{
            try{
                val channelListResponse = repository.getRecentChannelList(pageForRoomList)
                if(channelListResponse.isSuccess){
                    //Log.e(CHECK_TAG, "roomList success response : ${channelListResponse.result}")

                    val chatRoomSize = channelListResponse.result!!.chatList!!.size
                    for(i in 0 until chatRoomSize){
                        try{
                            val room = channelListResponse.result.chatList!![i]
                            roomList.add(room)
                            //Log.e(CHECK_TAG,"loop : $i")
                        }catch (e:Exception){
                            Log.e(CHECK_TAG,"room 넣기 실패")
                        }

                    }
                    liveRoomList.postValue(roomList)
                    pageForRoomList++
                }
                else{
                    Log.e(CHECK_TAG,"getRecentRoomList Fail, message : "+channelListResponse.message)
                    if(channelListResponse.code == 404)
                        _dialogEvent.postValue(Event(404))
                }
            }catch (e:Exception){
                Log.e(ERROR_TAG,"getRecentRoomList Error : $e")
                _dialogEvent.postValue(Event(404))
            }
        }
    }

    private fun loadNextRecentRoomList(){
        CoroutineScope(Dispatchers.IO).launch{
            try{
                val response = repository.getRecentChannelList(pageForRoomList)
                if(response.isSuccess){
                    val chatRoomSize = response.result!!.chatList!!.size
                    for(i in 0 until chatRoomSize){
                        try {
                            val room = response.result.chatList!![i]
                            roomList.add(room)
                        }catch (e:Exception){
                            Log.e(CHECK_TAG,"room 넣기 실패")
                        }
                    }
                    liveRoomList.postValue(roomList)
                    pageForRoomList++
                }
                else{
                    Log.e(CHECK_TAG,"getRecentRoomList Fail, message : "+response.message)
                }

            }catch (e:Exception){
                Log.e(ERROR_TAG,"getRecentRoomList Error : $e")
                _dialogEvent.postValue(Event(404))
            }
        }
    }

    private fun refreshAllChannelList(){
        pageForRoomList = 1
        roomList.clear()
        CoroutineScope(Dispatchers.IO).launch{
            try{
                val response = repository.getAllChannelList(pageForRoomList)
                if(response.isSuccess){
                    val chatRoomSize = response.result!!.chatList!!.size
                    for(i in 0 until chatRoomSize){
                        try {
                            val room = response.result.chatList!![i]
                            roomList.add(room)
                        }catch (e:Exception){
                            Log.e(CHECK_TAG,"room 넣기 실패")
                        }
                    }
                    liveRoomList.postValue(roomList)
                    pageForRoomList++
                }
                else{
                    Log.e(CHECK_TAG,"getAllRoomList Fail, message : "+response.message)
                }

            }catch (e:Exception){
                Log.e(ERROR_TAG,"getAllRoomList Error : $e")
                _dialogEvent.postValue(Event(404))
            }
        }
    }

    private fun loadNextAllRoomList(){
        CoroutineScope(Dispatchers.IO).launch{
            try{
                val response = repository.getAllChannelList(pageForRoomList)
                if(response.isSuccess){
                    Log.e(CHECK_TAG, "roomList success response : ${response.result}")

                    val chatRoomSize = response.result!!.chatList!!.size
                    for(i in 0 until chatRoomSize){
                        try{
                            val room = response.result.chatList!![i]
                            roomList.add(room)
                            //Log.e(CHECK_TAG,"loop : $i")
                        }catch (e:Exception){
                            Log.e(CHECK_TAG,"room 넣기 실패")                        }

                    }
                    liveRoomList.postValue(roomList)
                    pageForRoomList++
                }
                else{
                    Log.e(CHECK_TAG,"getAllRoomList Fail, message : "+response.message)                }
            }catch (e:Exception){
                Log.e(ERROR_TAG,"getAllRoomList Error : $e")
                _dialogEvent.postValue(Event(404))
            }
        }
    }

    private fun refreshFilteredChannelList(){
        pageForRoomList = 1
        roomList.clear()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val roomTerm= if(_filter!!.isTwoWeek){ 0 }
                    else{ 1 }
                val isStart = if(_filter!!.isOngoing){ 1 }
                    else{ 0 }
                val response = repository.getFilteredChannelList(
                    _filter!!.authCount,roomTerm,_filter!!.memberCount,isStart,pageForRoomList
                )

                if(response.isSuccess){
                    val chatRoomSize = response.result!!.chatList!!.size
                    for(i in 0 until chatRoomSize){
                        try {
                            val room = response.result.chatList!![i]
                            roomList.add(room)
                        }catch (e:Exception){
                            Log.e(CHECK_TAG,"room 넣기 실패")
                        }
                    }
                    liveRoomList.postValue(roomList)
                    pageForRoomList++
                }
                else{
                    Log.e(CHECK_TAG,"getFilteredRoomList Fail, message : "+response.message)
                }

            }catch (e:Exception){
                Log.e(ERROR_TAG,"getFilteredRoomList Error : $e")
                _dialogEvent.postValue(Event(404))
            }
        }
    }

    private fun loadNextFilteredRoomList(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val roomTerm= if(_filter!!.isTwoWeek){ 0 }
                else{ 1 }
                val isStart = if(_filter!!.isOngoing){ 1 }
                else{ 0 }
                val response = repository.getFilteredChannelList(
                    _filter!!.authCount,roomTerm,_filter!!.memberCount,isStart,pageForRoomList
                )

                if(response.isSuccess){
                    val chatRoomSize = response.result!!.chatList!!.size
                    for(i in 0 until chatRoomSize){
                        try {
                            val room = response.result.chatList!![i]
                            roomList.add(room)
                        }catch (e:Exception){
                            Log.e(CHECK_TAG,"room 넣기 실패")
                        }
                    }
                    liveRoomList.postValue(roomList)
                    pageForRoomList++
                }
                else{
                    Log.e(CHECK_TAG,"getFilteredRoomList Fail, message : "+response.message)
                }

            }catch (e:Exception){
                Log.e(ERROR_TAG,"getFilteredRoomList Error : $e")
                _dialogEvent.postValue(Event(404))
            }
        }
    }
    private fun refreshSearchChannelList(){
        pageForRoomList = 1
        roomList.clear()
        val keyword = this.searchKeyWord
        if(keyword==null||keyword==""){
            return
        }
        CoroutineScope(Dispatchers.IO).launch{
            try {
                val response = repository.getSearchChannelList(keyword,pageForRoomList)
                if(response.isSuccess){
                    val chatRoomSize = response.result!!.chatList!!.size
                    for(i in 0 until chatRoomSize){
                        try {
                            val room = response.result.chatList!![i]
                            roomList.add(room)
                        }catch (e:Exception){
                            Log.e(CHECK_TAG,"room 넣기 실패")
                        }
                    }
                    Log.e(CHECK_TAG,"getSearchChannelList Success ${response.result}")
                    if(chatRoomSize==0){
                        channelListener?.noChannelSearched()
                    }
                    else{
                        liveRoomList.postValue(roomList)
                        pageForRoomList++
                    }

                }
                else{
                    Log.e(CHECK_TAG,"getSearchChannelList Fail")
                }
            }
            catch (e: Exception){
                Log.e(ERROR_TAG,"getSearchChannelList Error : $e")
                _dialogEvent.postValue(Event(404))
            }
        }

    }
    private fun loadNextSearchChannelList(){
        val keyword = this.searchKeyWord!!
        if(keyword.isEmpty()){
            return
        }
        CoroutineScope(Dispatchers.IO).launch{
            try {
                val response = repository.getSearchChannelList(keyword,pageForRoomList)
                if(response.isSuccess){
                    val chatRoomSize = response.result!!.chatList!!.size
                    for(i in 0 until chatRoomSize){
                        try {
                            val room = response.result.chatList!![i]
                            roomList.add(room)
                        }catch (e:Exception){
                            Log.e(CHECK_TAG,"room 넣기 실패")
                        }
                    }
                    liveRoomList.postValue(roomList)
                    pageForRoomList++
                }
                else{
                    Log.e(CHECK_TAG,"getSearchChannelList Fail")
                }
            }
            catch (e: Exception){
                Log.e(ERROR_TAG,"getSearchChannelList Error : $e")
                _dialogEvent.postValue(Event(404))
            }
        }
    }

    fun enterRoom(){ //내 채팅방 cardview 클릭했을때

        if(isEntering){
            Log.e(CHECK_TAG,"이미 entering 진행중입니다")
            return
        }
        isEntering = true

        val channelUrl = myRoomUrl
        if(myRoomCount==0){
            Log.e(CHECK_TAG,"속한 채팅방이 없습니다")
            isEntering = false
            return
        }
        Log.e(CHECK_TAG, "내 채팅방 : $channelUrl")

        GroupChannel.getChannel(channelUrl, GroupChannel.GroupChannelGetHandler { groupChannel, e ->
            if (e != null) {
                // Error!
                channelListener?.makeSnackBar("존재하지 않는 링크입니다")
                Log.e(ERROR_TAG, "존재하지 않는 센드버드 Url입니다 : $e")
                isEntering = false
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
                Log.e(CHECK_TAG, "is member")
                isEntering = false
                channelListener?.callChatActivity(channelUrl!!,myChannelResponse.result!!.chatList!![0].roomIdx)
            }
            else {
                Log.e(CHECK_TAG, "is not member")
                groupChannel.join { e2 ->
                    if (e2 != null) {
                        Log.e(ERROR_TAG,"SendBird cannot Join Error : $e")
                        isEntering = false
                        if(e2.code==400750){
                            channelListener?.makeSnackBar("추방당한 채팅방입니다")
                        }
                    } else {
                        isEntering = false
                        channelListener?.callChatActivity(channelUrl!!,myChannelResponse.result!!.chatList!![0].roomIdx)
                    }
                }
            }
        })
    }

    fun exitAllChatRoom(ID:String){
        val listQuery: GroupChannelListQuery = GroupChannel.createMyGroupChannelListQuery()
        val userIds: MutableList<String> = ArrayList()
        userIds.add(ID)
        listQuery.userIdsExactFilter = userIds
        listQuery.next { list, e ->
            if (e != null) {
                Log.e(ERROR_TAG, "listQueryFail...$e")
            }
            Log.e(CHECK_TAG, "number of SendBird channels : ${list.size}")

            for (channel in list) {
                channel.leave { e2 ->
                    if (e2 != null) {
                        //ERROR
                        Log.e(ERROR_TAG, "SendBird leaving channel error ${channel.name}...")
                    } else {
                        Log.e(CHECK_TAG, "SendBird leaving success -> ${channel.name} ")
                    }
                }
            }
        }

        //서버 api에서 채팅방 나가기
        if(myChannelResponse.isSuccess){
            val myList = myChannelResponse.result!!.chatList
            val myChannelSize:Int = myChannelResponse.result!!.chatList?.size!!
            Log.e(CHECK_TAG,"number of our server chatRoom : $myChannelSize")
            for(i in 0 until myChannelSize){

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Log.e(CHECK_TAG,"roomIdx : ${myList!![i].roomIdx}")
                        val param = ChatExtentions(myList[i].roomIdx, null, null)
                        val leaveChannelResponse = repository.leaveChannel(param)
                        if(leaveChannelResponse.isSuccess) {
                            Log.e(CHECK_TAG, "leaveChannel success")

                            if(leaveChannelResponse.result!=null){
                                Log.e(CHECK_TAG,leaveChannelResponse.result.toString())
                                //방장 넘겨줘야함 but 여기선 임시로 만든 reset이니까 방장 주는건 생략
                            }
                        }
                        else{
                            Log.e(ERROR_TAG,"leaveChannel failed message:${leaveChannelResponse.message}")
                        }

                    }catch (e:Exception){
                        Log.e(ERROR_TAG,"leaveChannel Error $e")
                        _dialogEvent.postValue(Event(404))
                    }
                }

            }

        }
    }

}