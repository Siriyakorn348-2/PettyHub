package com.neatroots.newdog.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neatroots.newdog.Model.EventModel
import com.neatroots.newdog.R

class EventAdapter(
    private var events: List<EventModel>,
    private val onEdit: (EventModel) -> Unit,
    private val onDelete: (EventModel) -> Unit,
    private val onClick: (EventModel) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.eventTitle)
        val time: TextView = view.findViewById(R.id.eventDate)
        val menuButton: ImageButton = view.findViewById(R.id.menuButton)
        val colorBar: View = view.findViewById(R.id.colorBar)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.colorBar.setBackgroundColor(event.color)
        holder.title.text = event.title
        holder.time.text = event.time

        holder.itemView.setOnClickListener {
            onClick(event)
        }

        holder.menuButton.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.event_menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.edit_event -> {
                        onEdit(event)
                        true
                    }
                    R.id.delete_event -> {
                        onDelete(event)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount() = events.size

    fun updateEvents(newEvents: List<EventModel>) {
        events = newEvents
        notifyDataSetChanged()
    }
}