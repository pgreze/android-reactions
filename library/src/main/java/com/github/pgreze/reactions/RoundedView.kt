package com.github.pgreze.reactions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.Log
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
        alpha = 230
    }

    private var cornerSize = 0f

    private var xStart = 0f
    private var yStart = 0f

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        Log.d(tag, "onSizeChanged: w = $w; h = $h; oldW = $oldW; oldH = $oldH")

        val xPad = if (paddingLeft + paddingRight <= 0) {
            config.horizontalMargin * 2f
        } else {
            (paddingLeft + paddingRight) / 2f
        }
        val bPad = xPad / 2
        val nIcons = config.reactions.size
        val regIconSize = (w - (2 * xPad + (nIcons - 1) * bPad)) / nIcons
        cornerSize = xPad + regIconSize / 2
        xStart = cornerSize
        yStart = 0f

        Log.d(tag, "onSizeChanged: padding left = " + paddingLeft + "; padding right = " + paddingRight +
                "; padding top = " + paddingTop + "; padding bottom = " + paddingBottom)
        Log.d(tag, "onSizeChanged: xStart = " + (x + xStart) + "; cornerSize = " + cornerSize + "; x = " + x)
    }

    private val path = Path()
    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        // Draw the background rounded rectangle
        path.moveTo(xStart, yStart)

        // Top line between curves
        path.lineTo(xStart + (width - 2 * cornerSize), yStart)

        // First curve, right side
        rect.left = xStart + width - 2 * cornerSize
        rect.right = rect.left + cornerSize
        rect.top = yStart
        rect.bottom = yStart + height
        path.arcTo(rect, 270f, 180f)

        // Bottom line between curves
        path.lineTo(xStart, yStart + height)

        // Second curve, left side
        rect.left = xStart - cornerSize
        rect.right = xStart
        rect.top = yStart
        rect.bottom = yStart + height
        path.arcTo(rect, 90f, 180f)
        path.close()

        canvas.drawPath(path, paint)
        path.reset()
    }
}
