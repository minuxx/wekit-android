package com.coconutplace.wekit.ui.diary.decorators

import android.content.Context
import android.text.style.ForegroundColorSpan
import com.coconutplace.wekit.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import kotlin.collections.HashSet

class WrittenDatesDecorator(context: Context?, private val writtenDates: Collection<CalendarDay>): DayViewDecorator {
    private val color = context!!.getColor(R.color.diary_written_day)
    private val today = CalendarDay.today()
    private val writtenDateHash: HashSet<CalendarDay> = HashSet(writtenDates)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return writtenDateHash.contains(day)
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object: ForegroundColorSpan(color){})
    }
}