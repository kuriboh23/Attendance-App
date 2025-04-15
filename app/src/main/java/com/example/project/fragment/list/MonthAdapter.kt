package com.example.project.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.fragment.list.MonthItem
import com.example.project.fragment.list.YearHeader

class MonthAdapter(
    private val allItems: List<YearHeader>,
    private val selectMonth: (MonthItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val displayedItems: MutableList<Any> = mutableListOf()

    companion object {
        private const val TYPE_YEAR = 0
        private const val TYPE_MONTH = 1
    }

    init {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        allItems.forEach {
            if (it.year == currentYear) {
                it.isExpanded = true
            }
        }
        updateDisplayedItems()
    }

    private fun updateDisplayedItems() {
        displayedItems.clear()
        for (year in allItems) {
            displayedItems.add(year)
            if (year.isExpanded) {
                displayedItems.addAll(year.months)
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayedItems[position]) {
            is YearHeader -> TYPE_YEAR
            is MonthItem -> TYPE_MONTH
            else -> throw IllegalArgumentException("Unknown type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_YEAR -> {
                val view = inflater.inflate(R.layout.item_year_header, parent, false)
                YearHeaderViewHolder(view)
            }
            TYPE_MONTH -> {
                val view = inflater.inflate(R.layout.item_month, parent, false)
                MonthViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = displayedItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayedItems[position]) {
            is YearHeader -> {
                (holder as YearHeaderViewHolder).bind(item)
                holder.itemView.setOnClickListener {
                    item.isExpanded = !item.isExpanded
                    updateDisplayedItems()
                }
            }
            is MonthItem -> {
                (holder as MonthViewHolder).bind(item)
                holder.itemView.setOnClickListener {
                    selectMonth(item)
                }
            }
        }
    }

    class YearHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(yearHeader: YearHeader) {
            itemView.findViewById<TextView>(R.id.year_text).text = yearHeader.year.toString()
            val expandIcon = itemView.findViewById<ImageView>(R.id.expand_icon)
            expandIcon.rotation = if (yearHeader.isExpanded) 0f else -180f
        }
    }

    class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(monthItem: MonthItem) {
            itemView.findViewById<TextView>(R.id.month_name).text = monthItem.monthName
            itemView.findViewById<TextView>(R.id.month_number).text = monthItem.monthNumber.toString()
        }
    }
}
