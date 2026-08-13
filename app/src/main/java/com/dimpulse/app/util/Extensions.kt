package com.dimpulse.app.util

import java.util.Locale

fun formatMinutesToTimeString(totalMinutes: Int): String {
    val hours = (totalMinutes / 60) % 24
    val mins = totalMinutes % 60
    return String.format(Locale.getDefault(), "%02d:%02d", hours, mins)
}
