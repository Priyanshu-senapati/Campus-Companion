package com.rvu.campuscompanion.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.rvu.campuscompanion.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.toast(message: String, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun String.isValidEmail(): Boolean =
    isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidRvuEmail(): Boolean =
    isValidEmail() && lowercase(Locale.ROOT).endsWith(Constants.ALLOWED_EMAIL_DOMAIN)

fun Long.toFormattedDate(pattern: String = "dd MMM yyyy"): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))

fun Long.toFormattedDateTime(): String =
    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(this))

fun Long.toRelativeTime(): String {
    val diff = System.currentTimeMillis() - this
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 7 * 86_400_000 -> "${diff / 86_400_000}d ago"
        else -> toFormattedDate()
    }
}

fun Int.toAttendanceColor(context: Context): Int {
    val resId = when {
        this >= Constants.ATTENDANCE_GOOD -> R.color.attendance_good
        this >= Constants.ATTENDANCE_WARN -> R.color.attendance_warn
        else -> R.color.attendance_bad
    }
    return ContextCompat.getColor(context, resId)
}

fun Float.toAttendanceColor(context: Context): Int = this.toInt().toAttendanceColor(context)
