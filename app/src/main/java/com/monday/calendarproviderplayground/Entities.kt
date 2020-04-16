package com.monday.calendarproviderplayground

data class CalendarData(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val ownerName: String,
    val accountType: String
)

data class EventData(
    val id: Long,
    val calId: Long,
    val title: String?,
    val location: String?,
    val description: String?,
    val dtStartEpochMillis: Long,
    val dtEndEpochMillis: Long,
    val timeZoneStr: String?,
    val durationStr: String?,
    val eventColor: String?,
    var customDataList: List<EventCustomData>? = null
)

data class EventCustomData(
    val eventId: Long,
    val customDataKey: String,
    val customDataValue: String
)