package io.zla.reactions

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.Px
import android.support.v4.content.ContextCompat

/**
 * Selected reaction callback.
 * @param reaction selected item, or null if no selection.
 * @param position selected item position, or -1.
 * @return if this selection should end reaction selector.
 */
typealias ReactionSelectedListener = (reaction: Reaction?, position: Int) -> Boolean

data class Reaction(val image: Drawable)

data class ReactionsConfig(
        val reactions: Collection<Reaction>,
        @Px val reactionSize: Int,
        @Px val horizontalMargin: Int,
        @Px val verticalMargin: Int,
        @ColorInt val popupColor: Int
)

// TODO: use https://kotlinlang.org/docs/reference/type-safe-builders.html
class ReactionsConfigBuilder(private val context: Context) {
    private var _reactions: Collection<Reaction>? = null
    @Px var reactionSize: Int =
            context.resources.getDimensionPixelSize(R.dimen.reactions_item_size)
        private set
    @Px var horizontalMargin: Int =
            context.resources.getDimensionPixelSize(R.dimen.reactions_item_margin)
        private set
    @Px var verticalMargin: Int = horizontalMargin
        private set
    @ColorInt private var _popupColor: Int = Color.WHITE

    fun setReactions(drawables: Collection<Drawable>): ReactionsConfigBuilder =
            this.also { _reactions = drawables.map(::Reaction) }

    fun setReactions(res: IntArray): ReactionsConfigBuilder =
            setReactions(res.map { ContextCompat.getDrawable(context, it)!! })

    fun setReactionSize(size: Int): ReactionsConfigBuilder =
            this.also { reactionSize = size }

    fun setHorizontalReactionMargin(margin: Int): ReactionsConfigBuilder =
            this.also { horizontalMargin = margin }

    fun setVerticalReactionMargin(margin: Int): ReactionsConfigBuilder =
            this.also { verticalMargin = margin }

    fun setPopupColor(@ColorInt popupColor: Int): ReactionsConfigBuilder =
            this.also { _popupColor = popupColor }

    fun build(): ReactionsConfig =
            ReactionsConfig(
                    reactions = _reactions ?: throw NullPointerException("Empty reactions"),
                    popupColor = _popupColor,
                    reactionSize = reactionSize,
                    horizontalMargin = horizontalMargin,
                    verticalMargin = verticalMargin
            )
}
