package com.coconutplace.wekit.ui.diary.decorators

import android.content.Context
import android.text.style.ForegroundColorSpan
import com.coconutplace.wekit.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

class ToadyDecorator(context: Context?): DayViewDecorator {
    private val calendar = Calendar.getInstance()
    private val color = context!!.getColor(R.color.diary_today)
    private val today = CalendarDay.today()

    override fun shouldDecorate(day: CalendarDay): Boolean {
        calendar.set(day.year, day.month - 1, day.day)
        return day == today
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object: ForegroundColorSpan(color){})
    }
}