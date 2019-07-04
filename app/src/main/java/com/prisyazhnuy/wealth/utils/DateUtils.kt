package com.prisyazhnuy.wealth.utils

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*

const val FULL_DATE_PATTERN = "MMM YYYY" //Jun 2019

private val fullDatePattern = DateTimeFormat
        .forPattern(FULL_DATE_PATTERN)
        .withZone(DateTimeZone.getDefault())

fun getDisplayedFullDate(date: DateTime) = getFormattedDate(fullDatePattern, date)

private fun getFormattedDate(dateFormat: DateTimeFormatter, date: DateTime) =
        dateFormat.withZone(DateTimeZone.forTimeZone(Calendar.getInstance().timeZone)).print(date)

