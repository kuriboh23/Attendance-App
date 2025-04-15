package com.example.project.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.fragment.list.DateItem

class MonthAdapter(
    private val allItems: List<DateItem>,
    private val onMonthSelected: (DateItem.MonthItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_YEAR_HEADER = 0
        private const val TYPE_MONTH_ITEM = 1
    }

    // Filtered list based on expanded/collapsed state
    private val displayedItems: MutableList<DateItem> = mutableListOf()

    init {

        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        allItems.filterIsInstance<DateItem.YearHeader>().forEach {
            if (it.year == currentYear) {
                it.isExpanded = true
            }
        }
        // Initialize displayed items with all items
        updateDisplayedItems()
    }

    // Update the displayed items based on the expanded state of years
    private fun updateDisplayedItems() {
        displayedItems.clear()
        var currentYearHeader: DateItem.YearHeader? = null

        allItems.forEach { item ->
            when (item) {
                is DateItem.YearHeader -> {
                    currentYearHeader = item
                    displayedItems.add(item)
                }
                is DateItem.MonthItem -> {
                    if (currentYearHeader?.isExpanded == true) {
                        displayedItems.add(item)
                    }
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayedItems[position]) {
            is DateItem.YearHeader -> TYPE_YEAR_HEADER
            is DateItem.MonthItem -> TYPE_MONTH_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_YEAR_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_year_header, parent, false)
                YearHeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_month, parent, false)
                MonthViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayedItems[position]) {
            is DateItem.YearHeader -> {
                (holder as YearHeaderViewHolder).bind(item)
                holder.itemView.setOnClickListener {
                    item.isExpanded = !item.isExpanded
                    updateDisplayedItems()
                }
            }
            is DateItem.MonthItem -> {
                val monthHolder = holder as MonthViewHolder
                monthHolder.bind(item)

                holder.itemView.setOnClickListener {
                    // Deselect all months
                    allItems.filterIsInstance<DateItem.MonthItem>().forEach { it.isSelected = false }
                    // Select the clicked one
                    item.isSelected = true

                    // Refresh the full adapter (or optimize with notifyItemChanged)
                    updateDisplayedItems()

                    // Callback to parent
                    onMonthSelected(item)
                }
            }
        }
    }

    override fun getItemCount(): Int = displayedItems.size

    class YearHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(yearHeader: DateItem.YearHeader) {
            itemView.findViewById<TextView>(R.id.year_text).text = yearHeader.year.toString()
            val expandIcon = itemView.findViewById<ImageView>(R.id.expand_icon)
            expandIcon.rotation = if (yearHeader.isExpanded) 0f else -180f
        }
    }

    class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(monthItem: DateItem.MonthItem) {
            val numberView = itemView.findViewById<TextView>(R.id.month_number)
            val nameView = itemView.findViewById<TextView>(R.id.month_name)

            numberView.text = monthItem.monthNumber.toString()
            nameView.text = monthItem.monthName.uppercase()

            if (monthItem.isSelected) {
                itemView.setBackgroundResource(R.color.mainColor)
            } else {
                itemView.setBackgroundResource(R.color.black)
            }
        }
    }
}
