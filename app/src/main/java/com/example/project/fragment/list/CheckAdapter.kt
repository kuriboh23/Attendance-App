package com.example.project.fragment.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.Check
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckAdapter : RecyclerView.Adapter<CheckAdapter.CheckViewHolder>() {

    private var checkList = listOf<Check>()

    inner class CheckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txDayNum: TextView = itemView.findViewById(R.id.txDayNum)
        val txDayText: TextView = itemView.findViewById(R.id.txDayText)
        val tvCheckInTime: TextView = itemView.findViewById(R.id.tvCheckInTime)
        val tvCheckOutTime: TextView = itemView.findViewById(R.id.tvCheckOutTime)
        val tvTotalHours: TextView = itemView.findViewById(R.id.tvTotalHours)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckViewHolder {
        return CheckViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CheckViewHolder, position: Int) {
        val currentItem = checkList[position]
        // Example usage:
         holder.txDayNum.text = formatDateNum(currentItem.date)
         holder.txDayText.text = formatDateName(currentItem.date)
         holder.tvCheckInTime.text = currentItem.checkInTime
         holder.tvCheckOutTime.text = currentItem.checkOutTime
         holder.tvTotalHours.text = currentItem.duration
    }

    override fun getItemCount(): Int = checkList.size

    fun formatDateName(timestamp: Long): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("EEE", Locale.getDefault()) // "07 Mon"
        return sdf.format(date)
    }
    fun formatDateNum(timestamp: Long): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd", Locale.getDefault()) // "07 Mon"
        return sdf.format(date)
    }

    fun setData(newList: List<Check>) {
        checkList = newList
        notifyDataSetChanged()
    }
}
