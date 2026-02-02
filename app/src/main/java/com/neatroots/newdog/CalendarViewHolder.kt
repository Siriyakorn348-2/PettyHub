package com.neatroots.newdog


import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate

class CalendarViewHolder(
    itemView: View,
    private val onItemListener: CalendarAdapter.OnItemListener,
    private val days: ArrayList<LocalDate?>
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    val parentView: View = itemView.findViewById(R.id.parentView)
    val dayOfMonth: TextView = itemView.findViewById(R.id.cellDayText)

    init {
        itemView.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(view: View) {
        val date = days?.getOrNull(adapterPosition)
        date?.let {
            onItemListener.onItemClick(adapterPosition, it)
        }
    }
}

