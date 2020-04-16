package com.monday.calendarproviderplayground.base

import com.monday.calendarproviderplayground.CalendarData

interface IPresenter: IViewModel {
    suspend fun createCalendarEvent()
    suspend fun extractCalendarsData()
    fun onUserSelectedCalendar(calendarData: CalendarData)
}