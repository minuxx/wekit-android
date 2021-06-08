package com.coconutplace.wekit.ui.chat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.chat.listeners.ChatListener
import com.coconutplace.wekit.data.remote.chat.listeners.DialogListener
import com.coconutplace.wekit.databinding.ActivityChatBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.WekitV2Dialog
import com.coconutplace.wekit.ui.chat.dialog.*
import com.coconutplace.wekit.ui.member_gallery.MemberGalleryActivity
import com.coconutplace.wekit.ui.photo_viewer.PhotoViewerActivity
import com.coconutplace.wekit.ui.write_diary.WriteDiaryActivity
import com.coconutplace.wekit.utils.GlobalConstant.Companion.DUMMY_MESSAGE_COUNT
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_CERTIFY_DIARY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_LEAVE_CHANNEL
import com.coconutplace.wekit.utils.GlobalConstant.Companion.REQ_CODE_AUTH_IMAGE
import com.coconutplace.wekit.utils.GlobalConstant.Companion.REQ_CODE_SELECT_IMAGE
import com.coconutplace.wekit.utils.GlobalConstant.Companion.RES_CODE_AUTH_FAILURE
import com.coconutplace.wekit.utils.GlobalConstant.Companion.RES_CODE_AUTH_SUCCESS
import com.coconutplace.wekit.utils.PushUtil
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.ERROR_TAG
import com.coconutplace.wekit.utils.hideKeyboard
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.sendbird.android.BaseMessage
import com.sendbird.android.FileMessage
import com.sendbird.android.SendBird
import com.sendbird.android.SendBird.SetPushTriggerOptionHandler
import com.sendbird.android.UserMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : BaseActivity(),ChatListener, DialogListener,WekitV2Dialog.WekitV2DialogClickListener {

    private lateinit var binding: ActivityChatBinding//layout이름을 그대로 가져와야함( _ 없애고 대문자로 고쳐서 + Binding)
    private val chatViewModel: ChatViewModel by viewModel()
    private lateinit var messageAdapter: ChatMessageAdapter
    private lateinit var memberListAdapter: ChatMemberListAdapter

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        val channelUrl = intent.getStringExtra("channelUrl")!!
        val roomIdx = intent.getIntExtra("roomIdx",0)

        chatViewModel.setChatListener(this)
        chatViewModel.init(channelUrl,roomIdx)

        setupView()
        setupChatListAdapter()

        setupDrawer() //채팅방 맴버가 보이는 drawer 세팅
        setupMemberListAdapter()

        setupViewModel()

    }

    override fun onResume() {
        super.onResume()

        if (binding.chatDrawerLayout.isDrawerOpen(GravityCompat.END)) { //drawer가 열려있을 때 슬라이드로 열기 풀기
            displayMenu()
            binding.chatDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }
        else{ //drawer가 닫혀있을 때 슬라이드로 열기막기
            binding.chatDrawerLayout.setDrawerLockMode((DrawerLayout.LOCK_MODE_LOCKED_CLOSED))
        }
    }

    private fun displayMenu(){ //drawer맴버 어댑터 세팅하기
        memberListAdapter.clear()
        binding.chatNavListView.adapter = memberListAdapter

        chatViewModel.getRoomInfo()
    }

    private fun setupView(){

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        binding.lifecycleOwner = this
        binding.mChatViewModel = chatViewModel

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true //역순으로 출력

        binding.chatRecyclerview.layoutManager = linearLayoutManager

        messageAdapter = ChatMessageAdapter()

        binding.chatRecyclerview.adapter = messageAdapter
        binding.chatRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (linearLayoutManager.findLastVisibleItemPosition() == messageAdapter.itemCount-1) {
                    chatViewModel.loadPreviousMessages(DUMMY_MESSAGE_COUNT, null)
                }
            }
        })

        binding.chatSendButtonFrameLayout.setOnClickListener{
            val msg : String = binding.chatMsgEt.text.toString()
            if(msg==""){
                return@setOnClickListener
            }
            chatViewModel.sendMsg(msg)
            binding.chatMsgEt.setText("")
        }
        binding.chatSendButton.setOnClickListener{
            val msg : String = binding.chatMsgEt.text.toString()
            if(msg==""){
                return@setOnClickListener
            }
            chatViewModel.sendMsg(msg)
            binding.chatMsgEt.setText("")
        }

        binding.chatMenuButtonFrameLayout.setOnClickListener{
            binding.root.hideKeyboard()
            binding.chatDrawerLayout.openDrawer(GravityCompat.END)
            displayMenu()
        }
        binding.chatMenuButton.setOnClickListener{
            binding.root.hideKeyboard()
            binding.chatDrawerLayout.openDrawer(GravityCompat.END)
            displayMenu()
        }

        binding.chatBackButtonFrameLayout.setOnClickListener{
            finish()
        }
        binding.chatBackButton.setOnClickListener{
            finish()
        }

        binding.chatSendPictureButtonFrameLayout.setOnClickListener{
            //requestMedia()
            val imgTypeDialog = ChatImgTypeDialog(this)
            imgTypeDialog.callFunction(this)
        }
        binding.chatSendPictureButton.setOnClickListener{
            //requestMedia()
            val imgTypeDialog = ChatImgTypeDialog(this)
            imgTypeDialog.callFunction(this)
        }
    }

    private fun setupDrawer(){ //drawer세팅하기
        binding.chatDrawerLayout.addDrawerListener(object:DrawerListener{
            override fun onDrawerOpened(drawerView: View) {
                binding.chatDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
            override fun onDrawerClosed(drawerView: View) {
                binding.chatDrawerLayout.setDrawerLockMode((DrawerLayout.LOCK_MODE_LOCKED_CLOSED))
            }
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })

        binding.chatNavMenuBtn.setOnClickListener{
            val actionTypeDialog = ChatActionTypeDialog(this)
            actionTypeDialog.callFunction(this)
        }
        binding.chatNavExitBtn.setOnClickListener{
            val exitDialog = WekitV2Dialog(this, FLAG_LEAVE_CHANNEL)
            exitDialog.listener = this
            exitDialog.show(getString(R.string.chat_dialog_exit_title))
        }

        if(chatViewModel.getPushFlag()){
            binding.chatNavBellBtn.setImageResource(R.drawable.icn_bell_on)
            R.drawable.icn_bell_off
        }

        binding.chatNavBellBtn.setOnClickListener {
            if(chatViewModel.getPushFlag()){
                PushUtil.setPushNotification(false, //푸시알림 ON 하기
                    SetPushTriggerOptionHandler { e ->
                        if(e!=null){
                            Log.e(ERROR_TAG,"push notification OFF setting error $e")
                            return@SetPushTriggerOptionHandler
                        }
                        chatViewModel.setPushFlag(false)
                        binding.chatNavBellBtn.setImageResource(R.drawable.icn_bell_off)
                    })
            }
            else{
                PushUtil.setPushNotification(true, //푸시알림 OFF 하기
                    SetPushTriggerOptionHandler { e ->
                        if(e!=null){
                            Log.e(ERROR_TAG,"push notification ON setting error $e")
                            return@SetPushTriggerOptionHandler
                        }
                        chatViewModel.setPushFlag(true)
                        binding.chatNavBellBtn.setImageResource(R.drawable.icn_bell_on)
                    })
            }
        }
    }

    private fun setupChatListAdapter(){ //채팅 메세지별 클릭 이벤트 등록
        messageAdapter.setItemClickListener(object : ChatMessageAdapter.OnItemClickListener {
            override fun onUserMessageItemClick(message: UserMessage?) {
                Log.e(CHECK_TAG,"USER message 클릭함")
            }
            override fun onFileMessageItemClick(message: FileMessage?) {
                Log.e(CHECK_TAG,"File message 클릭함")
                val type = message!!.type.toLowerCase(Locale.ROOT)
                if (type.startsWith("image")) {
                    onImageMessageClicked(message)
                }
            }
            override fun onBackgroundClick() {
                Log.e(CHECK_TAG,"background item 클릭함")
                binding.root.hideKeyboard()
            }

            override fun onProfileClick(profileUrl: String?) {
                onProfileClicked(profileUrl!!)
            }

        })
    }

    private fun setupMemberListAdapter(){ //drawer의 멤버리스트 클릭 이벤트 등록
        memberListAdapter = ChatMemberListAdapter()

        memberListAdapter.setItemClickListener(object: ChatMemberListAdapter.OnItemClickListener{
            override fun onItemClick(memberInfo: ChatMemberListAdapter.MemberInfo) {
                Log.e(CHECK_TAG,memberInfo.nickName)

                val intent = Intent(this@ChatActivity,MemberGalleryActivity::class.java)
                intent.putExtra("userIdx",memberInfo.userIdx)
                intent.putExtra("roomIdx",chatViewModel.getRoomIdx())
                intent.putExtra("nickName",memberInfo.nickName)
                startActivity(intent)
            }
        })
    }

    private fun setupViewModel(){
        chatViewModel.refresh()
        chatViewModel.addSendBirdHandler()

        chatViewModel.isInitialized.observe(this,{
            if(it==true) { //초기 n개 메시지만 받았을때만 스크롤 맨아래로 이동
                binding.chatRecyclerview.scrollToPosition(0)
                Log.e(CHECK_TAG, "recyclerview position :${(messageAdapter.itemCount - 1)}")
            }
        })

        chatViewModel.liveMemberListInfo.observe(this,{
            memberListAdapter.clear()
            binding.chatNavListView.adapter = memberListAdapter

            for(member in it){
                memberListAdapter.addItem(member.countNum,member.nickname,member.type, member.todayCount, member.userIdx)
                Log.e(CHECK_TAG,"ListView addItem : ${member.nickname} 추가됨")
            }
            memberListAdapter.notifyDataSetChanged()

            Log.e(CHECK_TAG,"ListView addItem count : "+memberListAdapter.count)
        })

        chatViewModel.isLoading.observe(this,{
            if(it){
                binding.chatProgressbar.visibility = View.VISIBLE
            }
            else{
                binding.chatProgressbar.visibility = View.GONE
            }
        })

        chatViewModel.showDialog.observe(this,{ event ->
            event.getContentIfNotHandled()?.let {
                makePopup(it)
            }
        })
    }

    //채팅 이미지 클릭시 확대
    private fun onImageMessageClicked(message : FileMessage){
        val intent = Intent(this,PhotoViewerActivity::class.java)
        intent.putExtra("url", message.url)
        intent.putExtra("type", message.type)
        startActivity(intent)
    }

    //채팅 프로필 클릭시 확대
    private fun onProfileClicked(profileUrl: String){
        val intent = Intent(this,PhotoViewerActivity::class.java)
        intent.putExtra("url", profileUrl)
        startActivity(intent)
    }

    //사진 보내기 전에 갤러리열기
    private fun requestMedia() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE)
        SendBird.setAutoBackgroundDetection(false)
    }

    private fun makePopup(str: String) {
        runOnUiThread{
            showDialog(str)
        }
    }

    //채팅방 내에서 배지를 받을때(첫 인증사진 전송 등)
    override fun onBadgeResponse(badgeTitle:String,badgeUrl:String,badgeExplain:String, backgroundColor:String){
        runOnUiThread {
            val badgeDialog = ChatBadgeDialog(this)
            badgeDialog.callFunction(badgeTitle,badgeUrl,badgeExplain,backgroundColor)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQ_CODE_SELECT_IMAGE){ //일반 사진 보내기 위해 갤러리 갔다 왔을 때
            if(resultCode==Activity.RESULT_OK){
                SendBird.setAutoBackgroundDetection(true)
                val uri:Uri = data!!.data!!
                Log.e(CHECK_TAG,uri.toString())
                chatViewModel.sendFile(uri,this)
            }
        }
        else if(requestCode == REQ_CODE_AUTH_IMAGE){ //인증사진 보내기 위해 Diary갔다 왔을 때
            if(resultCode== RES_CODE_AUTH_SUCCESS){
                //writeDiaryActivity에서 받은것들 sendbird 채팅방에 보내야함
                Log.e(CHECK_TAG,"writeDiary에서 imgUrl을 받아왔습니다.")
                val urlArray:ArrayList<String> = data?.getSerializableExtra("imgList") as ArrayList<String>
//                val satisfaction:Int = data.getIntExtra("satisfaction",0)
//                val date:String? = data.getStringExtra("date")
//                val timezone:Int = data.getIntExtra("timezone",0)
//                val memo:String? = data.getStringExtra("memo")

                for(url in urlArray){
                    Log.e(CHECK_TAG,"url : $url")
                }
                //첫번째 사진만 보낸다
                if(urlArray.size>0){
                    sendFileWithUrl(urlArray[0])
                    //mChatViewModel.postDiaryWithChat(urlArray,satisfaction,date!!,timezone,memo!!)

                    val badgeTitle = data.getStringExtra("badgeTitle")
                    if(badgeTitle != null){
                        val badgeUrl = data.getStringExtra("badgeUrl")
                        val badgeExplain = data.getStringExtra("badgeExplain")
                        val backgroundColor = data.getStringExtra("backgroundColor")

                        val badgeDialog = ChatBadgeDialog(this)
                        badgeDialog.callFunction(badgeTitle,badgeUrl!!,badgeExplain!!,backgroundColor!!)
                    }
                }

            }
            else if(resultCode== RES_CODE_AUTH_FAILURE){
                Log.e(ERROR_TAG,"writeDiary 에서 imgUrl을 가져오지 못했습니다.")
                makePopup("다이어리에서 사진을 불러오지 못했습니다")
            }
        }
    }

    override fun onBackPressed() { //채팅방내에서 back button 눌렀을때 drawer열려있으면 닫기
        if (binding.chatDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.chatDrawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun onExitSuccess() { //채팅방 나가기 성공시
        finish()
    }

    override fun onSendMessageSuccess() { //채팅방에서 메세지 전송 성공시 스크롤 아래로 이동
        binding.chatRecyclerview.scrollToPosition(0)
    }

    override fun showStartChallengeButton(isHost: Boolean) { //챌린지 시작전 방장에게만 보이는 챌린지 시작버튼
        runOnUiThread{
            if(isHost){
                binding.chatNavStartChallengeBtn.visibility = View.VISIBLE
            }
            else{
                binding.chatNavStartChallengeBtn.visibility = View.INVISIBLE
            }
        }
    }

    //스크롤 위로 올려서 이전 메세지 추가
    override fun addOldMsg(msgList: List<BaseMessage>) {
        messageAdapter.addFirst(msgList)
        messageAdapter.notifyDataSetChanged()
    }

    //새롭게 도착한 메세지 추가
    override fun addRecentMessage(msg: BaseMessage) {
        messageAdapter.addLast(msg)
        messageAdapter.notifyDataSetChanged()
        binding.chatRecyclerview.scrollToPosition(0)
    }

    //사진을 전송할때 인증사진/일반사진 구분하였을때
    override fun getBackImgTypeDialog(type: Int) {
        if(type==1){ //인증 사진 보내기
            Log.e(CHECK_TAG,"인증사진 보내기 시작")
            chatViewModel.checkChallenge()
        }
        else if(type==2){ //일반 사진 보내기
            Log.e(CHECK_TAG,"일반사진 보내기 시작")
            requestMedia()
        }
    }

    override fun getBackActionTypeDialog(type: Int) {
        when(type){
            1 ->{ //방 신고하기 선택시
                val reportDialog = ChatReportDialog(this)
                reportDialog.callFunction(this)
            }
            2 ->{ //맴버 추방하기 선택시
                if(chatViewModel.isChallengeable){
                    val expelDialog = ChatExpelDialog(this,chatViewModel.getNickName())
                    expelDialog.callFunction(this, chatViewModel.getMemberInfoList())
                }
                else{
                    makePopup("방장만 추방을 할 수 있습니다.")
                }
            }
        }
    }

    //추방할 맴버 선택했을 때
    override fun getBackExpelDialog(member:String, reason:String) {
        chatViewModel.expelMember(member,reason)
    }

    //방 신고 사유 선택했을 때
    override fun getBackReportDialog(reason:String) {
        chatViewModel.reportChannel(reason)
    }

    //방 나가기 선택했을 때
    override fun getBackExitDialog(exitFlag: Boolean) {
        if(exitFlag){
            chatViewModel.exitChannel()
        }
    }

    override fun startDiary() { //인증사진보내기를 선택하였을 때
        val intent = Intent(this, WriteDiaryActivity::class.java)
        val date = CalendarDay.today()
        val month = if (date.month < 10) {"0" + date.month} else { date.month.toString() }
        val day = if (date.day < 10) {"0" + date.day} else { date.day.toString() }

        intent.putExtra("date", "${date.year}-${month}-${day}")
        intent.putExtra("flag", FLAG_CERTIFY_DIARY)
        intent.putExtra("roomIdx",chatViewModel.getRoomIdx())
        startActivityForResult(intent,REQ_CODE_AUTH_IMAGE)
    }

    private fun sendFileWithUrl(ImgUrl:String){//SendBird에 사진 전송할 때
        chatViewModel.isLoading.postValue(true)
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                try {
                    val url = URL(ImgUrl)
                    val conn: URLConnection = url.openConnection()
                    conn.connect()
                    val bis = BufferedInputStream(conn.getInputStream())
                    val imgBitmap = BitmapFactory.decodeStream(bis)
                    bis.close()

                    val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date()) + ".jpg"
                    val file = getFileFromBitmap(imgBitmap!!, fileName)
                    Log.e(CHECK_TAG, "파일이름:$fileName")
                    chatViewModel.sendAuthFile(file!!, fileName)

                } catch (e: Exception) {
                    Log.e(ERROR_TAG, "파일 만들기 에러$e")
                }
            }
        }
    }

    //사진을 전송할 때 파일 만들기
    private fun getFileFromBitmap(imgBitmap:Bitmap,fileName:String):File?{
        val storage: File = cacheDir

        val tempFile = File(storage, fileName)

        try{
            tempFile.createNewFile()
            val out = FileOutputStream(tempFile)
            imgBitmap.compress(Bitmap.CompressFormat.JPEG,100,out)
            out.close()
        }
        catch (e:Exception){
            Log.e(ERROR_TAG,"비트맵 저장 에러 $e")
        }

        val file = File(cacheDir.toString())
        val files = file.listFiles()!!
        for(imgFile:File in files){
            if(imgFile.name==fileName){
                Log.e(CHECK_TAG,"파일 찾았습니다.")
                return imgFile
            }
        }
        Log.e(ERROR_TAG,"파일을 찾지 못하였습니다")
        return null
    }

    override fun onOKClicked(flag: Int) {
        when(flag){
            FLAG_LEAVE_CHANNEL -> chatViewModel.exitChannel()
        }
    }

}

