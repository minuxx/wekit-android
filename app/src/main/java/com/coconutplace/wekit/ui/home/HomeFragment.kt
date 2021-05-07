package com.coconutplace.wekit.ui.home

import android.Manifest
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.BodyGraph
import com.coconutplace.wekit.data.entities.Home
import com.coconutplace.wekit.data.remote.home.listeners.HomeListener
import com.coconutplace.wekit.databinding.FragmentHomeBinding
import com.coconutplace.wekit.ui.BaseFragment
import com.coconutplace.wekit.ui.body_graph.BodyGraphActivity
import com.coconutplace.wekit.ui.set.SetActivity
import com.coconutplace.wekit.utils.GlobalConstant.Companion.DEBUG_TAG
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_NETWORK_ERROR
import com.coconutplace.wekit.utils.hide
import com.coconutplace.wekit.utils.show
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.messaging.FirebaseMessaging
import com.gun0912.tedpermission.TedPermission
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeFragment : BaseFragment(), HomeListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModel()
    private var mFlag = 0;
    private lateinit var bodyGraph: BodyGraph

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.homeListener = this

        binding.homeSettingBtn.setOnClickListener(this)
        binding.homeTargetWeightMoreTv.setOnClickListener(this)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        sendFcmToken()
        viewModel.home()
        binding.homeSettingBtn.isClickable = true
        binding.homeTargetWeightMoreTv.isClickable = true
    }

    override fun onResume() {
        super.onResume()
        if(!TedPermission.isGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)){
            val permissionDialog = PermissionDialog()
            permissionDialog.show(parentFragmentManager, permissionDialog.tag)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when(v){
            binding.homeSettingBtn -> startSetActivity()
            binding.homeTargetWeightMoreTv -> startBodyGraphActivity()
        }
    }

    private fun setWeightChart(
        xData: ArrayList<String>,
        yData: ArrayList<Entry>,
        targetWeight: Float
    ){
        binding.homeWeightChart.apply {
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
        }

        val xAxis = binding.homeWeightChart.xAxis

        xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            position = XAxis.XAxisPosition.BOTTOM
            labelCount = 7
            isGranularityEnabled = true
            textSize = 12f
            textColor = context?.let{ getColor(it, R.color.home_chart_x) }!!
            valueFormatter = XValueFormatter(xData)
        }

        val yAxis = binding.homeWeightChart.axisLeft

        yAxis.apply {
            setDrawAxisLine(false)
            setDrawLabels(false)
            isGranularityEnabled = true
            granularity = 1.8f
            gridLineWidth = 0.5f
            gridColor = context?.let{ getColor(it, R.color.body_graph_grid) }!!
            spaceMin = 10f
        }

        val setWeight = LineDataSet(yData, "")

        setWeight.apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircleHole(false)

            color = context?.let{ getColor(it, R.color.body_graph_line) }!!
            lineWidth = 1.2f
            valueTextColor = context?.let{ getColor(it, R.color.body_graph_value) }!!

            setCircleColor(context?.let { getColor(it, R.color.body_graph_line) }!!)
            circleRadius = 5f
            valueTextSize = 12f
            isHighlightEnabled = false
        }

        val targetWeightEntry = ArrayList<Entry>()
        targetWeightEntry.add(Entry(xData.size.toFloat(), targetWeight))

        val setTargetWeight = LineDataSet(targetWeightEntry, "target")

        setTargetWeight.apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircleHole(false)

            color = context?.let{ getColor(it, R.color.body_graph_line) }!!
            lineWidth = 1.2f
            valueTextColor = context?.let{ getColor(it, R.color.body_graph_value) }!!

            setCircleColor(context?.let { getColor(it, R.color.body_graph_line) }!!)
            setDrawCircleHole(true)
            circleHoleColor = context?.let{ getColor(it, R.color.white) }!!
            circleRadius = 5f
            circleHoleRadius = 3.5f
            valueTextSize = 12f
            isHighlightEnabled = false
        }

        val lineData = LineData(setWeight)
        lineData.addDataSet(setTargetWeight)
        binding.homeWeightChart.data = lineData
        binding.homeWeightChart.invalidate()
        binding.homeWeightChart.visibility = View.VISIBLE
    }

    private fun setCertificationBar(day: Int, totalDay: Int){
        if(totalDay > 0) {
            val counts = "$day/$totalDay"
            binding.homeCertificationCountsTv.text = counts

//            binding.homeCertificationPb.progress = (day / totalDay) * 100
            binding.homeCertificationPb.max = totalDay
            binding.homeCertificationPb.progress = day

        }
    }

    private fun sendFcmToken(){
        val fcmTask = FirebaseMessaging.getInstance().token
            .addOnSuccessListener {
                viewModel.sendFcmToken(it)
            }
            .addOnFailureListener {
                viewModel.sendFcmToken(null)
            }
    }

    private fun startSetActivity(){
        binding.homeSettingBtn.isClickable = false

        val intent = Intent(context, SetActivity::class.java)
        startActivity(intent)
    }

    private fun startBodyGraphActivity(){
        binding.homeTargetWeightMoreTv.isClickable = false

        val intent = Intent(context, BodyGraphActivity::class.java)
        intent.putExtra("recent-body-graph", bodyGraph)
        startActivity(intent)
    }

    override fun onHomeStarted() {
        binding.homeLoading.show()
    }

    override fun onHomeSuccess(home: Home) {
        binding.homeLoading.hide()

        val greeting: String = home.nickname + "님," + getText(R.string.home_greeting)

        val span = SpannableString(greeting)
        span.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            home.nickname.length + 2,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.homeGreetingTv.text = span

        home.challengeText?.let{
            binding.homeChallengeTv.text =  home.challengeText
        }

        val targetWeight = getString(R.string.home_target_weight_title) + " " + home.targetWeight + "kg"

        binding.homeTargetWeightTitleTv.text = targetWeight

        setWeightChart(
            home.bodyGraph!!.xData!!,
            home.bodyGraph!!.weightData!!,
            home.targetWeight.toFloat()
        )

        setCertificationBar(home.day, home.totalDay)

        bodyGraph = BodyGraph(
            xData = home.bodyGraph!!.xData!!,
            weightData = home.bodyGraph!!.weightData!!,
            basalMetabolismData = home.bodyGraph!!.basalMetabolismData!!,
            bmiData = home.bodyGraph!!.bmiData!!
        )
    }

    override fun onHomeFailure(code: Int, message: String) {
        binding.homeLoading.hide()

        when(code){
            301, 302, 500 -> Log.d(DEBUG_TAG, message)
            404 -> {
                mFlag = FLAG_NETWORK_ERROR
                showDialog(getString(R.string.network_error), requireActivity())
            }
        }
    }

    override fun onSendFcmTokenStarted() {
        binding.homeLoading.show()
    }

    override fun onSendFcmTokenSuccess() {
        binding.homeLoading.hide()
    }

    override fun onSendFcmTokenFailure(code: Int, message: String) {
        binding.homeLoading.hide()
    }
}