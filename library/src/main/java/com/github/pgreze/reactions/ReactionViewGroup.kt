package com.github.pgreze.reactions

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.pgreze.reactions.PopupGravity.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * This ViewGroup displays Reactions and handles interactions with them.
 *
 * It should most often be used within a ReactionPopup
 * and given height and width attributes as match_parent to properly draw the View.
 */
@SuppressLint("ViewConstructor")
class ReactionViewGroup(context: Context, private val config: ReactionsConfig) : ViewGroup(context) {

    private val tag = ReactionViewGroup::class.java.simpleName

    private val horizontalPadding: Int = config.horizontalMargin
    private val verticalPadding: Int = config.verticalMargin

    private var iconDivider: Int = horizontalPadding / 2

    private var smallIconSize: Int
    private var mediumIconSize: Int = config.reactionSize
    private var largeIconSize: Int = 2 * mediumIconSize

    private var firstClick = Point()
    private var parentLocation = Point()
    private var parentSize: Size = Size(0, 0)

    private var dialogWidth: Int
    private var dialogHeight: Int = mediumIconSize + 2 * verticalPadding

    init {
        val nIcons = config.reactions.size

        dialogWidth = horizontalPadding * 2 +
                mediumIconSize * nIcons +
                iconDivider * nIcons.minus(1)

        smallIconSize = (dialogWidth
                - horizontalPadding * 2
                - largeIconSize
                - iconDivider * nIcons.minus(1)
                ) / nIcons.minus(1)
    }

    private val background = RoundedView(context, config)
            .also {
                it.layoutParams = LayoutParams(dialogWidth, dialogHeight)
                addView(it)
            }
    private val reactions: List<ReactionView> = config.reactions
            .map {
                ReactionView(context, it).also {
                    it.layoutParams = LayoutParams(mediumIconSize, mediumIconSize)
                    addView(it)
                }
            }
            .toList()
    private val reactionText: TextView = TextView(context)
            .also {
                it.textSize = config.textSize
                it.setTextColor(config.textColor)
                it.setPadding(
                        config.textHorizontalPadding,
                        config.textVerticalPadding,
                        config.textHorizontalPadding,
                        config.textVerticalPadding)
                it.background = config.textBackground
                it.visibility = View.GONE
                addView(it)
            }

    private var dialogX: Int = 0
    private var dialogY: Int = 0

    private var currentState: ReactionViewState? = null
        set(value) {
            if (field == value) return

            val oldValue = field
            field = value
            Log.i(tag, "State: $oldValue -> $value")
            when (value) {
                is ReactionViewState.Boundary -> animTranslationY(value)
                is ReactionViewState.WaitingSelection -> animSize(null)
                is ReactionViewState.Selected -> animSize(value)
            }
        }

    private var currentAnimator: ValueAnimator? = null
        set(value) {
            field?.cancel()

            field = value
            reactionText.visibility = View.GONE
            field?.duration = 100
            field?.start()
        }

    private var isFirstTouchAlwaysInsideButton = true
    private var isIgnoringFirstReaction: Boolean = false

    var reactionSelectedListener: ReactionSelectedListener? = null

    var dismissListener: (() -> Unit)? = null

    // onLayout/onMeasure https://newfivefour.com/android-custom-views-onlayout-onmeasure.html
    // Detailed  https://proandroiddev.com/android-draw-a-custom-view-ef79fe2ff54b
    // Advanced sample: https://github.com/frogermcs/LikeAnimation/tree/master/app/src/main/java/frogermcs/io/likeanimation

    override fun onSizeChanged(width: Int, height: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(width, height, oldW, oldH)

        dialogX = when (config.popupGravity) {
            DEFAULT -> // Slightly on right of parent's left position
                firstClick.x - horizontalPadding - mediumIconSize / 2
            PARENT_LEFT -> // Fallback to SCREEN_RIGHT
                parentLocation.x
                    .takeUnless { it + dialogWidth > width }
                    ?: width - dialogWidth - config.popupMargin
            PARENT_RIGHT -> // Fallback to SCREEN_LEFT
                (parentLocation.x + parentSize.width - dialogWidth)
                    .takeUnless { it < 0 }
                    ?: config.popupMargin
            SCREEN_LEFT ->
                config.popupMargin
            SCREEN_RIGHT ->
                width - dialogWidth - config.popupMargin
            CENTER ->
                (width - dialogWidth) / 2
        }
        // Fallback to center if invalid position
        if (dialogX < 0 || dialogX + dialogWidth >= width) {
            dialogX = max(0, (width - dialogWidth) / 2)
        }

        // Y position will be slightly on top of parent view
        dialogY = parentLocation.y - dialogHeight * 2
        if (dialogY < 0) {
            // Below parent view
            dialogY = parentLocation.y + parentSize.height + dialogHeight
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        background.also { view ->
            val translationX = view.translationX.toInt()
            val translationY = view.translationY.toInt()
            view.layout(
                    dialogX + translationX,
                    dialogY + mediumIconSize - view.layoutParams.height + translationY,
                    dialogX + dialogWidth + translationX,
                    dialogY + dialogHeight + translationY)
        }

        var prevX = 0
        reactions.forEach { view ->
            val translationX = view.translationX.toInt()
            val translationY = view.translationY.toInt()

            val bottom = dialogY + dialogHeight - verticalPadding + translationY
            val top = bottom - view.layoutParams.height + translationY
            val left = dialogX + horizontalPadding + prevX + translationX
            val right = left + view.layoutParams.width + translationX
            view.layout(left, top, right, bottom)

            prevX += view.width + iconDivider
        }

        if (reactionText.visibility == View.VISIBLE) {
            reactionText.measure(0, 0)
            val selectedView = (currentState as? ReactionViewState.Selected)?.view ?: return
            val top = selectedView.top - min(selectedView.layoutParams.size, reactionText.measuredHeight * 2)
            val bottom = top + reactionText.measuredHeight
            val left = selectedView.left + (selectedView.right - selectedView.left) / 2f - reactionText.measuredWidth / 2f
            val right = left + reactionText.measuredWidth
            reactionText.layout(left.toInt(), top, right.toInt(), bottom)
        }
    }

    fun show(event: MotionEvent, parent: View) {
        this.firstClick = Point(event.rawX.roundToInt(), event.rawY.roundToInt())
        this.parentLocation = IntArray(2)
                .also(parent::getLocationOnScreen)
                .let { Point(it[0], it[1]) }
        parentSize = Size(parent.width, parent.height)
        isFirstTouchAlwaysInsideButton = true
        isIgnoringFirstReaction = true

        // Resize, could be fixed with later resolved width/height
        onSizeChanged(width, height, width, height)

        // Appear effect
        visibility = View.VISIBLE
        currentState = ReactionViewState.Boundary.Appear(path = dialogHeight to 0)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        isFirstTouchAlwaysInsideButton = isFirstTouchAlwaysInsideButton && inInsideParentView(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // Track first moves of the first click, avoiding to auto-select first reaction
                if (isIgnoringFirstReaction) {
                    val v = reactions.first()
                    val isBelowFirstReaction = event.rawX >= v.x && event.rawX <= v.right &&
                            event.rawY >= v.y + v.height && event.rawY <= v.y + v.height + dialogHeight
                    isIgnoringFirstReaction = isIgnoringFirstReaction &&
                            (isBelowFirstReaction || isFirstTouchAlwaysInsideButton)
                    if (isIgnoringFirstReaction) return true
                }

                // Ignores when appearing
                if (currentState is ReactionViewState.Boundary.Appear) return true

                val view = getIntersectedIcon(event.rawX, event.rawY)
                if (view == null) {
                    currentState = ReactionViewState.WaitingSelection
                } else if ((currentState as? ReactionViewState.Selected)?.view != view) {
                    currentState = ReactionViewState.Selected(view)
                }
            }
            MotionEvent.ACTION_UP -> {
                // Ignores it if first move was always inside parent view
                if (isFirstTouchAlwaysInsideButton) {
                    isFirstTouchAlwaysInsideButton = false
                    return true
                }

                val reaction = getIntersectedIcon(event.rawX, event.rawY)?.reaction
                val position = reaction?.let { config.reactions.indexOf(it) } ?: -1
                if (reactionSelectedListener?.invoke(position)?.not() == true) {
                    currentState = ReactionViewState.WaitingSelection
                } else { // reactionSelectedListener == null or reactionSelectedListener() == true
                    dismiss()
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                currentState = ReactionViewState.WaitingSelection
            }
        }
        return true
    }

    fun resetChildrenToNormalSize() {
        currentState = ReactionViewState.WaitingSelection
    }

    fun dismiss() {
        if (currentState == null) return

        currentState = ReactionViewState.Boundary.Disappear(
                (currentState as? ReactionViewState.Selected)?.view,
                0 to dialogHeight)
    }

    private fun inInsideParentView(event: MotionEvent): Boolean =
            event.rawX >= parentLocation.x
                    && event.rawX <= parentLocation.x + parentSize.width
                    && event.rawY >= parentLocation.y
                    && event.rawY <= parentLocation.y + parentSize.height

    private fun getIntersectedIcon(x: Float, y: Float): ReactionView? =
            reactions.firstOrNull {
                x >= it.location.x - horizontalPadding
                        && x < it.location.x + it.width + iconDivider
                        && y >= it.location.y - horizontalPadding
                        && y < it.location.y + it.height + dialogHeight + iconDivider
            }

    private fun animTranslationY(boundary: ReactionViewState.Boundary) {
        // Init views
        val initialAlpha = if (boundary is ReactionViewState.Boundary.Appear) 0f else 1f
        forEach {
            it.alpha = initialAlpha
            it.translationY = boundary.path.first.toFloat()
            if (boundary is ReactionViewState.Boundary.Appear) {
                it.layoutParams.size = mediumIconSize
            }
        }
        requestLayout()

        // TODO: animate selected index if boundary == Disappear
        currentAnimator = ValueAnimator.ofFloat(0f, 1f)
                .apply {
                    addUpdateListener {
                        val progress = it.animatedValue as Float
                        val translationY = boundary.path.progressMove(progress).toFloat()

                        forEach {
                            it.translationY = translationY
                            it.alpha = if (boundary is ReactionViewState.Boundary.Appear) {
                                progress
                            } else {
                                1 - progress
                            }
                        }

                        // Invalidate children positions
                        requestLayout()
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}

                        override fun onAnimationEnd(animation: Animator?) {
                            when (boundary) {
                                is ReactionViewState.Boundary.Appear -> {
                                    currentState = ReactionViewState.WaitingSelection
                                }
                                is ReactionViewState.Boundary.Disappear -> {
                                    visibility = View.GONE
                                    currentState = null
                                    // Notify listener
                                    dismissListener?.invoke()
                                }
                            }
                        }

                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationStart(animation: Animator?) {}
                    })
                }
    }

    private fun animSize(state: ReactionViewState.Selected?) {
        val paths = reactions.map {
            it.layoutParams.size to when {
                state == null -> mediumIconSize
                state.view == it -> largeIconSize
                else -> smallIconSize
            }
        }

        currentAnimator = ValueAnimator.ofFloat(0f, 1f)
                .apply {
                    addUpdateListener {
                        val progress = it.animatedValue as Float

                        reactions.forEachIndexed { index, view ->
                            val size = paths[index].progressMove(progress)
                            view.layoutParams.size = size
                        }

                        // Invalidate children positions
                        requestLayout()
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}

                        override fun onAnimationEnd(animation: Animator?) {
                            val index = state?.view ?: return
                            reactionText.text =
                                    config.reactionTextProvider(reactions.indexOf(index))
                                    ?: return
                            reactionText.visibility = View.VISIBLE
                            requestLayout()
                        }

                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationStart(animation: Animator?) {}
                    })
                }
    }
}

private var ViewGroup.LayoutParams.size: Int
    get() = width
    set(value) {
        width = value
        height = value
    }

/** Replace with [android.util.Size] when minSdkVersion = 21 */
private class Size(val width: Int, val height: Int)

private inline fun ViewGroup.forEach(action: (View) -> Unit) {
    for (child in 0 until childCount) {
        action(getChildAt(child))
    }
}

private fun progressMove(from: Int, to: Int, progress: Float): Int =
        from + ((to - from) * progress).toInt()

private fun Pair<Int, Int>.progressMove(progress: Float): Int =
        progressMove(first, second, progress)

sealed class ReactionViewState {

    sealed class Boundary(val path: Pair<Int, Int>) : ReactionViewState() {

        /** All views are moving from +translationY to 0 with normal size */
        class Appear(path: Pair<Int, Int>) : Boundary(path)

        /**
         * Different behaviour considering [selectedView]:
         * - if no [selectedView], going down with normal size
         * - otherwise going down
         *   while [selectedView] is going (idx=0=up, other=up/left) and decreasing size
         */
        class Disappear(val selectedView: ReactionView?, path: Pair<Int, Int>) : Boundary(path)
    }

    object WaitingSelection : ReactionViewState()

    /**
     * Increase size of selected [view] while others are decreasing.
     */
    class Selected(val view: ReactionView) : ReactionViewState()
}
