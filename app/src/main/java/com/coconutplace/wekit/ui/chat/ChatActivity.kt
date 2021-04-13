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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.UserInfo
import com.coconutplace.wekit.data.remote.chat.listeners.ChatListener
import com.coconutplace.wekit.data.remote.chat.listeners.DialogListener
import com.coconutplace.wekit.databinding.ActivityChatBinding
import com.coconutplace.wekit.ui.chat.dialog.*
import com.coconutplace.wekit.ui.member_gallery.MemberGalleryActivity
import com.coconutplace.wekit.ui.photo_viewer.PhotoViewerActivity
import com.coconutplace.wekit.ui.write_diary.WriteDiaryActivity
import com.coconutplace.wekit.utils.GlobalConstant.Companion.DUMMY_MESSAGE_COUNT
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_CERTIFY_DIARY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.REQ_CODE_AUTH_IMAGE
import com.coconutplace.wekit.utils.GlobalConstant.Companion.REQ_CODE_SELECT_IMAGE
import com.coconutplace.wekit.utils.GlobalConstant.Companion.RES_CODE_AUTH_FAILURE
import com.coconutplace.wekit.utils.GlobalConstant.Companion.RES_CODE_AUTH_SUCCESS
import com.coconutplace.wekit.utils.PushUtil
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.ERROR_TAG
import com.coconutplace.wekit.utils.hideKeyboard
import com.coconutplace.wekit.utils.snackbar
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

class ChatActivity : AppCompatActivity(),ChatListener, DialogListener {

    private lateinit var mBinding: ActivityChatBinding//layout이름을 그대로 가져와야함( _ 없애고 대문자로 고쳐서 + Binding)
    private val mChatViewModel: ChatViewModel by viewModel()
    private lateinit var messageAdapter: ChatMessageAdapter
    private lateinit var memberListAdapter: ChatMemberListAdapter
    private lateinit var linearLayoutManager:LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        val channelUrl = intent.getStringExtra("channelUrl")!!
        val roomIdx = intent.getIntExtra("roomIdx",0)

        mChatViewModel.setChatListener(this)
        mChatViewModel.init(this,channelUrl,roomIdx)

        setupView()
        setupChatListAdapter()

        setupDrawer()
        setupMemberListAdapter()

        setupViewModel()

    }

    override fun onResume() {
        super.onResume()

        if (mBinding.chatDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            displayMenu()
            mBinding.chatDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }
        else{
            mBinding.chatDrawerLayout.setDrawerLockMode((DrawerLayout.LOCK_MODE_LOCKED_CLOSED))
        }

    }

    private fun displayMenu(){
        memberListAdapter.clear()
        mBinding.chatNavListView.adapter = memberListAdapter

        mChatViewModel.getRoomInfo()
    }

    private fun setupView(){

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        mBinding.lifecycleOwner = this
        mBinding.mChatViewModel = mChatViewModel

        linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true //역순으로 출력

        mBinding.chatRecyclerview.layoutManager = linearLayoutManager

        messageAdapter = ChatMessageAdapter(this)

        mBinding.chatRecyclerview.adapter = messageAdapter
        mBinding.chatRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (linearLayoutManager.findLastVisibleItemPosition() == messageAdapter.itemCount-1) {
                    //Log.e(CHECK_TAG,"가장 오래된 메세지를 보았습니다.")
                    mChatViewModel.loadPreviousMessages(DUMMY_MESSAGE_COUNT, null)
                }
                //Log.e(CHECK_TAG,"scroll position : ${linearLayoutManager.findLastVisibleItemPosition()} ,${messageAdapter.itemCount-1}")
            }
        })

        mBinding.chatSendButtonFrameLayout.setOnClickListener{
            val msg : String = mBinding.chatMsgEt.text.toString()
            if(msg==""){
                return@setOnClickListener
            }
            mChatViewModel.sendMsg(msg)
            mBinding.chatMsgEt.setText("")
        }
        mBinding.chatSendButton.setOnClickListener{
            val msg : String = mBinding.chatMsgEt.text.toString()
            if(msg==""){
                return@setOnClickListener
            }
            mChatViewModel.sendMsg(msg)
            mBinding.chatMsgEt.setText("")
        }

        mBinding.chatMenuButtonFrameLayout.setOnClickListener{
            mBinding.root.hideKeyboard()
            mBinding.chatDrawerLayout.openDrawer(GravityCompat.END)
            displayMenu()
        }
        mBinding.chatMenuButton.setOnClickListener{
            mBinding.root.hideKeyboard()
            mBinding.chatDrawerLayout.openDrawer(GravityCompat.END)
            displayMenu()
        }

        mBinding.chatBackButtonFrameLayout.setOnClickListener{
            finish()
        }
        mBinding.chatBackButton.setOnClickListener{
            finish()
        }

        mBinding.chatSendPictureButtonFrameLayout.setOnClickListener{
            //requestMedia()
            val imgTypeDialog = ChatImgTypeDialog(this)
            imgTypeDialog.callFunction(this)
        }
        mBinding.chatSendPictureButton.setOnClickListener{
            //requestMedia()
            val imgTypeDialog = ChatImgTypeDialog(this)
            imgTypeDialog.callFunction(this)
        }
    }

    private fun setupDrawer(){
        mBinding.chatDrawerLayout.addDrawerListener(object:DrawerListener{
            override fun onDrawerOpened(drawerView: View) {
                mBinding.chatDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
            override fun onDrawerClosed(drawerView: View) {
                mBinding.chatDrawerLayout.setDrawerLockMode((DrawerLayout.LOCK_MODE_LOCKED_CLOSED))
            }
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })

        mBinding.chatNavMenuBtn.setOnClickListener{
            val actionTypeDialog = ChatActionTypeDialog(this)
            actionTypeDialog.callFunction(this)
        }
        mBinding.chatNavExitBtn.setOnClickListener{
            //Log.e(CHECK_TAG,"나가기 버튼 누름")
            val exitDialog = ChatExitDialog(this)
            exitDialog.callFunction(this)
        }

        if(mChatViewModel.getPushFlag()){
            mBinding.chatNavBellBtn.setImageResource(R.drawable.icn_bell_on)
        }

        mBinding.chatNavBellBtn.setOnClickListener {
            if(mChatViewModel.getPushFlag()){
                PushUtil.setPushNotification(false,
                    SetPushTriggerOptionHandler { e ->
                        if(e!=null){
                            Log.e(ERROR_TAG,"push notification OFF setting error $e")
                            return@SetPushTriggerOptionHandler
                        }
                        mChatViewModel.setPushFlag(false)
                        mBinding.chatNavBellBtn.setImageResource(R.drawable.icn_bell_off)
                    })
            }
            else{
                PushUtil.setPushNotification(true,
                    SetPushTriggerOptionHandler { e ->
                        if(e!=null){
                            Log.e(ERROR_TAG,"push notification ON setting error $e")
                            return@SetPushTriggerOptionHandler
                        }
                        mChatViewModel.setPushFlag(true)
                        mBinding.chatNavBellBtn.setImageResource(R.drawable.icn_bell_on)
                    })
            }
        }
    }

    private fun setupChatListAdapter(){
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
                mBinding.root.hideKeyboard()
            }
        })
    }

    private fun setupMemberListAdapter(){
        memberListAdapter = ChatMemberListAdapter(this)

        memberListAdapter.setItemClickListener(object: ChatMemberListAdapter.OnItemClickListener{
            override fun onItemClick(memberInfo: ChatMemberListAdapter.MemberInfo) {
                Log.e(CHECK_TAG,memberInfo.nickName)

                val intent = Intent(this@ChatActivity,MemberGalleryActivity::class.java)
                intent.putExtra("userIdx",memberInfo.userIdx)
                intent.putExtra("roomIdx",mChatViewModel.getRoomIdx())
                intent.putExtra("nickName",memberInfo.nickName)
                startActivity(intent)
            }
        })
    }

    private fun setupViewModel(){
        mChatViewModel.refresh()
        mChatViewModel.addSendBirdHandler()

        mChatViewModel.isInitialized.observe(this,Observer<Boolean>{
            if(it==true) { //초기 n개 메시지만 받았을때만 스크롤 맨아래로 이동
                mBinding.chatRecyclerview.scrollToPosition(0)
                Log.e(CHECK_TAG, "recyclerview position :${(messageAdapter.itemCount - 1)}")
            }
        })

        mChatViewModel.liveMemberListInfo.observe(this,Observer<ArrayList<UserInfo>>{

            memberListAdapter.clear()
            mBinding.chatNavListView.adapter = memberListAdapter

            for(member in it){
                memberListAdapter.addItem(member.countNum,member.nickname,member.type, member.todayCount, member.userIdx)
                Log.e(CHECK_TAG,"ListView addItem : ${member.nickname} 추가됨")
            }
            memberListAdapter.notifyDataSetChanged()

            Log.e(CHECK_TAG,"ListView addItem count : "+memberListAdapter.count)
        })

        mChatViewModel.isLoading.observe(this,{
            if(it){
                mBinding.chatProgressbar.visibility = View.VISIBLE
            }
            else{
                mBinding.chatProgressbar.visibility = View.GONE
            }
        })
    }

    private fun onImageMessageClicked(message : FileMessage){
        val intent = Intent(this,PhotoViewerActivity::class.java)
        intent.putExtra("url", message.url)
        intent.putExtra("type", message.type)
        startActivity(intent)
    }

    private fun requestMedia() {

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            // If storage permissions are not granted, request permissions at run-time,
//            // as per < API 23 guidelines.
//            requestStoragePermissions()
//        } else {
//            val intent = Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            intent.type = "image/*"
//            startActivityForResult(intent,REQ_CODE_SELECT_IMAGE)
//        }

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE)
        SendBird.setAutoBackgroundDetection(false)

    }

    private fun displayBadgeDialog(){
        val badgeDialog = ChatBadgeDialog(this)
        val url = "https://firebasestorage.googleapis.com/v0/b/wekit-a56e6.appspot.com/o/certification-diary%2F9298346c-6550-4e5a-bd25-440f46c7f016.jpg?alt=media&token=bfd29b70-36b7-400d-a88a-e4c24fafabc1"
        badgeDialog.callFunction("나의 선물",url,"헬리콥터, 비행기, 공항, 드론, 철새, 기러기 등등 도착하였습니다","#ff0000")
    }

    override fun onBadgeResponse(badgeTitle:String,badgeUrl:String,badgeExplain:String, backgroundColor:String){
        runOnUiThread {
            val badgeDialog = ChatBadgeDialog(this)
            badgeDialog.callFunction(badgeTitle,badgeUrl,badgeExplain,backgroundColor)
        }
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQ_CODE_SELECT_IMAGE){
            if(resultCode==Activity.RESULT_OK){
                SendBird.setAutoBackgroundDetection(true)
                val uri:Uri = data!!.data!!
                Log.e(CHECK_TAG,uri.toString())
                mChatViewModel.sendFile(uri)
            }
        }
        else if(requestCode == REQ_CODE_AUTH_IMAGE){
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
                val message:String = data?.getStringExtra("message")!!
                makeSnackBar(message)
            }
        }
    }

    override fun onBackPressed() {
        if (mBinding.chatDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mBinding.chatDrawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun onExitSuccess() {
        finish()
    }

    override fun onSendMessageSuccess() {
        mBinding.chatRecyclerview.scrollToPosition(0)
    }

    override fun showStartChallengeButton(isHost: Boolean) {
        runOnUiThread(Runnable {
            if(isHost){
                mBinding.chatNavStartChallengeBtn.visibility = View.VISIBLE
            }
            else{
                mBinding.chatNavStartChallengeBtn.visibility = View.INVISIBLE
            }
        })
    }

    override fun makeSnackBar(str: String) {
        runOnUiThread {
            mBinding.root.snackbar(str)
        }
    }

    override fun addOldMsg(msgList: List<BaseMessage>) {
        messageAdapter.addFirst(msgList)
        messageAdapter.notifyDataSetChanged()
    }

    override fun addRecentMessage(msg: BaseMessage) {
        messageAdapter.addLast(msg)
        messageAdapter.notifyDataSetChanged()
        mBinding.chatRecyclerview.scrollToPosition(0)
    }

    override fun getBackImgTypeDialog(type: Int) {
        if(type==1){ //인증 사진 보내기
            Log.e(CHECK_TAG,"인증사진 보내기 시작")
            mChatViewModel.checkChallenge()
        }
        else if(type==2){ //일반 사진 보내기
            Log.e(CHECK_TAG,"일반사진 보내기 시작")
            requestMedia()
        }
    }

    override fun getBackActionTypeDialog(type: Int) {
        if(type==1){ //신고하기
            val reportDialog = ChatReportDialog(this)
            reportDialog.callFunction(this)
        }
        else if(type==2){ //추방하기
            val expelDialog = ChatExpelDialog(this,mChatViewModel.getNickName())
            expelDialog.callFunction(this, mChatViewModel.getMemberInfoList())
        }
    }

    override fun getBackExpelDialog(member:String, reason:String) {
        mChatViewModel.expelMember(member,reason)
    }

    override fun getBackReportDialog(reason:String) {
        mChatViewModel.reportChannel(reason)
    }

    override fun getBackExitDialog(exitFlag: Boolean) {
        if(exitFlag){
            mChatViewModel.exitChannel()
        }
    }

    override fun startDiary() {
        startWriteDiaryActivity()
    }

    private fun startWriteDiaryActivity(){
        val intent = Intent(this, WriteDiaryActivity::class.java)
        val date = CalendarDay.today()
        val month = if (date.month < 10) {"0" + date.month} else { date.month.toString() }
        val day = if (date.day < 10) {"0" + date.day} else { date.day.toString() }

        intent.putExtra("date", "${date.year}-${month}-${day}")
        intent.putExtra("flag", FLAG_CERTIFY_DIARY)
        intent.putExtra("roomIdx",mChatViewModel.getRoomIdx())
        startActivityForResult(intent,REQ_CODE_AUTH_IMAGE)
    }

    private fun sendFileWithUrl(ImgUrl:String){
        mChatViewModel.isLoading.postValue(true)
        var imgBitmap: Bitmap? = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.e(CHECK_TAG, "progress 1")
                val url: URL = URL(ImgUrl)
                val conn: URLConnection = url.openConnection()
                conn.connect()
                Log.e(CHECK_TAG, "progress 2")
                val bis: BufferedInputStream = BufferedInputStream(conn.getInputStream())
                Log.e(CHECK_TAG, "progress 3")
                imgBitmap = BitmapFactory.decodeStream(bis)
                Log.e(CHECK_TAG, "progress 4")
                bis.close()

                val fileName = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".jpg"
                val file = getFileFromBitmap(imgBitmap!!, fileName)

                Log.e(CHECK_TAG, "파일이름:$fileName")

                mChatViewModel.sendAuthFile(file!!, fileName)

            } catch (e: Exception) {
                Log.e(ERROR_TAG, "파일 만들기 에러$e")
            }
        }

    }
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
        val files = file.listFiles()
        for(imgFile:File in files){
            if(imgFile.name==fileName){
                Log.e(CHECK_TAG,"파일 찾았습니다.")
                return imgFile
            }
        }
        Log.e(ERROR_TAG,"파일을 찾지 못하였습니다")
        return null
    }

}

