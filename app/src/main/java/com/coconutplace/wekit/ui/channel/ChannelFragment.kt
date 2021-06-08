package com.coconutplace.wekit.ui.channel

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import co.lujun.androidtagview.TagView
import com.bumptech.glide.Glide
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.ChannelFilter
import com.coconutplace.wekit.data.remote.channel.listeners.ChannelListener
import com.coconutplace.wekit.databinding.FragmentChannelBinding
import com.coconutplace.wekit.ui.BaseFragment
import com.coconutplace.wekit.ui.channel_filter.ChannelFilterActivity
import com.coconutplace.wekit.ui.chat.ChatActivity
import com.coconutplace.wekit.ui.chat.dialog.ChatBadgeDialog
import com.coconutplace.wekit.ui.create_channel.CreateChannelActivity
import com.coconutplace.wekit.ui.enter_channel.EnterChannelActivity
import com.coconutplace.wekit.ui.main.MainActivity
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.ERROR_TAG
import com.coconutplace.wekit.utils.hideKeyboard
import com.coconutplace.wekit.utils.snackbar
import com.github.mmin18.widget.RealtimeBlurView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChannelFragment : BaseFragment(), ChannelListener, BackPressListener {

    private lateinit var mBinding: FragmentChannelBinding
    private val mChannelViewModel: ChannelViewModel by viewModel()

    private lateinit var adapter: ChannelRecyclerAdapter
    private lateinit var mSwipeRefresh: SwipeRefreshLayout

    private var id:String? = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        mBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_channel,container,false)
        mBinding.lifecycleOwner = activity
        mBinding.mChannelViewModel = mChannelViewModel
        mChannelViewModel.setChannelListener(this)

        val view = mBinding.root
        
        id = mChannelViewModel.getID()
        if(id==null||id==""){
            Log.e(ERROR_TAG,"sharedpreference에 저장된 아이디가 없습니다.")
        }
        else{
            Log.e(CHECK_TAG,"ID : $id")
        }

        setupView()
        setupViewModel()

        setUpChannelListAdapter()

        return view
    }

    override fun onResume() {
        super.onResume()
        mBinding.channelBlurView.visibility = RealtimeBlurView.INVISIBLE
        when(mChannelViewModel.liveStatus.value){
            1-> setRecentChannelView()
            2-> setAllChannelView()
            3-> setFilteredChannelView()
            4-> setSearchChannelView()
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.channelBlurView.bringToFront()
        mBinding.channelBlurView.visibility = RealtimeBlurView.VISIBLE
    }

    private fun setupView(){
        val channelRecyclerView: RecyclerView = mBinding.myRecyclerView
        val linearLayoutManager = LinearLayoutManager(context)
        channelRecyclerView.layoutManager = linearLayoutManager

        adapter = ChannelRecyclerAdapter()
        channelRecyclerView.adapter = adapter

        mBinding.channelFullScrollview.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, _, _, _ ->
            val view: View = v.getChildAt(v.childCount - 1)
            val diff: Int = view.bottom - (v.height + v.scrollY)
            if(diff==0){
                Log.e(CHECK_TAG, "BOTTOM SCROLL")
                mChannelViewModel.loadNextRoomList()
            }
        })

        mSwipeRefresh = mBinding.channelSwipeLayout
        mSwipeRefresh.setOnRefreshListener {
            mSwipeRefresh.isRefreshing = true
            mChannelViewModel.refresh()
            if (mSwipeRefresh.isRefreshing) {
                mSwipeRefresh.isRefreshing = false
            }
        }

        mBinding.channelPlusIc.setOnClickListener{
            if(mChannelViewModel.getMyRoomCount()>0){
                mBinding.channelBlurView.visibility = RealtimeBlurView.VISIBLE

                val intent = Intent(context,CreateChannelActivity::class.java)
                intent.putExtra("createFlag",false)
                startActivityForResult(intent,100)

                return@setOnClickListener
            }

            //흐림 정도 조절
            //mBinding.channelBlurView.setBlurRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10f,resources.displayMetrics))
            mBinding.channelBlurView.visibility = RealtimeBlurView.VISIBLE

            val intent = Intent(context,CreateChannelActivity::class.java)
            intent.putExtra("createFlag",true)
            startActivityForResult(intent,100)
        }

        mBinding.channelResetBtn.setOnClickListener{
            mChannelViewModel.exitAllChatRoom(id!!)
        }

        mBinding.channelCancelBtn.setOnClickListener {
            mBinding.channelSearchEt.setText("")
            mChannelViewModel.setSearchKeyWord("")
            adapter.clear()
            mBinding.channelExampleTagLayout.visibility = View.VISIBLE
            mBinding.channelExampleTagLayout.bringToFront()
        }

        mBinding.channelSearchBtn.setOnClickListener {
            val keyword = mBinding.channelSearchEt.text.toString()
            if(keyword==""){
                makeTopSnackbar("검색어를 입력해주세요")
                //makeSnackBar("검색어를 입력해주세요")
                return@setOnClickListener
            }
            mBinding.channelExampleTagLayout.visibility = View.GONE
            mChannelViewModel.setSearchKeyWord(keyword)
            mChannelViewModel.refresh()
        }

        mBinding.channelLookAllRoomText.setOnClickListener {
            mChannelViewModel.liveStatus.value = 2
            setAllChannelView()
        }

        mBinding.channelBackToMainFrameLayout.setOnClickListener {
            mChannelViewModel.liveStatus.value = 1
            setRecentChannelView()
            mBinding.root.hideKeyboard()
        }

        mBinding.channelSearchIc.setOnClickListener {
            mChannelViewModel.liveStatus.value = 4
            setSearchChannelView()
        }

        mBinding.channelFilterIc.setOnClickListener {
            val intent = Intent(context,ChannelFilterActivity::class.java)
            startActivityForResult(intent,101)
        }

        addTagSample()
    }

    private fun setRecentChannelView(){ //status 1 상태
        adapter.clear()
        mBinding.channelListText.text = "채팅방 목록"
        mBinding.channelListText.visibility = View.GONE
        mBinding.channelMyChatRoomText.visibility = View.VISIBLE
        mBinding.channelMychattingroomCardview.visibility = View.VISIBLE
        mBinding.channelNoBelongedLayout.visibility = View.VISIBLE
        mBinding.channelAllBelongedLayout.visibility = View.GONE
        mBinding.channelBackToMainButton.visibility = View.GONE
        mBinding.channelGrayUnderbarView.visibility = View.VISIBLE

        mBinding.channelSearchIc.visibility = View.VISIBLE
        mBinding.channelSearchEdittextLayout.visibility = View.GONE
        mBinding.channelExampleTagLayout.visibility = View.GONE
        mBinding.channelLogoIv.visibility = View.VISIBLE
        mChannelViewModel.refresh()
    }
    private fun setAllChannelView(){ //status 2 상태
        adapter.clear()
        mBinding.channelListText.text = "모든 채팅방"
        mBinding.channelListText.visibility = View.VISIBLE
        mBinding.channelMyChatRoomText.visibility = View.GONE
        mBinding.channelMychattingroomCardview.visibility = View.GONE
        mBinding.channelNoBelongedLayout.visibility = View.GONE
        mBinding.channelAllBelongedLayout.visibility = View.VISIBLE
        mBinding.channelBackToMainButton.visibility = View.VISIBLE
        mBinding.channelGrayUnderbarView.visibility = View.VISIBLE

        mBinding.channelSearchIc.visibility = View.VISIBLE
        mBinding.channelSearchEdittextLayout.visibility = View.GONE
        mBinding.channelExampleTagLayout.visibility = View.GONE
        mBinding.channelLogoIv.visibility = View.GONE
        mChannelViewModel.refresh()
    }
    private fun setFilteredChannelView(){ //status 3 상태
        adapter.clear()
        mBinding.channelListText.text = "필터된 채팅방"
        mBinding.channelListText.visibility = View.VISIBLE
        mBinding.channelMyChatRoomText.visibility = View.GONE
        mBinding.channelMychattingroomCardview.visibility = View.GONE
        mBinding.channelNoBelongedLayout.visibility = View.GONE
        mBinding.channelAllBelongedLayout.visibility = View.VISIBLE
        mBinding.channelBackToMainButton.visibility = View.VISIBLE
        mBinding.channelGrayUnderbarView.visibility = View.VISIBLE

        mBinding.channelSearchIc.visibility = View.VISIBLE
        mBinding.channelSearchEdittextLayout.visibility = View.GONE
        mBinding.channelExampleTagLayout.visibility = View.GONE
        mBinding.channelLogoIv.visibility = View.GONE
        mChannelViewModel.refresh()
    }
    private fun setSearchChannelView(){ //status 4 상태
        adapter.clear()
        mBinding.channelListText.text = "채팅방 찾기"
        mBinding.channelListText.visibility = View.VISIBLE
        mBinding.channelMyChatRoomText.visibility = View.GONE
        mBinding.channelMychattingroomCardview.visibility = View.GONE
        mBinding.channelNoBelongedLayout.visibility = View.GONE
        mBinding.channelAllBelongedLayout.visibility = View.GONE
        mBinding.channelBackToMainButton.visibility = View.VISIBLE
        mBinding.channelGrayUnderbarView.visibility = View.INVISIBLE

        mBinding.channelSearchIc.visibility = View.INVISIBLE
        mBinding.channelSearchEdittextLayout.visibility = View.VISIBLE
        mBinding.channelExampleTagLayout.visibility = View.VISIBLE
        mBinding.channelExampleTagLayout.bringToFront()
        mBinding.channelLogoIv.visibility = View.GONE
        mChannelViewModel.refresh()
    }

    private fun addTagSample(){
        val tagStringList: MutableList<String> = arrayListOf()
        val colors: MutableList<IntArray> = arrayListOf()
        val primaryColor = ContextCompat.getColor(requireContext(),R.color.primary)
        //int[] color = {TagBackgroundColor, TagBorderColor, TagTextColor, TagSelectedBackgroundColor}
        val color = intArrayOf(Color.TRANSPARENT, primaryColor, primaryColor, Color.TRANSPARENT)

        colors.add(color)
        tagStringList.add("#제로웨이스트")
        colors.add(color)
        tagStringList.add("#채식")
        colors.add(color)
        tagStringList.add("#비건 푸드")
        colors.add(color)
        tagStringList.add("#저탄고지")
        colors.add(color)
        tagStringList.add("#키토제닉")
        colors.add(color)
        tagStringList.add("#저염")
        colors.add(color)
        tagStringList.add("#무염 식단")

        mBinding.channelTagContainerLayout.setTags(tagStringList,colors)

        mBinding.channelTagContainerLayout.setOnTagClickListener(object : TagView.OnTagClickListener {
            override fun onTagClick(position: Int, text: String) {
                mBinding.channelSearchEt.setText(text)
                mChannelViewModel.setSearchKeyWord(text)
                mChannelViewModel.refresh()
                mBinding.channelExampleTagLayout.visibility = View.GONE
            }
            override fun onSelectedTagDrag(position: Int, text: String?) { }
            override fun onTagLongClick(position: Int, text: String?) { }
            override fun onTagCrossClick(position: Int) { }
        })

    }

    private fun setupViewModel(){
        mChannelViewModel.liveRoomList.observe(mBinding.lifecycleOwner!!, {
            Log.e(CHECK_TAG,"current channel count : "+it.size)
            adapter.setGroupChannelList(it)

            if (mSwipeRefresh.isRefreshing) {
                mSwipeRefresh.isRefreshing = false
            }
            if(it.size!=0){
                mBinding.channelExampleTagLayout.visibility = View.GONE
            }
        })

        mChannelViewModel.liveMyChatImgUrl.observe(mBinding.lifecycleOwner!!,{
            if(it=="none"){
                Log.e(CHECK_TAG,"myChatImg가 없습니다")
                Glide.with(this).load(R.drawable.bg_transparent).into(mBinding.channelMyroomImg)
                mBinding.channelMyroomNoRoomLayout.visibility = View.VISIBLE
            }
            else{
                Log.e(CHECK_TAG,"glide url : $it")
                Glide.with(this).load(it).override(300).circleCrop().into(mBinding.channelMyroomImg)
                mBinding.channelMyroomNoRoomLayout.visibility = View.INVISIBLE
            }
        })

        mChannelViewModel.dialogEvent.observe(mBinding.lifecycleOwner!!,{ event ->
            event.getContentIfNotHandled()?.let {
                when(it){
                    404 -> showDialog(getString(R.string.network_error),requireContext())
                }

            }
        })

        mChannelViewModel.myChannelSetEvent.observe(mBinding.lifecycleOwner!!,{ event ->
            Log.e(CHECK_TAG,"push setEvent handled1 : ${event.hasBeenHandled}")
            event.getContentIfNotHandled()?.let{
                Log.e(CHECK_TAG,"push setEvent handled2 : ${event.hasBeenHandled}")
                (activity as MainActivity).getChannelUrlWithPush()?.let{ pushUrl ->
                    Log.e(CHECK_TAG,"setChannelUrlWithPush called")
                    mChannelViewModel.setChannelUrlWithPush(pushUrl)
                }
            }
        })
    }

    private fun setUpChannelListAdapter() {
        adapter.setOnItemClickListener { otherRoom ->

            if(mChannelViewModel.getMyRoomCount()>0){
                //makeSnackBar("이미 소속된 채팅방이 있습니다.")
                mBinding.channelBlurView.visibility = RealtimeBlurView.VISIBLE
                val intent = Intent(context,EnterChannelActivity::class.java)
                intent.putExtra("roomInfo",otherRoom)
                intent.putExtra("enterFlag",false)
                startActivity(intent)
                return@setOnItemClickListener
            }

            mBinding.channelBlurView.visibility = RealtimeBlurView.VISIBLE
            val intent = Intent(context,EnterChannelActivity::class.java)
            intent.putExtra("roomInfo",otherRoom)
            intent.putExtra("enterFlag",true)
            startActivity(intent)
        }
    }

    //정보) onActivityResult는 반드시 onResume()이전에 실행된다.(경우에 따라 onStart()이전에 실행될수도 있음)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e(CHECK_TAG,"onActivityResult Code : $resultCode, request code : $requestCode")
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                100 -> { //방 만들고 나옴
                    Log.e(CHECK_TAG,"방 만들고 방 refresh")
                    val badgeTitle = data?.getStringExtra("badgeTitle")
                    if(badgeTitle!=null){
                        Log.e(CHECK_TAG,"배지 획득")
                        val badgeUrl = data.getStringExtra("badgeUrl")
                        val badgeExplain = data.getStringExtra("badgeExplain")
                        val backgroundColor = data.getStringExtra("backgroundColor")

                        val badgeDialog = ChatBadgeDialog(requireContext())
                        badgeDialog.callFunction(badgeTitle,badgeUrl!!,badgeExplain!!,backgroundColor!!)
                    }
                    mChannelViewModel.liveStatus.value = 1
                    //mChannelViewModel.refresh() 이거 하면안됨. 2번중복됨
                }
                101 -> { //채널 필터링 설정하고 나옴
                    Log.e(CHECK_TAG,"필터 설정 완료")
                    val filter: ChannelFilter = data?.getSerializableExtra("filter") as ChannelFilter
                    Log.e(CHECK_TAG,"${filter.authCount}, ${filter.isTwoWeek}, ${filter.memberCount}, ${filter.isOngoing}")
                    mChannelViewModel.liveStatus.value = 3
                    mChannelViewModel.setFilter(filter)
                }
            }
        }
        else if(resultCode == RESULT_CANCELED){
            when (requestCode){
                101 -> {
                    Log.e(CHECK_TAG,"필터 설정 해제")
                    mChannelViewModel.liveStatus.value = 2
                }
            }
        }
    }

    private fun makeTopSnackbar(str: String){
        mBinding.channelTopSnackbarPositionLayout.bringToFront()
        mBinding.channelTopSnackbarPositionLayout.snackbar(str)
    }

    override fun callChatActivity(channelUrl:String, roomIdx:Int){
        val intent = Intent(context,ChatActivity::class.java)
        intent.putExtra("channelUrl",channelUrl)
        intent.putExtra("roomIdx",roomIdx)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun makeSnackBar(str:String){
        mBinding.root.snackbar(str)
    }

    override fun showCardView(hasChatRoom: Boolean) {
        activity?.runOnUiThread {
            if(hasChatRoom){
                mBinding.channelMyroomDurationText.background = ContextCompat.getDrawable(requireContext(),R.drawable.bg_channel_myroom_duration_text)
                mBinding.channelMychattingroomCardview.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.primary))

            }
            else{
                mBinding.channelMyroomDurationText.background = ContextCompat.getDrawable(requireContext(),R.color.transparent)
                mBinding.channelMychattingroomCardview.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.channel_myroom_cardview_bg))
            }
        }
    }

    override fun noChannelSearched() {
        activity?.runOnUiThread {
            makeTopSnackbar("검색 결과가 없습니다.")
            adapter.clear()
        }
    }

    override fun onBackPressed(): Boolean {
        Log.e(CHECK_TAG,"channel fragment back pressed")
        when(mChannelViewModel.liveStatus.value){
            1-> return true
            2-> {
                mChannelViewModel.liveStatus.value = 1
                setRecentChannelView()
                mBinding.root.hideKeyboard()
                return false
            }
            3-> {
                mChannelViewModel.liveStatus.value = 2
                setAllChannelView()
                mBinding.root.hideKeyboard()
                return false
            }
            4-> {
                mChannelViewModel.liveStatus.value = 1
                setRecentChannelView()
                mBinding.root.hideKeyboard()
                return false
            }
        }
        return true
    }
}