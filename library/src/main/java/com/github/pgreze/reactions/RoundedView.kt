package com.github.pgreze.reactions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.view.View

/**
 * Reaction selector floating dialog background.
 */
@SuppressLint("ViewConstructor")
class RoundedView(context: Context, private val config: ReactionsConfig) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = config.popupColor
        style = Paint.Style.FILL
        alpha = config.popupAlphaValue
    }

    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        // Draw the background rounded rectangle
        rect.left = 0f
        rect.right = width.toFloat()
        rect.top = 0f
        rect.bottom = height.toFloat()
        val popupCornerRadius = config.popupCornerRadius.toFloat()
        canvas.drawRoundRect(rect, popupCornerRadius, popupCornerRadius, paint)
    }
}
