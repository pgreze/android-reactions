package io.zla.reactions

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import java.util.*

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

    private var dialogWidth: Int = 0
    private var dialogHeight: Int = 0

    private var iconDivider: Int = 0

    private var smallIconSize: Int = 0
    private var mediumIconSize: Int = 0
    private var largeIconSize: Int = 0

    /** Context location (top/left for a button using ReactionPopup, 0/0 for tests) */
    private var parentLocation = Point()
    private var parentHeight: Int = 0

    private var dialogX: Int = 0
    private var dialogY: Int = 0
    private var cornerSize: Int = 0

    private var isFirstLayout = true

    private val background = RoundedView(context, config)
    private val reactions: List<ReactionView>

    init {
        // Add background
        addView(background)
        // Add all reactions
        reactions = ArrayList<ReactionView>(config.reactions.size).also { list ->
            for (reaction in config.reactions) {
                val iconView = ReactionView(context, reaction)
                addView(iconView)
                list.add(iconView)
            }
        }
    }

    var reactionSelectedListener: ReactionSelectedListener? = null

    override fun onSizeChanged(width: Int, height: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(width, height, oldW, oldH)
        Log.d(tag, "onSizeChanged: oldW = $oldW; oldH = $oldH; w = $width; h = $height")

        iconDivider = horizontalPadding / 2

        val nIcons = config.reactions.size

        mediumIconSize = config.reactionSize

        dialogWidth = horizontalPadding * 2 +
                mediumIconSize * nIcons +
                iconDivider * nIcons.minus(1)

        largeIconSize = 2 * mediumIconSize

        smallIconSize = (dialogWidth
                - horizontalPadding * 2
                - largeIconSize
                - iconDivider * nIcons.minus(1)
                ) / nIcons.minus(1)

        dialogHeight = mediumIconSize + 2 * verticalPadding

        cornerSize = horizontalPadding + mediumIconSize / 2

        // X position will be slightly on right of parent's left position
        dialogX = parentLocation.x + cornerSize + horizontalPadding
        if (dialogX + dialogWidth >= width) {
            // Center dialog
            dialogX = Math.max(0, (width - dialogWidth) / 2) + cornerSize
        }
        // Y position will be slightly on top of parent view
        dialogY = parentLocation.y - dialogHeight - parentHeight
        if (parentHeight < 0) {
            // Below parent view
            dialogY = parentLocation.y + parentHeight
        }

        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
    }

    private val animators = ArrayList<Animator>(config.reactions.size + 1)
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Log.d(tag, "onLayout: l = $l; t = $t; r = $r; b = $b")

        // If there are small icon sizes adjust the height of the background to them
        background.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

        // Animate size changes for each view
        animators.clear()
        loop@ for (i in 0 until childCount) {
            val view = getChildAt(i)

            animators.add(when (view) {
                is ReactionView -> {
                    val targetSize = when (view.mode) {
                        ReactionView.Mode.SMALL -> smallIconSize
                        ReactionView.Mode.MEDIUM -> mediumIconSize
                        ReactionView.Mode.LARGE -> largeIconSize
                    }

                    if (isFirstLayout) {
                        layoutIconView(view, targetSize.toFloat())
                        continue@loop
                    }

                    ValueAnimator.ofFloat(view.width.toFloat(), targetSize.toFloat()).apply {
                        addUpdateListener { animation -> layoutIconView(view, animation.animatedValue as Float) }
                    }
                }
                is RoundedView -> {
                    Log.d(tag, "onLayout: dialogX = $dialogX; cornerSize = $cornerSize")
                    val top = if (getSelectedIcon() != null) {
                        dialogY + (mediumIconSize - smallIconSize)
                    } else {
                        dialogY
                    }.toFloat()

                    if (isFirstLayout) {
                        layoutRoundedView(view, top)
                        continue@loop
                    }

                    ValueAnimator.ofFloat(view.y, top).apply {
                        addUpdateListener { animation -> layoutRoundedView(view, animation.animatedValue as Float) }
                    }
                }
                else -> continue@loop
            })
        }

        if (!isFirstLayout) {
            AnimatorSet().apply {
                playTogether(animators)
                duration = 100
            }.start()
        }
        isFirstLayout = false
    }

    private fun layoutIconView(view: ReactionView, targetSize: Float) {
        // Slow, think of another way to do this
        var prevX = 0
        for (i in 1 until indexOfChild(view)) {
            prevX += getChildAt(i).width + iconDivider
        }
        val bottom = dialogY + dialogHeight - verticalPadding
        val top = (bottom - targetSize).toInt()
        val left = dialogX - mediumIconSize / 2 + prevX
        val right = (left + targetSize).toInt()
        view.layout(left, top, right, bottom)
    }

    private fun layoutRoundedView(view: RoundedView, top: Float) {
        view.layout(
                dialogX - cornerSize,
                top.toInt(),
                dialogX + dialogWidth - cornerSize,
                dialogY + dialogHeight)
    }

    fun show(parentLocation: Point, parent: View) {
        this.parentLocation = parentLocation
        parentHeight = parent.height
        visibility = View.VISIBLE
        resetChildrenToNormalSize()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val view = getIntersectedIcon(event.rawX, event.rawY)
                if (view == null) {
                    resetChildrenToNormalSize()
                } else if (view.mode !== ReactionView.Mode.LARGE) {
                    setChildToLarge(view)
                }
            }
            MotionEvent.ACTION_UP -> {
                val reaction = getIntersectedIcon(event.rawX, event.rawY)?.reaction
                val position = reaction?.let { config.reactions.indexOf(it) } ?: -1
                if (reactionSelectedListener?.invoke(reaction, position) == true) {
                    dismiss()
                } else {
                    resetChildrenToNormalSize()
                }
            }
            MotionEvent.ACTION_CANCEL -> resetChildrenToNormalSize()
        }
        return true
    }

    fun resetChildrenToNormalSize() {
        reactions.forEach { it.mode = ReactionView.Mode.MEDIUM }
        requestLayout()
    }

    fun dismiss() {
        parentLocation.set(0, 0)
        parentHeight = 0
        visibility = View.GONE
    }

    private fun getSelectedIcon(): ReactionView? =
            reactions.firstOrNull { it.mode == ReactionView.Mode.LARGE }

    private fun getIntersectedIcon(x: Float, y: Float): ReactionView? =
            reactions.firstOrNull {
                x >= it.location.x - horizontalPadding
                        && x < it.location.x + it.width + iconDivider
                        && y >= it.location.y - horizontalPadding
                        && y < it.location.y + it.height + iconDivider
            }

    private fun setChildToLarge(child: ReactionView) {
        reactions.forEach { it.mode = if (it == child) ReactionView.Mode.LARGE else ReactionView.Mode.SMALL }
        requestLayout()
    }
}
