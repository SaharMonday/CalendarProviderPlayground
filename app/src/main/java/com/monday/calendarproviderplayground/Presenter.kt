package com.monday.calendarproviderplayground

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.provider.CalendarContract
import android.util.Log
import com.monday.calendarproviderplayground.base.IPresenter
import com.monday.calendarproviderplayground.base.IViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class Presenter(private val viewModel: IViewModel,
                private val contentResolver: ContentResolver) : IPresenter, IViewModel by viewModel {

    private var calendarDataToUse: CalendarData? = null
    private val TAG = "Presenter"

    private val eventTitle = "My Custom event title"
    private val eventDescription = "My Custom event description"

    companion object {
        private val EVENT_PROJECTION: Array<String> = arrayOf(
            CalendarContract.Calendars._ID,                     // 0
            CalendarContract.Calendars.ACCOUNT_NAME,            // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
            CalendarContract.Calendars.OWNER_ACCOUNT            // 3
        )

        // The indices for the projection array above.
        private const val PROJECTION_ID_INDEX: Int = 0
        private const val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
        private const val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
        private const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3

        private val CALENDAR_TABLE_URI = CalendarContract.Calendars.CONTENT_URI
        private val EVENTS_TABLE_URI = CalendarContract.Events.CONTENT_URI
    }

    override suspend fun createCalendarEvent() {
        viewModel.getSelectedCalendarData()?.let { calendarData ->
            Log.d(TAG, "createCalendarEvent(): creating event using calendar data: $calendarData")
            createEventInCalendar(calendarData)
        } ?: Log.e(TAG, "createCalendarEvent: no selected calendar data found!")
    }

    @SuppressLint("MissingPermission")
    private suspend fun createEventInCalendar(calendarData: CalendarData) =
        withContext(Dispatchers.IO) {
            val calId = calendarData.id
            val startMillis = Calendar.getInstance().time.time
            val endMillis = startMillis + (1000 * 60 * 60) // 1 hour later
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, eventTitle)
                put(CalendarContract.Events.DESCRIPTION, eventDescription)
                put(CalendarContract.Events.CALENDAR_ID, calId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().displayName)
            }
            contentResolver.insert(EVENTS_TABLE_URI, values)?.let { uri ->
                val eventId = uri.lastPathSegment?.toLong() ?: -1L
                Log.d(TAG, "createEventInCalendar: created event with id = $eventId")
                setGeneratedEventId(eventId)
            } ?: Log.e(TAG, "createEventInCalendar: error creating event!")
        }

    override suspend fun extractCalendarsData() {
        val calendarDataList = readCalendars()
        viewModel.setExtractedCalendarsData(calendarDataList)
    }

    @SuppressLint("MissingPermission")
    suspend fun readCalendars() = withContext(Dispatchers.IO) {
        val calendarsData = mutableListOf<CalendarData>()
        contentResolver.query(CALENDAR_TABLE_URI, EVENT_PROJECTION, null, null, null)?.use {cur ->
            while(cur.moveToNext()) {
                val calId = cur.getLong(PROJECTION_ID_INDEX)
                val displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                val accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                val ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
                calendarsData.add(
                    CalendarData(
                        id = calId,
                        displayName = displayName,
                        accountName = accountName,
                        ownerName = ownerName
                    )
                )
            }

        }
        calendarsData
    }

    override fun onUserSelectedCalendar(calendarData: CalendarData) {
        viewModel.setSelectedCalendarData(calendarData)
    }
}