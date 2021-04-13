package com.coconutplace.wekit.ui.body_graph

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.BodyGraph
import com.coconutplace.wekit.data.remote.body.listeners.BodyGraphListener
import com.coconutplace.wekit.databinding.ActivityBodyGraphBinding
import com.coconutplace.wekit.ui.BaseActivity
import com.coconutplace.wekit.ui.input_body.InputBodyActivity
import com.coconutplace.wekit.utils.hide
import com.coconutplace.wekit.utils.show
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class BodyGraphActivity : BaseActivity(), BodyGraphListener {
    private lateinit var binding: ActivityBodyGraphBinding
    private val viewModel: BodyGraphViewModel by viewModel()
    private lateinit var recentBodyGraph: BodyGraph

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_body_graph)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.bodyGraphListener = this

        if(intent.hasExtra("recent-body-graph")){
           recentBodyGraph = intent.getParcelableExtra("recent-body-graph")!!
        }else{
            finish()
        }

        binding.bodyGraphPager.isUserInputEnabled = false
        binding.bodyGraphBackBtn.setOnClickListener(this)
        binding.bodyGraphInputBodyBtn.setOnClickListener(this)

        viewModel.getBodyInfo()
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.getBodyInfo()
        binding.bodyGraphInputBodyBtn.isClickable = true
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when(v){
            binding.bodyGraphBackBtn -> finish()
            binding.bodyGraphInputBodyBtn -> startInputBody()
        }
    }

    private fun initViewPager(recent: BodyGraph, total: BodyGraph){
        val adapter = BodyGraphAdapter(this, recent, total)

        binding.bodyGraphPager.adapter = adapter
        val tabLayoutTextArray = arrayOf(getString(R.string.body_graph_recent), getString(R.string.body_graph_total))

        TabLayoutMediator(binding.bodyGraphTab, binding.bodyGraphPager){ tab, position->
            tab.text = tabLayoutTextArray[position]
        }.attach()
    }

    private fun startInputBody(){
        binding.bodyGraphInputBodyBtn.isClickable = false
        val intent = Intent(this@BodyGraphActivity, InputBodyActivity::class.java)

        startActivity(intent)
        finish()
    }

    override fun onBodyGraphStarted() {
        binding.bodyGraphLoading.show()
    }

    override fun onBodyGraphSuccess(total: BodyGraph) {
        binding.bodyGraphLoading.hide()

        initViewPager(recentBodyGraph, total)
    }

    override fun onBodyGraphFailure(code: Int, message: String) {
        binding.bodyGraphLoading.hide()
    }
}