package uk.ac.abertay.plannorfunctions.util

import java.math.RoundingMode
import java.text.DecimalFormat

fun formatTime(seconds: String, minutes: String, hours: String): String {
    return "$hours:$minutes:$seconds"
}

fun Int.pad(): String {
    return this.toString().padStart(2, '0')
}
fun roundFloat(num: Float): String {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.DOWN
    return df.format(num)
}
