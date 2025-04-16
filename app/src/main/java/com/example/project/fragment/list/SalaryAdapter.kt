package com.example.project.fragment.list

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.TimeManager
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class SalaryAdapter(private var timeManagers: List<TimeManager>
) : RecyclerView.Adapter<SalaryAdapter.TimeManagerViewHolder>() {

    class TimeManagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txDayNum: TextView = itemView.findViewById(R.id.txDayNum)
        val txDayText: TextView = itemView.findViewById(R.id.txDayText)
        val tvWorkTime: TextView = itemView.findViewById(R.id.tv_work_time)
        val tvExtraTime: TextView = itemView.findViewById(R.id.tv_extra_time)
        val tvSalary: TextView = itemView.findViewById(R.id.tv_salary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeManagerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_manager, parent, false)
        return TimeManagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeManagerViewHolder, position: Int) {
        val timeManager = timeManagers[position]

        // Format date
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val formattedDate = try {
            val date = inputFormat.parse(timeManager.date)
            outputFormat.format(date)
        } catch (e: Exception) {
            timeManager.date
        }

        // Calculate salary for the day (workTime + extraTime) * 200
        val dailySalary = (timeManager.workTime + timeManager.extraTime) * 200

        val dayNum = timeManager.date.split("-")[2]

        holder.txDayNum.text = dayNum
        holder.txDayText.text = getDayAbbreviation(timeManager.date)

        holder.tvWorkTime.text = "${timeManager.workTime} hours"
        holder.tvExtraTime.text = "${timeManager.extraTime} hours"
        holder.tvSalary.text = "MAD $dailySalary"
    }

    override fun getItemCount(): Int = timeManagers.size

    fun updateData(newTimeManagers: List<TimeManager>) {
        timeManagers = newTimeManagers
        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayAbbreviation(dateString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val date = LocalDate.parse(dateString, inputFormatter)
        val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH) // "EEE" gives "Fri"
        return date.format(dayFormatter)
    }
}