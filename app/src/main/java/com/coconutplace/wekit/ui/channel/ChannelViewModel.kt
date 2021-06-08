package com.coconutplace.wekit.ui.channel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coconutplace.wekit.data.entities.ChannelFilter
import com.coconutplace.wekit.data.entities.ChatExtentions
import com.coconutplace.wekit.data.entities.ChatRoom
import com.coconutplace.wekit.data.remote.channel.ChannelResponse
import com.coconutplace.wekit.data.remote.channel.listeners.ChannelListener
import com.coconutplace.wekit.data.repository.channel.ChannelRepository
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
import kotlinx.coroutines.withContext
import java.util.*

class ChannelViewModel(private val repository: ChannelRepository, private val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {

    private var channelListener: ChannelListener? = null

    private var pageForRoomList = 1 //페이징 인덱스
    private var roomList = ArrayList<ChatRoom>() //받아온 채팅방 리스트
    private var myRoomCount = 0 //내가 속한 채팅방 수(2021.6.2 기준 0또는 1이어야함)
    private var myRoomUrl:String? = null // 내가 속한
    private lateinit var myChannelResponse:ChannelResponse //내가 속한 채팅방 리스트 정보
    private var isEntering = false //한 채팅방에 들어갈때 flag
    //var status:Int = 1
    private var _filter: ChannelFilter ?= null //채팅방 필터 화면일 때 필터 기준값
    private var searchKeyWord: String?=null //채팅방 검색 화면일 때

    private val _dialogEvent = MutableLiveData<Event<Any>>() //Fragment에서 다이얼로그 띄울때 메세지 전달
    val dialogEvent: LiveData<Event<Any>>
        get() = _dialogEvent

    private val _myChannelSetEvent = MutableLiveData<Event<Unit>>() //내가 속한 채팅방이 있다는 이벤트 전달
    val myChannelSetEvent: LiveData<Event<Unit>>
        get() = _myChannelSetEvent

    fun getMyRoomCount():Int{ //'채팅방생성'Activity,'채팅방입장'Activity에 들어갈때 이미속한 방이 있는지 없는지 체크할 때 사용
        return myRoomCount
    }

    fun getID():String?{ //사용자 로그인 ID
        return sharedPreferencesManager.getClientID()
    }

    fun setChannelListener(channelListener: ChannelListener?) { //ChannelFragment에 이벤트 전달 인터페이스
        this.channelListener = channelListener
    }

    // 1:최근 채널리스트 보기 2:모든 채널리스트 보기 3: 필터링된 채널리스트 보기 4: 검색 결과 채널리스트 보기
    val liveStatus: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply {
            value = 1
        }
    }

    //채팅방 리스트
    val liveRoomList: MutableLiveData<ArrayList<ChatRoom>> by lazy{
        MutableLiveData<ArrayList<ChatRoom>>().apply {
            postValue(ArrayList<ChatRoom>())
        }
    }

    //내가속한 채팅방 이름
    val liveMyChatRoomName : MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply { postValue("") }
    }

    //내가속한 채팅방의 맴버 수
    val liveMyChatMemberCount: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply{ postValue("") }
    }

    //내가속한 채팅방 설명글
    val liveMyChatRoomExplain: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply{ postValue("") }
    }

    //내가 속한 채팅방의 기간(2주/4주)
    val liveMyChatRoomDuration: MutableLiveData<String> by lazy{
        MutableLiveData<String>().apply{ postValue("")}
    }

    //내가 속한 채팅방의 프로필 url
    val liveMyChatImgUrl: MutableLiveData<String> by lazy{
        MutableLiveData<String>()
    }

    //푸시알림으로 채팅방 입장
    fun setChannelUrlWithPush(channelUrl:String){
        if(myRoomUrl!=null){
            enterRoom()
            Log.e(CHECK_TAG,"myRoomUrl is not null -> direct enterRoom, $myRoomUrl")
        }
        else{
            viewModelScope.launch(Dispatchers.IO) {
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

    //새로고침(현재 스크린에서 status에 맞는 화면 초기화)
    fun refresh(){
        when(liveStatus.value){
            1->refreshRecentChannelList()
            2->refreshAllChannelList()
            3->refreshFilteredChannelList()
            4->refreshSearchChannelList()
        }
    }

    //필터 설정
    fun setFilter(filter:ChannelFilter){
        _filter = filter
    }
    //검색어 설정
    fun setSearchKeyWord(str: String){
        searchKeyWord = str
    }

    //현재 스크린에서 status에 맞는 다름 채팅방 페이지 요청
    fun loadNextRoomList(){
        when(liveStatus.value){
            1->loadNextRecentRoomList()
            2->loadNextAllRoomList()
            3->loadNextFilteredRoomList()
            4->loadNextSearchChannelList()
        }
    }

    //내가 속한 채팅방, 속하지 않은 채팅방 리스트 받기
    private fun refreshRecentChannelList() {
        pageForRoomList = 1
        roomList.clear()

        //내가 속한 채팅방 리스트 받기
        viewModelScope.launch(Dispatchers.IO) {
            try{
                myChannelResponse = repository.getMyChannel()
                if(myChannelResponse.isSuccess){

                    myRoomCount = myChannelResponse.result!!.chatList!!.size
                    if(myRoomCount>0){
                        val myFirstRoom = myChannelResponse.result!!.chatList!![0]
                        _myChannelSetEvent.let {
                            if(it.value == null){
                                it.postValue(Event(Unit))
                            }
                        }

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
        viewModelScope.launch(Dispatchers.IO){
            try{
                val channelListResponse = repository.getRecentChannelList(pageForRoomList)
                if(channelListResponse.isSuccess){
                    val chatRoomSize = channelListResponse.result!!.chatList!!.size
                    for(i in 0 until chatRoomSize){
                        try{
                            val room = channelListResponse.result.chatList!![i]
                            roomList.add(room)
                        }catch (e:Exception){
                            Log.e(CHECK_TAG,"room insertion failed $e")
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

    //'최근 채팅방'의 다음 페이지 받기
    private fun loadNextRecentRoomList(){
        viewModelScope.launch(Dispatchers.IO){
            try{
                val response = repository.getRecentChannelList(pageForRoomList)
                if(response.isSuccess){
                    val chatRoomSize = response.result!!.chatList!!.size
                    for(i in 0 until chatRoomSize){
                        try {
                            val room = response.result.chatList!![i]
                            roomList.add(room)
                        }catch (e:Exception){
                            Log.e(CHECK_TAG,"room insertion failed $e")
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

    //'모든 채팅방'의 리스트 초기화 및 첫번째 페이지 받기
    private fun refreshAllChannelList(){
        pageForRoomList = 1
        roomList.clear()
        viewModelScope.launch(Dispatchers.IO){
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

    //'모든 채팅방'의 다음 페이지 받기
    private fun loadNextAllRoomList(){
        viewModelScope.launch(Dispatchers.IO){
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

    //'필터된 채팅방'의 리스트 초기화 및 첫번째 페이지 받기
    private fun refreshFilteredChannelList(){
        pageForRoomList = 1
        roomList.clear()
        viewModelScope.launch(Dispatchers.IO) {
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

    //'다음 채팅방'의 다음 페이지 받기
    private fun loadNextFilteredRoomList(){
        viewModelScope.launch(Dispatchers.IO) {
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

    //'검색 채팅방'초기화 및 첫번째 페이지 받기
    private fun refreshSearchChannelList(){
        pageForRoomList = 1
        roomList.clear()
        val keyword = this.searchKeyWord
        if(keyword==null||keyword==""){
            return
        }
        viewModelScope.launch(Dispatchers.IO){
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

    //'검색 채팅방'의 다음 페이지 받기
    private fun loadNextSearchChannelList(){
        val keyword = this.searchKeyWord!!
        if(keyword.isEmpty()){
            return
        }
        viewModelScope.launch(Dispatchers.IO){
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

    //내 채팅방 cardview 클릭했을때 입장하기
    fun enterRoom(){
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
                if(this::myChannelResponse.isInitialized){
                    channelListener?.callChatActivity(channelUrl!!,myChannelResponse.result!!.chatList!![0].roomIdx)
                }
                else{
                    Log.e(CHECK_TAG,"myChannelResponse is not initialized")
                }
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

    //내가 속한 모든 채팅방 나가기(실제론 안쓰임)
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

                viewModelScope.launch(Dispatchers.IO) {
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