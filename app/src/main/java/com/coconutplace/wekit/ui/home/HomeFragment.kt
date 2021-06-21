package com.coconutplace.wekit.ui.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.BodyGraph
import com.coconutplace.wekit.data.entities.Home
import com.coconutplace.wekit.data.entities.MiracleStory
import com.coconutplace.wekit.data.remote.home.listeners.HomeListener
import com.coconutplace.wekit.databinding.FragmentHomeBinding
import com.coconutplace.wekit.ui.BaseFragment
import com.coconutplace.wekit.ui.set.SetActivity
import com.coconutplace.wekit.utils.GlobalConstant.Companion.DEBUG_TAG
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_NETWORK_ERROR
import com.coconutplace.wekit.utils.hide
import com.coconutplace.wekit.utils.show
import com.google.firebase.messaging.FirebaseMessaging
import com.gun0912.tedpermission.TedPermission
import com.prolificinteractive.materialcalendarview.CalendarDay
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Math.floor
import java.util.*
import kotlin.collections.ArrayList


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
        binding.homeMiracleStory01Iv.setOnClickListener(this)
        binding.homeMiracleStory02Iv.setOnClickListener(this)
//        binding.homeMiracleStory03Iv.setOnClickListener(this)
//        binding.homeMiracleStory04Iv.setOnClickListener(this)
//        binding.homeTargetWeightMoreTv.setOnClickListener(this)
        setToday()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        sendFcmToken()
        viewModel.home()
        binding.homeSettingBtn.isClickable = true
//        binding.homeTargetWeightMoreTv.isClickable = true
    }

    override fun onResume() {
        super.onResume()
        if(!TedPermission.isGranted(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )){
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
            binding.homeMiracleStory01Iv -> openBrowserOfMiracleStory(viewModel.miracleStoryLinks[0])
            binding.homeMiracleStory02Iv -> openBrowserOfMiracleStory(viewModel.miracleStoryLinks[1])
//            binding.homeMiracleStory03Iv -> openBrowserOfMiracleStory(viewModel.miracleStoryLinks[2])
//            binding.homeMiracleStory04Iv -> openBrowserOfMiracleStory(viewModel.miracleStoryLinks[3])
//            binding.homeTargetWeightMoreTv -> startBodyGraphActivity()
        }
    }

    private fun setToday(){
        val cal = CalendarDay.today()
        val month = if (cal.month < 10) "0${cal.month}" else "${cal.month}"
        val day = if (cal.day < 10) "0${cal.day}" else "${cal.day}"

        val today = "${cal.year}.${month}.${day}"

        binding.homeTodayDateTv.text = today
    }

    private fun setCertificationBar(day: Int, totalDay: Int){
        if(totalDay > 0) {
            val counts = "$day/$totalDay"
            val ratio: Double = kotlin.math.floor((day.toDouble() / totalDay.toDouble() * 100)) / 100

            binding.homeCertificationCountsTv.text = counts

            binding.homeCertificationPb.max = totalDay
            binding.homeCertificationPb.progress = day

            (binding.homePercentView.layoutParams as ConstraintLayout.LayoutParams).horizontalWeight = if(ratio.toFloat() >= 0.88f) 0.88f else ratio.toFloat()
            binding.homePercentView.requestLayout()

            (binding.homeCertificationPercentageTv.layoutParams as ConstraintLayout.LayoutParams).horizontalWeight = if(ratio.toFloat() >= 0.88f) 0.12f else 1.0f - ratio.toFloat()
            binding.homeCertificationPercentageTv.requestLayout()

            binding.homeCertificationPercentageTv.text = (ratio * 100).toInt().toString() + "%"
        }
    }

    private fun setMiracleStory(miracleStories: ArrayList<MiracleStory>){
        Glide.with(requireActivity()).load(miracleStories[0].imgUrl).into(binding.homeMiracleStory01Iv)
        Glide.with(requireActivity()).load(miracleStories[1].imgUrl).into(binding.homeMiracleStory02Iv)
//        Glide.with(requireActivity()).load(miracleStories[2].imgUrl).into(binding.homeMiracleStory03Iv)
//        Glide.with(requireActivity()).load(miracleStories[3].imgUrl).into(binding.homeMiracleStory04Iv)

        binding.homeMiracleStory01TitleTv.text = miracleStories[0].title
        binding.homeMiracleStory02TitleTv.text = miracleStories[1].title
//        binding.homeMiracleStory03TitleTv.text = miracleStories[2].title
//        binding.homeMiracleStory04TitleTv.text = miracleStories[3].title

        viewModel.miracleStoryLinks.add(miracleStories[0].link)
        viewModel.miracleStoryLinks.add(miracleStories[1].link)
//        viewModel.miracleStoryLinks.add(miracleStories[2].link)
//        viewModel.miracleStoryLinks.add(miracleStories[3].link)
    }

    private fun openBrowserOfMiracleStory(link: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(intent)
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

//    private fun startBodyGraphActivity(){
//        binding.homeTargetWeightMoreTv.isClickable = false
//
//        val intent = Intent(context, BodyGraphActivity::class.java)
//        intent.putExtra("recent-body-graph", bodyGraph)
//        startActivity(intent)
//    }

    override fun onHomeStarted() {
        binding.homeLoading.show()
    }

    override fun onHomeSuccess(home: Home) {
        binding.homeLoading.hide()

        binding.homeGreetingNameTv.text = home.nickname
        binding.homeCertificationSubTitleTv.text = home.nickname + requireActivity().getString(R.string.home_certification_sub_title)

        home.challengeText?.let{
            binding.homeChallengeTv.text =  home.challengeText
        }

        setCertificationBar(home.day, home.totalDay)
        setMiracleStory(home.miracleStories)

        //        val targetWeight = getString(R.string.home_target_weight_title) + " " + home.targetWeight + "kg"

//        binding.homeTargetWeightTitleTv.text = targetWeight

    //        setWeightChart(
//            home.bodyGraph!!.xData!!,
//            home.bodyGraph!!.weightData!!,
//            home.targetWeight.toFloat()
//        )

//        bodyGraph = BodyGraph(
//            xData = home.bodyGraph!!.xData!!,
//            weightData = home.bodyGraph!!.weightData!!,
//            basalMetabolismData = home.bodyGraph!!.basalMetabolismData!!,
//            bmiData = home.bodyGraph!!.bmiData!!
//        )
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

//private fun setTitleText(text: String, start: Int): SpannableString {
//    val color = requireActivity().getColor(R.color.routine_done_active)
//    val span = SpannableString(text)
//    span.setSpan(StyleSpan(Typeface.BOLD), start, start + 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//    span.setSpan(ForegroundColorSpan(color),start,start + 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//    return span
//}

//    private fun setWeightChart(
//        xData: ArrayList<String>,
//        yData: ArrayList<Entry>,
//        targetWeight: Float
//    ){
//        binding.homeWeightChart.apply {
//            axisRight.isEnabled = false
//            description.isEnabled = false
//            legend.isEnabled = false
//        }
//
//        val xAxis = binding.homeWeightChart.xAxis
//
//        xAxis.apply {
//            setDrawGridLines(false)
//            setDrawAxisLine(false)
//            position = XAxis.XAxisPosition.BOTTOM
//            labelCount = 7
//            isGranularityEnabled = true
//            textSize = 12f
//            textColor = context?.let{ getColor(it, R.color.home_chart_x) }!!
//            valueFormatter = XValueFormatter(xData)
//        }
//
//        val yAxis = binding.homeWeightChart.axisLeft
//
//        yAxis.apply {
//            setDrawAxisLine(false)
//            setDrawLabels(false)
//            isGranularityEnabled = true
//            granularity = 1.8f
//            gridLineWidth = 0.5f
//            gridColor = context?.let{ getColor(it, R.color.body_graph_grid) }!!
//            spaceMin = 10f
//        }
//
//        val setWeight = LineDataSet(yData, "")
//
//        setWeight.apply {
//            mode = LineDataSet.Mode.CUBIC_BEZIER
//            setDrawCircleHole(false)
//
//            color = context?.let{ getColor(it, R.color.body_graph_line) }!!
//            lineWidth = 1.2f
//            valueTextColor = context?.let{ getColor(it, R.color.body_graph_value) }!!
//
//            setCircleColor(context?.let { getColor(it, R.color.body_graph_line) }!!)
//            circleRadius = 5f
//            valueTextSize = 12f
//            isHighlightEnabled = false
//        }
//
//        val targetWeightEntry = ArrayList<Entry>()
//        targetWeightEntry.add(Entry(xData.size.toFloat(), targetWeight))
//
//        val setTargetWeight = LineDataSet(targetWeightEntry, "target")
//
//        setTargetWeight.apply {
//            mode = LineDataSet.Mode.CUBIC_BEZIER
//            setDrawCircleHole(false)
//
//            color = context?.let{ getColor(it, R.color.body_graph_line) }!!
//            lineWidth = 1.2f
//            valueTextColor = context?.let{ getColor(it, R.color.body_graph_value) }!!
//
//            setCircleColor(context?.let { getColor(it, R.color.body_graph_line) }!!)
//            setDrawCircleHole(true)
//            circleHoleColor = context?.let{ getColor(it, R.color.white) }!!
//            circleRadius = 5f
//            circleHoleRadius = 3.5f
//            valueTextSize = 12f
//            isHighlightEnabled = false
//        }
//
//        val lineData = LineData(setWeight)
//        lineData.addDataSet(setTargetWeight)
//        binding.homeWeightChart.data = lineData
//        binding.homeWeightChart.invalidate()
//        binding.homeWeightChart.visibility = View.VISIBLE
//    }