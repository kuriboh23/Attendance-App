package com.example.project.fragment.list

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.Check
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CheckViewHolder, position: Int) {
        val currentItem = checkList[position]
        val dayNum = currentItem.date.split("-")[2]

         holder.txDayNum.text = dayNum
         holder.txDayText.text = getDayAbbreviation(currentItem.date)
         holder.tvCheckInTime.text = currentItem.checkInTime
         holder.tvCheckOutTime.text = currentItem.checkOutTime
        val durationInSeconds = currentItem.durationInSecond
        val hours = durationInSeconds / 3600
        val minutes = (durationInSeconds % 3600) / 60
        val durationStr = "${hours}h ${minutes}m"
         holder.tvTotalHours.text =durationStr
    }

    override fun getItemCount(): Int = checkList.size

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayAbbreviation(dateString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val date = LocalDate.parse(dateString, inputFormatter)
        val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH) // "EEE" gives "Fri"
        return date.format(dayFormatter)
    }

    fun setData(newList: List<Check>) {
        checkList = newList
        notifyDataSetChanged()
    }
}
