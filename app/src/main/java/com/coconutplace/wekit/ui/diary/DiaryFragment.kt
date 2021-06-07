package com.coconutplace.wekit.ui.diary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.Diary
import com.coconutplace.wekit.data.remote.diary.listeners.DiaryListener
import com.coconutplace.wekit.databinding.FragmentDiaryBinding
import com.coconutplace.wekit.ui.BaseFragment
import com.coconutplace.wekit.ui.diary.decorators.NormalDayDecorator
import com.coconutplace.wekit.ui.diary.decorators.SelectedDayDecorator
import com.coconutplace.wekit.ui.diary.decorators.WrittenDatesDecorator
import com.coconutplace.wekit.ui.diary.decorators.WrittenDayDecorator
import com.coconutplace.wekit.ui.write_diary.WriteDiaryActivity
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_READ_DIARY
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_WRITE_DIARY
import com.coconutplace.wekit.utils.hide
import com.coconutplace.wekit.utils.show
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class DiaryFragment : BaseFragment(), DiaryListener, OnDateSelectedListener,
    OnMonthChangedListener {
    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiaryViewModel by viewModel()
    private lateinit var mDiaryAdapter: DiaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.diaryListener = this

        initRecyclerView()
        setCalendar()

        viewModel.getWrittenDates(getCurrentDate(CalendarDay.today()).substring(0, 7))

        binding.diaryWriteBtn.setOnClickListener(this)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.getDiaries(getCurrentDate(binding.diaryCalendarView.selectedDate!!))
        viewModel.getWrittenDates(getCurrentDate(binding.diaryCalendarView.selectedDate!!).substring(0, 7))
        binding.diaryWriteBtn.isClickable = true

        binding.diaryCalendarView.addDecorators(SelectedDayDecorator(context, binding.diaryCalendarView.selectedDate!!))
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when(v){
            binding.diaryWriteBtn -> startWriteDiaryActivity()
        }
    }

    private fun startWriteDiaryActivity(){
        if(isFutureDay()){
            showDialog(getString(R.string.diary_warning_future_day), requireActivity())
            return
        }

        binding.diaryWriteBtn.isClickable = false

        val intent = Intent(context, WriteDiaryActivity::class.java)
        val date =  getCurrentDate(binding.diaryCalendarView.selectedDate!!)

        intent.putExtra("date", date)
        intent.putExtra("flag", FLAG_WRITE_DIARY)

        startActivity(intent)
    }

    private fun initRecyclerView(){
        mDiaryAdapter = DiaryAdapter{
            startReadDiaryActivity(it.diaryIdx!!)
        }

        binding.diaryRecyclerview.adapter = mDiaryAdapter
    }

    private fun startReadDiaryActivity(diaryIdx: Int){
        val intent = Intent(context, WriteDiaryActivity::class.java)
        intent.putExtra("flag", FLAG_READ_DIARY)
        intent.putExtra("diaryIdx", diaryIdx)

        startActivity(intent)
    }

    private fun setCalendar() {
        binding.diaryCalendarView.setOnMonthChangedListener(this)
        binding.diaryCalendarView.setOnDateChangedListener(this)
        binding.diaryCalendarView.topbarVisible = false
        binding.diaryCalendarView.isDynamicHeightEnabled = true

        binding.diaryCalendarView.selectedDate = CalendarDay.today()

        val today = CalendarDay.today()

        binding.diaryYearTv.text = today.year.toString()
        binding.diaryMonthTv.text = if ( today.month < 10 ){ "0" + today.month } else { today.month.toString() }

        binding.diaryCalendarView.addDecorators(SelectedDayDecorator(context, today))
    }

    private fun getCurrentDate(date: CalendarDay): String{
        return date.year.toString() + if ( date.month < 10 ){ "-0" + date.month } else { "-" + date.month } +
                if ( date.day < 10 ){ "-0" + date.day } else { "-" + date.day }
    }

    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        if(viewModel.writtenDates.indexOf(viewModel.previousDay) != -1){
            binding.diaryCalendarView.addDecorator(WrittenDayDecorator(context, viewModel.previousDay))
        }else{
            binding.diaryCalendarView.addDecorator(NormalDayDecorator(context, viewModel.previousDay))
        }

        widget.selectedDate = date
        widget.addDecorator(SelectedDayDecorator(context, date))

        if(!isFutureDay()) {
            viewModel.getDiaries(getCurrentDate(date))
        }

        viewModel.previousDay = date
    }

    override fun onMonthChanged(widget: MaterialCalendarView, date: CalendarDay) {
        binding.diaryYearTv.text = date.year.toString()
        binding.diaryMonthTv.text = if ( date.month < 10 ){ "0" + date.month } else { date.month.toString() }

        viewModel.getWrittenDates(getCurrentDate(date).substring(0, 7))
    }


    private fun isFutureDay(): Boolean{
        val today = CalendarDay.today()
        val selectedDate = binding.diaryCalendarView.selectedDate!!

        val beginDay = Calendar.getInstance().apply {
            set(Calendar.YEAR, today.year)
            set(Calendar.MONTH, today.month)
            set(Calendar.DAY_OF_MONTH, today.day)
        }.timeInMillis

        val lastDay = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedDate.year)
            set(Calendar.MONTH, selectedDate.month)
            set(Calendar.DAY_OF_MONTH, selectedDate.day)
        }.timeInMillis

        return getIgnoredTimeDays(lastDay) - getIgnoredTimeDays(beginDay) > 0
    }

    private fun getIgnoredTimeDays(time: Long): Long{
        return Calendar.getInstance().apply {
            timeInMillis = time

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    override fun onGetDiaryStarted() {
        binding.diaryLoading.show()
    }

    override fun onGetDiarySuccess(diaries: ArrayList<Diary>) {
        binding.diaryLoading.hide()

        mDiaryAdapter.addItems(diaries)
    }

    override fun onGetDiaryFailure(code: Int, message: String) {
        binding.diaryLoading.hide()

        when(code){
            404 -> {
                showDialog(getString(R.string.network_error), requireActivity())
            }
        }
    }

    override fun onGetWrittenDatesStarted() {
        binding.diaryLoading.show()
    }

    override fun onGetWrittenDatesSuccess() {
        binding.diaryLoading.hide()

        binding.diaryCalendarView.addDecorators(WrittenDatesDecorator(context, viewModel.writtenDates))
//        if(binding.diaryCalendarView.selectedDate == CalendarDay.today()){
//            binding.diaryCalendarView.addDecorators(SelectedDayDecorator(context, CalendarDay.today()))
//        }

        binding.diaryCalendarView.selectedDate?.let{
            binding.diaryCalendarView.addDecorators(SelectedDayDecorator(context, it))
        }
    }

    override fun onGetWrittenDatesFailure(code: Int, message: String) {
        binding.diaryLoading.hide()

        when(code){
            404 -> {
                showDialog(getString(R.string.network_error), requireActivity())
            }
        }
    }
}