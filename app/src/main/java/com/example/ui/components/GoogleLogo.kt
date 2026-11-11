package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * آرم چهاررنگ گوگل.
 *
 * کتابخانه‌ی آیکون‌های متریال آرم تجاری گوگل را ندارد و ساخت ImageVector از
 * روی داده‌ی مسیر، به رجیستر داخلی Compose نیاز دارد؛ پس همین شکل با Canvas و
 * همان چهار رنگ استاندارد برند رسم می‌شود. اندازه‌ی مرجع ۲۴ واحد است.
 */
private val googleBlue = Color(0xFF4285F4)
private val googleGreen = Color(0xFF34A853)
private val googleYellow = Color(0xFFFBBC05)
private val googleRed = Color(0xFFEA4335)

@Composable
fun GoogleLogo(modifier: Modifier = Modifier) {
    Canvas(modifier.size(20.dp)) {
        val stroke = size.width * 0.19f
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(stroke / 2f, stroke / 2f)

        fun arc(color: Color, start: Float, sweep: Float) = drawArc(
            color = color,
            startAngle = start,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )

        // چرخش ساعتگرد از سمت چپ (۱۸۰°) شروع می‌شود
        arc(googleRed, start = 180f, sweep = 95f)    // قرمز: چپ تا بالا
        arc(googleYellow, start = 90f, sweep = 50f)  // زرد: پایین-چپ
        arc(googleGreen, start = 25f, sweep = 65f)   // سبز: پایین-راست
        arc(googleBlue, start = -40f, sweep = 65f)   // آبی: بالا-راست

        // میله‌ی افقی آبی که مرکز را به قوس راست وصل می‌کند
        drawRect(
            color = googleBlue,
            topLeft = Offset(size.width / 2f, size.height / 2f - stroke / 2f),
            size = Size(size.width / 2f - stroke / 2f, stroke)
        )
    }
}
