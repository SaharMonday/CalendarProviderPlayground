package com.monday.calendarproviderplayground

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import android.provider.CalendarContract.ExtendedProperties
import android.util.Log
import com.monday.calendarproviderplayground.base.IPresenter
import com.monday.calendarproviderplayground.base.IViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


class Presenter(private val viewModel: IViewModel,
                private val contentResolver: ContentResolver) : IPresenter, IViewModel by viewModel {

    private val TAG = "Presenter"

    private val eventTitle = "My Custom event title"
    private val eventDescription = "My Custom event description"

    companion object {

        // "Calendars" table
        private val CALENDAR_PROJECTION: Array<String> = arrayOf(
            CalendarContract.Calendars._ID,                     // 0
            CalendarContract.Calendars.ACCOUNT_NAME,            // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
            CalendarContract.Calendars.OWNER_ACCOUNT,            // 3
            CalendarContract.Calendars.ACCOUNT_TYPE             // 4
        )

        // The indices for the projection array above.
        private const val CALENDAR_PROJECTION_ID_INDEX: Int = 0
        private const val CALENDAR_PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
        private const val CALENDAR_PROJECTION_DISPLAY_NAME_INDEX: Int = 2
        private const val CALENDAR_PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3
        private const val CALENDAR_PROJECTION_ACCOUNT_TYPE_INDEX: Int = 4

        // "Events" table
        private val EVENTS_PROJECTION: Array<String> = arrayOf(
            CalendarContract.Events._ID,  // 0
            CalendarContract.Events.CALENDAR_ID, // 1
            CalendarContract.Events.TITLE, // 2
            CalendarContract.Events.EVENT_LOCATION, //3
            CalendarContract.Events.DESCRIPTION, // 4
            CalendarContract.Events.DTSTART, //5
            CalendarContract.Events.DTEND, //6
            CalendarContract.Events.EVENT_TIMEZONE, // 7
            CalendarContract.Events.DURATION, // 8
            CalendarContract.Events.EVENT_COLOR // 9
        )

        // The indices for the projection array above.
        private const val EVENTS_PROJECTION_ID_INDEX: Int = 0
        private const val EVENTS_PROJECTION_CALENDAR_ID_INDEX: Int = 1
        private const val EVENTS_PROJECTION_TITLE_INDEX: Int = 2
        private const val EVENTS_PROJECTION_EVENT_LOCATION_INDEX: Int = 3
        private const val EVENTS_PROJECTION_DESCRIPTION_INDEX: Int = 4
        private const val EVENTS_PROJECTION_DTSTART_INDEX: Int = 5
        private const val EVENTS_PROJECTION_DTEND_INDEX: Int = 6
        private const val EVENTS_PROJECTION_EVENT_TIMEZONE_INDEX: Int = 7
        private const val EVENTS_PROJECTION_EVENT_DURATION_INDEX: Int = 8
        private const val EVENTS_PROJECTION_EVENT_COLOR_INDEX: Int = 9

        // "Extended Properties" table
        private val EXTENDED_PROPERTIES_PROJECTION: Array<String> = arrayOf(
            ExtendedProperties.EVENT_ID,  // 0
            ExtendedProperties.NAME,  // 1
            ExtendedProperties.VALUE  // 2
        )

        private const val EXTENDED_PROPERTIES_PROJECTION_EVENT_ID_INDEX: Int = 0
        private const val EXTENDED_PROPERTIES_PROJECTION_NAME_INDEX: Int = 1
        private const val EXTENDED_PROPERTIES_PROJECTION_VALUE_INDEX: Int = 2


        private val CALENDAR_TABLE_URI = CalendarContract.Calendars.CONTENT_URI
        private val EVENTS_TABLE_URI = CalendarContract.Events.CONTENT_URI
        private val EXTENDED_PROPERTIES_URI = CalendarContract.ExtendedProperties.CONTENT_URI
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
        contentResolver.query(CALENDAR_TABLE_URI, CALENDAR_PROJECTION, null, null, null)?.use { cur ->
            while(cur.moveToNext()) {
                val calId = cur.getLong(CALENDAR_PROJECTION_ID_INDEX)
                val displayName = cur.getString(CALENDAR_PROJECTION_DISPLAY_NAME_INDEX)
                val accountName = cur.getString(CALENDAR_PROJECTION_ACCOUNT_NAME_INDEX)
                val ownerName = cur.getString(CALENDAR_PROJECTION_OWNER_ACCOUNT_INDEX)
                val accountType = cur.getString(CALENDAR_PROJECTION_ACCOUNT_TYPE_INDEX)
                calendarsData.add(
                    CalendarData(
                        id = calId,
                        displayName = displayName,
                        accountName = accountName,
                        ownerName = ownerName,
                        accountType = accountType
                    )
                )
            }

        }
        calendarsData
    }

    override suspend fun queryGeneratedEventData() {
        viewModel.getGeneratedEventId()?.let { eventId ->
            queryEventData(eventId)?.let { eventData ->
                viewModel.setEventData(eventData)
            }
        } ?: Log.e(TAG, "queryGeneratedEventData: no generated event id found!")
    }

    override suspend fun addCustomDataToEvent(keyTxt: String, keyValue: String) {
        val generatedEventId = viewModel.getGeneratedEventId()
        val selectedCalendarData = viewModel.getSelectedCalendarData()
        if(generatedEventId != null && selectedCalendarData != null ) {
            addCustomData(EventCustomData(generatedEventId, keyTxt, keyValue), selectedCalendarData)
        } else {
            Log.e(TAG, "addCustomDataToEvent: no generated event id found!")
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun addCustomData(eventCustomData: EventCustomData, calendarData: CalendarData) = withContext(Dispatchers.IO) {
        val contentValues = ContentValues()
        contentValues.put(ExtendedProperties.EVENT_ID, eventCustomData.eventId.toString())
        contentValues.put(ExtendedProperties.NAME, eventCustomData.customDataKey)
        contentValues.put(ExtendedProperties.VALUE, eventCustomData.customDataValue)

        // we must use this "hack" in order to write to ExtendedProperties table (otherwise we'll have
        // to impl. a "syncAdapter".
        var extendedPropUri: Uri = EXTENDED_PROPERTIES_URI
        extendedPropUri = extendedPropUri.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(Calendars.ACCOUNT_NAME, calendarData.accountName)
            .appendQueryParameter(Calendars.ACCOUNT_TYPE, calendarData.accountType).build()

        contentResolver.insert(extendedPropUri, contentValues)?.let {uri ->
            val newRowId = uri.lastPathSegment?.toLong()?: -1
            Log.d(TAG, "addCustomData: newRowId = $newRowId")
            viewModel.setCustomDataAddedSuccessfully(newRowId)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun queryEventData(eventId: Long): EventData? = withContext(Dispatchers.IO) {
        var eventData: EventData? = null
        val selection = "((${CalendarContract.Events._ID} = ?))"
        contentResolver.query(EVENTS_TABLE_URI, EVENTS_PROJECTION, selection, arrayOf(eventId.toString()), null)?.use {cur ->
            while(cur.moveToNext()){
                val eventId = cur.getLong(EVENTS_PROJECTION_ID_INDEX)
                val calId = cur.getLong(EVENTS_PROJECTION_CALENDAR_ID_INDEX)
                val title = cur.getString(EVENTS_PROJECTION_TITLE_INDEX)
                val location = cur.getString(EVENTS_PROJECTION_EVENT_LOCATION_INDEX)
                val description = cur.getString(EVENTS_PROJECTION_DESCRIPTION_INDEX)
                val dtStart = cur.getLong(EVENTS_PROJECTION_DTSTART_INDEX)
                val dtEnd = cur.getLong(EVENTS_PROJECTION_DTEND_INDEX)
                val timeZoneStr = cur.getString(EVENTS_PROJECTION_EVENT_TIMEZONE_INDEX)
                val durationStr = cur.getString(EVENTS_PROJECTION_EVENT_DURATION_INDEX)
                val color = cur.getString(EVENTS_PROJECTION_EVENT_COLOR_INDEX)
                eventData = EventData(
                    id = eventId,
                    calId = calId,
                    title = title,
                    location = location,
                    description = description,
                    dtStartEpochMillis = dtStart,
                    dtEndEpochMillis = dtEnd,
                    timeZoneStr = timeZoneStr,
                    durationStr = durationStr,
                    eventColor = color
                )
            }
        }

        if(eventData != null){
            // check for extended properties (if exist)
            val selection = "((${CalendarContract.ExtendedProperties.EVENT_ID} = ?))"
            contentResolver.query(EXTENDED_PROPERTIES_URI, EXTENDED_PROPERTIES_PROJECTION, selection, arrayOf(eventId.toString()), null)?.use {cur ->
                val extendedPropertiesList = mutableListOf<EventCustomData>()
                while(cur.moveToNext()){
                    val name = cur.getString(EXTENDED_PROPERTIES_PROJECTION_NAME_INDEX)
                    val value = cur.getString(EXTENDED_PROPERTIES_PROJECTION_VALUE_INDEX)
                    extendedPropertiesList.add(EventCustomData(eventId = eventId, customDataKey = name, customDataValue = value))
                }
                if(extendedPropertiesList.isNotEmpty()) eventData?.customDataList = extendedPropertiesList
            }
        }

        eventData
    }

    override fun onUserSelectedCalendar(calendarData: CalendarData) {
        viewModel.setSelectedCalendarData(calendarData)
    }
}