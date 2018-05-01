package io.zla.reactions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow

/**
 * Entry point for reaction popup.
 */
class ReactionPopup(context: Context, reactionsConfig: ReactionsConfig)
    : PopupWindow(context), View.OnTouchListener {

    var reactionSelectedListener: ReactionSelectedListener? = null

    private var view: ReactionViewGroup = ReactionViewGroup(context, reactionsConfig).also {
        it.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER)

        it.reactionSelectedListener = ReactionSelectedListener { reaction, position ->
            dismiss()
            reactionSelectedListener?.onReactionSelected(reaction, position)
        }
    }
    private var isTouchAlwaysInsideButton = true
    private var buttonLocation = Point()

    init {
        contentView = FrameLayout(context).also { rootLayout ->
            rootLayout.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            rootLayout.addView(view)
        }
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!isShowing) {
            // Show fullscreen with button as context provider
            showAtLocation(v, Gravity.NO_GRAVITY, 0, 0)
            isTouchAlwaysInsideButton = true
            buttonLocation = IntArray(2)
                    .also(v::getLocationOnScreen)
                    .let { Point(it[0], it[1]) }
            view.show(buttonLocation, v)
        }

        isTouchAlwaysInsideButton = isTouchAlwaysInsideButton && event.inInsideView(buttonLocation, v)

        if (event.action == MotionEvent.ACTION_UP && isTouchAlwaysInsideButton) {
            // Keep action up and just reset to un-touch defaults
            view.resetChildrenToNormalSize()
        } else {
            // Forward last action up event
            view.onTouchEvent(event)
        }

        return true
    }

    private fun MotionEvent.inInsideView(location: Point, v: View): Boolean =
            rawX >= location.x
                    && rawX <= location.x + v.width
                    && rawY >= location.y
                    && rawY <= location.y + v.height

    override fun dismiss() {
        view.dismiss()
        super.dismiss()
    }
}
