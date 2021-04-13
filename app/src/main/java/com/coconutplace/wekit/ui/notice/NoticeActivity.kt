package com.coconutplace.wekit.ui.notice

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Notice
import com.coconutplace.wekit.data.remote.notice.NoticeListener
import com.coconutplace.wekit.databinding.ActivityNoticeBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.utils.hide
import com.coconutplace.wekit.utils.show
import com.coconutplace.wekit.utils.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class NoticeActivity : BaseActivity(), NoticeListener{
    private lateinit var binding: ActivityNoticeBinding
    private val viewModel: NoticeViewModel by viewModel()
    private lateinit var mNoticeAdapter: NoticeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notice)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel
        viewModel.noticeListener = this

        binding.noticeBackBtn.setOnClickListener(this)

        initRecyclerView()
        viewModel.getNotices(1)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when(v){
            binding.noticeBackBtn -> finish()
        }
    }

    private fun initRecyclerView(){
        mNoticeAdapter = NoticeAdapter()

        binding.noticeRecyclerview.adapter = mNoticeAdapter
    }

    override fun onGetNoticeStarted() {
        binding.noticeLoading.show()
    }

    override fun onGetNoticeSuccess(notices: ArrayList<Notice>) {
        binding.noticeLoading.hide()

        mNoticeAdapter.addItems(notices)
    }

    override fun onGetNoticeFailure(code: Int, message: String) {
        binding.noticeLoading.hide()

        when(code){
            303, 304 -> binding.noticeRootLayout.snackbar(message)
            404 -> binding.noticeRootLayout.snackbar(getString(R.string.network_error))
            else -> binding.noticeRootLayout.snackbar(message)
        }
    }
}