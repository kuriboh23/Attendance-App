package com.example.project.fragment.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.Leave

class LeaveAdapter : RecyclerView.Adapter<LeaveAdapter.LeaveViewHolder>() {

    private var leaveList: List<Leave> = emptyList()

    // ViewHolder class
    class LeaveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvLeaveDate)
        val tvType: TextView = itemView.findViewById(R.id.tvLeaveType)
        val tvStatus: TextView = itemView.findViewById(R.id.tvLeaveStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaveViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leave, parent, false)
        return LeaveViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaveViewHolder, position: Int) {
        val leave = leaveList[position]

        holder.tvDate.text = leave.date
        holder.tvType.text = leave.type
        holder.tvStatus.text = leave.status

        // Set status text color based on status
        val context = holder.itemView.context
        val statusColor = when (leave.status.lowercase()) {
            "approved" -> ContextCompat.getColor(context, android.R.color.holo_green_dark)
            "rejected" -> ContextCompat.getColor(context, android.R.color.holo_red_dark)
            "pending" -> ContextCompat.getColor(context, android.R.color.holo_orange_dark)
            else -> ContextCompat.getColor(context, android.R.color.black)
        }
        holder.tvStatus.setTextColor(statusColor) }

    override fun getItemCount(): Int = leaveList.size

    // Function to update the data
    fun setData(newLeaveList: List<Leave>) {
        leaveList = newLeaveList
        notifyDataSetChanged()
    }

}