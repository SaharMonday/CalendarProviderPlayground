package com.monday.calendarproviderplayground.base

import androidx.lifecycle.Lifecycle
import com.monday.calendarproviderplayground.CalendarData
import com.monday.calendarproviderplayground.EventData

interface IViewModel {
    fun setGeneratedEventId(eventId: Long)
    fun getGeneratedEventId(): Long?
    fun observeGeneratedEventId(lifecycle: Lifecycle, observer: (Long) -> Unit)

    fun setExtractedCalendarsData(calendarDataList: List<CalendarData>)
    fun observeExtractedCalendarsData(lifecycle: Lifecycle, observer: (List<CalendarData>) -> Unit)

    fun setSelectedCalendarData(data: CalendarData)
    fun observeSelectedCalendarData(lifecycle: Lifecycle, observer: (CalendarData) -> Unit)
    fun getSelectedCalendarData(): CalendarData?

    fun setEventData(eventData: EventData)
    fun observeEventData(lifecycle: Lifecycle, observer: (EventData) -> Unit)

    fun setCustomDataAddedSuccessfully(rowId: Long)
    fun observeCustomDataAddedSuccessfully(lifecycle: Lifecycle, observer: (Long) -> Unit)
}