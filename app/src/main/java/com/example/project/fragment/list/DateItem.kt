package com.example.project.fragment.list

sealed class DateItem {
    data class YearHeader(val year: Int, var isExpanded: Boolean = false) : DateItem()
    data class MonthItem(val monthNumber: Int, val monthName: String, val year: Int, var isSelected: Boolean = false) : DateItem()
}