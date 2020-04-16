package com.monday.calendarproviderplayground

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.monday.calendarproviderplayground.base.IViewModel

class MainViewModel: ViewModel(), IViewModel {

    private val generatedEventId = MutableLiveData<Long>()
    private val extractedCalendarLiveData = MutableLiveData<List<CalendarData>>()
    private val selectedCalendarData = MutableLiveData<CalendarData>()
    private val eventDataLiveData = MutableLiveData<EventData>()
    private val customDataSetSuccessfullyLiveData = MutableLiveData<Long>()

    override fun setGeneratedEventId(eventId: Long) {
        generatedEventId.postValue(eventId)
    }

    override fun getGeneratedEventId(): Long? = generatedEventId.value

    override fun observeGeneratedEventId(lifecycle: Lifecycle, observer: (Long) -> Unit) {
        generatedEventId.observe({lifecycle}){
            it?.let(observer)
        }
    }

    override fun setExtractedCalendarsData(calendarDataList: List<CalendarData>) {
        extractedCalendarLiveData.postValue(calendarDataList)
    }

    override fun observeExtractedCalendarsData(
        lifecycle: Lifecycle,
        observer: (List<CalendarData>) -> Unit
    ) {
        extractedCalendarLiveData.observe({lifecycle}){
            it?.let(observer)
        }
    }

    override fun setSelectedCalendarData(data: CalendarData) {
        selectedCalendarData.postValue(data)
    }

    override fun observeSelectedCalendarData(
        lifecycle: Lifecycle,
        observer: (CalendarData) -> Unit
    ) {
        selectedCalendarData.observe({lifecycle}){
            it?.let(observer)
        }
    }

    override fun getSelectedCalendarData(): CalendarData? = selectedCalendarData.value

    override fun setEventData(eventData: EventData) {
        eventDataLiveData.postValue(eventData)
    }

    override fun observeEventData(lifecycle: Lifecycle, observer: (EventData) -> Unit) {
        eventDataLiveData.observe({lifecycle}){
            it?.let(observer)
        }
    }

    override fun setCustomDataAddedSuccessfully(rowId: Long) {
        customDataSetSuccessfullyLiveData.postValue(rowId)
    }

    override fun observeCustomDataAddedSuccessfully(lifecycle: Lifecycle, observer: (Long) -> Unit) {
        customDataSetSuccessfullyLiveData.observe({lifecycle}){
            it?.let(observer)
        }
    }
}