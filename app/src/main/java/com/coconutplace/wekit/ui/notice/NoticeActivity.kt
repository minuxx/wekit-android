package com.coconutplace.wekit.ui.notice

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.Engine
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Notice
import com.coconutplace.wekit.data.remote.notice.NoticeListener
import com.coconutplace.wekit.databinding.ActivityNoticeBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.utils.hide
import com.coconutplace.wekit.utils.show
import com.coconutplace.wekit.utils.snackbar
import kotlinx.android.synthetic.main.activity_notice.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class NoticeActivity : BaseActivity(), NoticeListener{
    private val viewModel: NoticeViewModel by viewModel()
    private lateinit var mNoticeAdapter: NoticeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        notice_back_btn.setOnClickListener(this)

        initRecyclerView()
//        viewModel.getNotices(1)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when(v){
            notice_back_btn -> finish()
        }
    }

    private fun initRecyclerView(){
        val adapter = NoticeAdapter(this)

        notice_recyclerview.adapter = adapter

        lifecycleScope.launch {
            viewModel.noticeFlow.collectLatest{ pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    override fun onGetNoticeStarted() {
        notice_loading.show()
    }

    override fun onGetNoticeSuccess(notices: ArrayList<Notice>) {
        notice_loading.hide()

//        mNoticeAdapter.addItems(notices)
    }

    override fun onGetNoticeFailure(code: Int, message: String) {
        notice_loading.hide()

        when(code){
            303, 304 -> notice_root_layout.snackbar(message)
            404 -> notice_root_layout.snackbar(getString(R.string.network_error))
            else -> notice_root_layout.snackbar(message)
        }
    }
}