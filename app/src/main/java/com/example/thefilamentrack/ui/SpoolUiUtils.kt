package com.example.thefilamentrack.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun colorFromHex(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    Color.Gray
}

@Composable
fun progressColorFor(pct: Float): Color = when {
    pct > 0.5f -> MaterialTheme.colorScheme.primary
    pct > 0.2f -> Color(0xFFE8832A)
    else -> MaterialTheme.colorScheme.error
}
