package com.monday.calendarproviderplayground.base

import com.monday.calendarproviderplayground.CalendarData

interface IPresenter: IViewModel {
    suspend fun createCalendarEvent()
    suspend fun extractCalendarsData()
    suspend fun queryGeneratedEventData()
    fun onUserSelectedCalendar(calendarData: CalendarData)
    suspend fun addCustomDataToEvent(keyTxt: String, keyValue: String)
}