package com.coconutplace.wekit.ui.create_channel


import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import co.lujun.androidtagview.TagView.OnTagClickListener
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.remote.channel.listeners.CreateChannelListener
import com.coconutplace.wekit.databinding.ActivityCreateChannelBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.CHECK_TAG
import com.coconutplace.wekit.utils.hideKeyboard
import org.koin.androidx.viewmodel.ext.android.viewModel


class CreateChannelActivity: BaseActivity(), CreateChannelListener {

    private lateinit var mBinding: ActivityCreateChannelBinding
    private val mCreateChannelViewModel: CreateChannelViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mCreateChannelViewModel.createFlag = intent.getBooleanExtra("createFlag",false)

        setupView()
        setupViewModel()
    }

    private fun setupView(){
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_create_channel)
        mBinding.lifecycleOwner = this
        mBinding.mCreateChatViewModel = mCreateChannelViewModel

        // X 버튼
        mBinding.createChannelBackBtn.setOnClickListener{
            finish()
        }
        mBinding.createChannelBackBtnFrameLayout.setOnClickListener{
            finish()
        }
        //취소 버튼
        mBinding.createChannelCancelBtn.setOnClickListener{
            finish()
        }

        mBinding.createChannelTwoWeekBtn.setOnClickListener{
            mBinding.createChannelTwoWeekBtn.background = ContextCompat.getDrawable(this,R.drawable.bg_create_channel_white_button)
            mBinding.createChannelTwoWeekBtn.setTextColor(ContextCompat.getColor(this,R.color.dark_gray))
            mBinding.createChannelFourWeekBtn.background = ContextCompat.getDrawable(this,R.drawable.bg_create_channel_transparent_button)
            mBinding.createChannelFourWeekBtn.setTextColor(ContextCompat.getColor(this,R.color.white))
            mCreateChannelViewModel.durationLong = false
        }
        mBinding.createChannelFourWeekBtn.setOnClickListener{
//            mBinding.createChannelTwoWeekBtn.background = ContextCompat.getDrawable(this,R.drawable.bg_create_channel_transparent_button)
//            mBinding.createChannelTwoWeekBtn.setTextColor(ContextCompat.getColor(this,R.color.white))
//            mBinding.createChannelFourWeekBtn.background = ContextCompat.getDrawable(this,R.drawable.bg_create_channel_white_button)
//            mBinding.createChannelFourWeekBtn.setTextColor(ContextCompat.getColor(this,R.color.dark_gray))
//            mCreateChannelViewModel.durationLong = true
            makePopup("추후 업데이트 예정입니다.")
        }

        //방 만들기 완료 버튼
        mBinding.createChannelCompleteBtn.setOnClickListener{

            val count = mBinding.createChannelAuthCountSpinner.selectedItem.toString().substring(0,1).toInt()
            val limit = mBinding.createChannelMaxMemberSpinner.selectedItem.toString().substring(0,1).toInt()

            val nameSize = mBinding.createChannelNameEt.text.toString().length
            if(nameSize<2||nameSize>20){
                makeSnackBar("방 이름의 글자 수는 2~20 글자 사이여야 합니다")
                return@setOnClickListener
            }
            val explainSize = mBinding.createChannelExplainEt.text.toString().length
            if(explainSize>50){
                makeSnackBar("방 설명의 글자수는 50글자 이내여야 합니다")
                return@setOnClickListener
            }
            if(!mCreateChannelViewModel.createFlag){
                makeSnackBar("이미 소속된 채팅방이 있습니다")
                return@setOnClickListener
            }
            mBinding.createChannelCompleteBtn.isClickable = false

            mCreateChannelViewModel.createGroupChannel(count,limit)
        }

        //태그 추가하기
        mBinding.createChannelAddTagBtn.setOnClickListener{

            mBinding.root.hideKeyboard()

            if(mCreateChannelViewModel.tagStringList.size>=5){
                makeSnackBar("태그는 5개가 최대 개수입니다.")
                mBinding.createChannelTagEt.setText("")
                return@setOnClickListener
            }
            if(mBinding.createChannelTagEt.text.toString()==""){
                makeSnackBar("태그를 입력해주세요")
                return@setOnClickListener
            }

            val tempTag = "#"+mBinding.createChannelTagEt.text.toString()
            if(tempTag.contains("|")){
                Toast.makeText(this,"태그에 '|'는 입력할 수 없습니다.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mCreateChannelViewModel.tagStringList.add(tempTag)

            val colors: MutableList<IntArray> = arrayListOf()
            //int[] color = {TagBackgroundColor, TagBorderColor, TagTextColor, TagSelectedBackgroundColor}
            val color = intArrayOf(Color.TRANSPARENT, Color.WHITE, Color.WHITE, Color.TRANSPARENT)
            for(item in mCreateChannelViewModel.tagStringList){
                colors.add(color)
            }

            mBinding.createChannelTagContainerLayout.setTags(mCreateChannelViewModel.tagStringList,colors)
            mBinding.createChannelTagEt.setText("")
        }

        //태그 누르면 삭제
        mBinding.createChannelTagContainerLayout.setOnTagClickListener(object : OnTagClickListener {
            override fun onTagClick(position: Int, text: String) {
                mCreateChannelViewModel.tagStringList.remove(text)
                val colors: MutableList<IntArray> = arrayListOf()
                //int[] color = {TagBackgroundColor, TagBorderColor, TagTextColor, TagSelectedBackgroundColor}
                val color = intArrayOf(Color.TRANSPARENT, Color.WHITE, Color.WHITE, Color.TRANSPARENT)
                val size = mCreateChannelViewModel.tagStringList.size

                for(i in 0 until size){
                    colors.add(color)
                }

                mBinding.createChannelTagContainerLayout.setTags(mCreateChannelViewModel.tagStringList,colors)
            }

            override fun onTagLongClick(position: Int, text: String) { }

            override fun onSelectedTagDrag(position: Int, text: String) { }

            override fun onTagCrossClick(position: Int) { }
        })

        //스피너
        val authSpinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.create_channel_auth_count_array,
            R.layout.spinner_create_channel_selected
        )
        authSpinnerAdapter.setDropDownViewResource(R.layout.spinner_create_channel_dropdown)
        mBinding.createChannelAuthCountSpinner.adapter = authSpinnerAdapter

        val countSpinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.create_channel_member_count_array,
            R.layout.spinner_create_channel_selected
        )
        countSpinnerAdapter.setDropDownViewResource(R.layout.spinner_create_channel_dropdown)
        mBinding.createChannelMaxMemberSpinner.adapter = countSpinnerAdapter

        mBinding.root.setOnClickListener{
            mBinding.root.hideKeyboard()
        }
    }

    private fun setupViewModel(){
        mCreateChannelViewModel.createChannelListener = this
    }

    private fun makePopup(str: String) {
        runOnUiThread{
            Log.e(CHECK_TAG,"make popup")
            showDialog(str)
        }
    }

    override fun onCreateChannelSuccess() {
        mBinding.createChannelCompleteBtn.isClickable = true
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onCreateChannelSuccess(
        badgeTitle: String,
        badgeUrl: String,
        badgeExplain: String,
        backgroundColor:String
    ) {
        mBinding.createChannelCompleteBtn.isClickable = true
        val intent = Intent()
        intent.putExtra("badgeTitle",badgeTitle)
        intent.putExtra("badgeUrl",badgeUrl)
        intent.putExtra("badgeExplain",badgeExplain)
        intent.putExtra("backgroundColor",backgroundColor)

        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onCreateChannelFailure() {
        mBinding.createChannelCompleteBtn.isClickable = true
    }

    override fun makeSnackBar(str: String) {
        //mBinding.root.snackbar(str)
        showDialog(str)
    }
}