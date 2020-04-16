package com.monday.calendarproviderplayground.rcv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.monday.calendarproviderplayground.CalendarData
import com.monday.calendarproviderplayground.R
import kotlinx.android.synthetic.main.calendar_item.view.*

class CalendarAdapter(private val dataSet: List<CalendarData>,
                      private val clickListener: (CalendarData) -> Unit) : RecyclerView.Adapter<CalendarItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_item, parent, false)
        return CalendarItemViewHolder(view)
    }

    override fun getItemCount(): Int = dataSet.size

    override fun onBindViewHolder(holder: CalendarItemViewHolder, position: Int) {
        val dataItem = dataSet[position]
        holder.txtVDisplayName.text = dataItem.displayName
        holder.itemView.setOnClickListener { clickListener.invoke(dataItem) }
    }
}

class CalendarItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val txtVDisplayName = itemView.txtV_calendar_name
}