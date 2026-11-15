package com.vozbarrial.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.core.content.ContextCompat
import com.vozbarrial.R

private val pinCache = mutableMapOf<Int, Bitmap>()
private var userDotCache: Bitmap? = null

/** Ícono del pin de un reporte: círculo del color de la categoría, borde blanco e ícono blanco. */
fun reportPinBitmap(context: Context, category: String): Bitmap {
    val (iconRes, badgeColor) = when {
        category.contains("alumbrado", true) || category.contains("servicios", true) -> R.drawable.ic_pin_alumbrado to Color.parseColor("#ca8a04")
        category.contains("emergencia", true) -> R.drawable.ic_pin_emergencias to Color.parseColor("#dc2626")
        category.contains("infraestructura", true) || category.contains("vías", true) -> R.drawable.ic_pin_infraestructura to Color.parseColor("#ea580c")
        category.contains("mascota", true) -> R.drawable.ic_pin_mascotas to Color.parseColor("#9333ea")
        category.contains("seguridad", true) -> R.drawable.ic_pin_security to Color.parseColor("#2563eb")
        else -> R.drawable.ic_pin_comunidad to Color.parseColor("#16a34a")
    }

    return pinCache.getOrPut(iconRes) {
        val size = 64
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            color = badgeColor
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)

        val borderPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, borderPaint)

        val drawable = ContextCompat.getDrawable(context, iconRes)
        if (drawable != null) {
            val iconSize = 34
            val left = (size - iconSize) / 2
            val top = (size - iconSize) / 2
            drawable.setBounds(left, top, left + iconSize, top + iconSize)
            drawable.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            drawable.draw(canvas)
        }
        bitmap
    }
}

/** Punto azul que marca la ubicación del usuario en el mapa. */
fun userLocationDotBitmap(): Bitmap = userDotCache ?: Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888).apply {
    val canvas = Canvas(this)
    val paint = Paint().apply {
        color = Color.parseColor("#3188E8")
        isAntiAlias = true
    }
    canvas.drawCircle(24f, 24f, 22f, paint)
    paint.color = Color.WHITE
    canvas.drawCircle(24f, 24f, 10f, paint)
}.also { userDotCache = it }
