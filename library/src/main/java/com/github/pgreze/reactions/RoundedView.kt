package com.github.pgreze.reactions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.util.TypedValue
import android.view.View

/**
 * Reaction selector floating dialog background.
 */
@SuppressLint("ViewConstructor")
class RoundedView(context: Context, private val config: ReactionsConfig) : View(context) {

    private val tag = RoundedView::class.java.simpleName

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = config.popupColor
        style = Paint.Style.FILL
        alpha = config.popupAlphaValue
    }

    private var offset = 0f

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        Log.d(tag, "onSizeChanged: w = $w; h = $h; oldW = $oldW; oldH = $oldH")
        offset = 0f
        Log.d(tag, "onSizeChanged: padding left = " + paddingLeft + "; padding right = " + paddingRight +
                "; padding top = " + paddingTop + "; padding bottom = " + paddingBottom)
        Log.d(tag, "onSizeChanged: xStart = " + (x + offset) + "; cornerSize = " + config.cornerSizeInDp + "; x = " + x)
    }

    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        // Draw the background rounded rectangle
        rect.left = offset
        rect.right = width.toFloat() - offset
        rect.top = offset
        rect.bottom = height.toFloat() - offset
        val cornerSizeInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, config.cornerSizeInDp.toFloat(), context.resources.displayMetrics)
        canvas.drawRoundRect(rect, cornerSizeInPx, cornerSizeInPx, paint)
    }
}
