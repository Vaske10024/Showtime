package rs.edu.raf.rma.showtime.util

import kotlin.math.roundToInt

private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

fun imageUrl(path: String?, size: String = "w500"): String? {
    val cleanPath = path?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return if (cleanPath.startsWith("http://") || cleanPath.startsWith("https://")) {
        cleanPath
    } else {
        IMAGE_BASE_URL + size + "/" + cleanPath.trimStart('/')
    }
}

fun Double?.ratingText(): String = this?.let { oneDecimal(it) } ?: "-"

fun oneDecimal(value: Double): String {
    val tenths = (value * 10).roundToInt()
    return "${tenths / 10}.${kotlin.math.abs(tenths % 10)}"
}

fun scoreText(value: Double): String {
    val cents = (value * 100).roundToInt().coerceAtLeast(0)
    return "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
}
