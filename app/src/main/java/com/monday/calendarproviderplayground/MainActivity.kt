package com.monday.calendarproviderplayground

import android.Manifest.permission.READ_CALENDAR
import android.Manifest.permission.WRITE_CALENDAR
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.monday.calendarproviderplayground.base.IPresenter
import com.monday.calendarproviderplayground.rcv.CalendarAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.rcv_popup_layout.view.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val REQ_CODE_CALENDAR_PERMISSIONS = 100
    private val TAG = "[MainActivity]"

    private lateinit var btnCreateEvent: Button
    private lateinit var btnQueryEvent: Button
    private lateinit var edTxtEventId: EditText
    private lateinit var txtVEventData: TextView
    private lateinit var txtVSelectedCalendarData: TextView
    private lateinit var linLayCustomData: View
    private lateinit var linLayShowEventData: View

    private lateinit var presenter: IPresenter
    private val viewModel: MainViewModel by viewModels()
    private var popupWindow: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestCalendarPermissions()
        initUIComponents()
        presenter = Presenter(viewModel, contentResolver)
        setupPresenterObservations()
    }

    private fun setupPresenterObservations() {
        presenter.observeExtractedCalendarsData(lifecycle){
            handleExtractedCalendarData(it)
        }

        presenter.observeSelectedCalendarData(lifecycle){
            handleSelectedCalendarData(it)
        }

        presenter.observeGeneratedEventId(lifecycle){
            handleGeneratedEventId(it)
        }
    }

    private fun handleGeneratedEventId(generatedEventId: Long) {
        edTxtEventId.setText("Generated event id = $generatedEventId")
        linLayCustomData.visibility = View.VISIBLE
        linLayShowEventData.visibility = View.VISIBLE
    }

    private fun handleSelectedCalendarData(selectedCalendarData: CalendarData) {
        val txt = "Selected calendar:\nid = ${selectedCalendarData.id}\ndisplayName = ${selectedCalendarData.displayName}\naccountName = ${selectedCalendarData.accountName}\nownerName = ${selectedCalendarData.ownerName}"
        txtVSelectedCalendarData.text = txt
        popupWindow?.dismiss()
    }

    private fun handleExtractedCalendarData(calendarDataList: List<CalendarData>) {
        Log.d(TAG, "handleExtractedCalendarData(): calendarDataList = $calendarDataList")
        val view = LayoutInflater.from(applicationContext).inflate(R.layout.rcv_popup_layout, null)
        val rcv = view.rcv
        rcv.layoutManager = LinearLayoutManager(applicationContext)
        val adapter = CalendarAdapter(calendarDataList) {
            presenter.onUserSelectedCalendar(it)
        }
        rcv.adapter = adapter
        popupWindow = PopupWindow(view, 1000, 1000)
        popupWindow?.showAtLocation(constLay_main_content, Gravity.CENTER, 0, 0)
    }

    private fun showSelectCalendarDialog() {
        viewModel.viewModelScope.launch {
            presenter.extractCalendarsData()
        }
    }

    private fun initUIComponents() {
        btnCreateEvent = bnt_create_event
        btnQueryEvent = btn_query_event_data
        edTxtEventId = edTxtV_event_id
        txtVEventData = txtV_event_data
        txtVSelectedCalendarData = txtV_selected_calendar_data
        linLayCustomData = linLay_custom_data
        linLayShowEventData = linLay_show_event_data
        btnCreateEvent.setOnClickListener {
            viewModel.viewModelScope.launch {
                presenter.createCalendarEvent()
            }
        }
    }

    private fun requestCalendarPermissions() {
        requestPermissions(arrayOf(READ_CALENDAR, WRITE_CALENDAR), REQ_CODE_CALENDAR_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty()){
           when(requestCode){
               REQ_CODE_CALENDAR_PERMISSIONS -> {
                   Log.d(TAG, "onRequestPermissionsResult: case \"REQ_CODE_CALENDAR_PERMISSIONS\"")
                   grantResults.forEachIndexed { index, i ->
                       Log.d(TAG, "onRequestPermissionsResult: case \"REQ_CODE_CALENDAR_PERMISSIONS\", grantResult[$index].isPermitted = ${i == PackageManager.PERMISSION_GRANTED}")
                   }
                   if(grantResults.filterNot { it == PackageManager.PERMISSION_GRANTED }.isEmpty()) {
                       showSelectCalendarDialog()
                   }
               }
           }
        }
    }
}