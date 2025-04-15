package com.example.project.fragment.list


data class MonthItem(
    val year: Int,
    val monthNumber: Int,
    val monthName: String
)

data class YearHeader(
    val year: Int,
    val months: List<MonthItem>,
    var isExpanded: Boolean = false
)
