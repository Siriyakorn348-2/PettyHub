package com.neatroots.newdog.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neatroots.newdog.EventDogMainActivity
import com.neatroots.newdog.Model.NotiDog
import com.neatroots.newdog.R
import java.text.SimpleDateFormat
import java.util.*

class NotiDogAdapter(private val notificationList: List<NotiDog>) :
    RecyclerView.Adapter<NotiDogAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList[position]
        holder.messageTextView.text = notification.message

        holder.timestampTextView.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(notification.timestamp))


        holder.itemView.setOnClickListener {
            val dateFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
            val dateString = dateFormat.format(Date(notification.timestamp))

            val intent = Intent(holder.itemView.context, EventDogMainActivity::class.java).apply {
                putExtra("SELECTED_DATE", dateString)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = notificationList.size

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.notificationMessage)
        val timestampTextView: TextView = itemView.findViewById(R.id.notificationTimestamp)
    }
}