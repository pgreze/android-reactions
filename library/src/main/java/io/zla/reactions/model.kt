package io.zla.reactions

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.annotation.ArrayRes
import android.support.annotation.ColorInt
import android.support.annotation.Px
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import kotlin.math.roundToInt

/**
 * Selected reaction callback.
 * @param reaction selected item, or null if no selection.
 * @param position selected item position, or -1.
 * @return if this selection should end reaction selector.
 */
typealias ReactionSelectedListener = (reaction: Reaction?, position: Int) -> Boolean

/**
 * Reaction text provider.
 * @param position position of current selected item in [ReactionsConfig.reactions]
 * @return optional reaction text, null for no text
 */
typealias ReactionTextProvider = (position: Int) -> CharSequence?

data class Reaction(
        val image: Drawable,
        val scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER
)

data class ReactionsConfig(
        val reactions: Collection<Reaction>,
        @Px val reactionSize: Int,
        @Px val horizontalMargin: Int,
        @Px val verticalMargin: Int,
        @ColorInt val popupColor: Int,
        val reactionTextProvider: ReactionTextProvider?,
        val textBackground: Drawable,
        @ColorInt val textColor: Int,
        val textHorizontalPadding: Int,
        val textVerticalPadding: Int,
        val textSize: Float
)

// TODO: use https://kotlinlang.org/docs/reference/type-safe-builders.html
class ReactionsConfigBuilder(private val context: Context) {
    private var reactions: Collection<Reaction>? = null
    @Px var reactionSize: Int =
            context.resources.getDimensionPixelSize(R.dimen.reactions_item_size)
        private set
    @Px var horizontalMargin: Int =
            context.resources.getDimensionPixelSize(R.dimen.reactions_item_margin)
        private set
    @Px var verticalMargin: Int = horizontalMargin
        private set
    @ColorInt private var popupColor: Int? = null
    private var reactionTextProvider: ReactionTextProvider? = null
    private var textBackground: Drawable? = null
    @ColorInt private var textColor: Int? = null
    private var textHorizontalPadding: Int? = null
    private var textVerticalPadding: Int? = null
    private var textSize: Float? = null

    fun setReactions(reactions: Collection<Reaction>): ReactionsConfigBuilder =
            this.also { this.reactions = reactions }

    @JvmOverloads
    fun setReactions(
            res: IntArray,
            scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER
    ): ReactionsConfigBuilder =
            setReactions(res.map { Reaction(ContextCompat.getDrawable(context, it)!!, scaleType) })

    fun setReactionTexts(reactionTextProvider: ReactionTextProvider?): ReactionsConfigBuilder =
            this.also { this.reactionTextProvider = reactionTextProvider }

    fun setReactionTexts(@ArrayRes res: Int): ReactionsConfigBuilder =
            this.also { reactionTextProvider = context.resources.getStringArray(res)::get }

    fun setReactionSize(size: Int): ReactionsConfigBuilder =
            this.also { reactionSize = size }

    fun setHorizontalReactionMargin(margin: Int): ReactionsConfigBuilder =
            this.also { horizontalMargin = margin }

    fun setVerticalReactionMargin(margin: Int): ReactionsConfigBuilder =
            this.also { verticalMargin = margin }

    fun setPopupColor(@ColorInt popupColor: Int): ReactionsConfigBuilder =
            this.also { this.popupColor = popupColor }

    fun setTextBackground(textBackground: Drawable): ReactionsConfigBuilder =
            this.also { this.textBackground = textBackground }

    fun setTextColor(@ColorInt textColor: Int): ReactionsConfigBuilder =
            this.also { this.textColor = textColor }

    fun setTextHorizontalPadding(textHorizontalPadding: Int): ReactionsConfigBuilder =
            this.also { this.textHorizontalPadding = textHorizontalPadding }

    fun setTextVerticalPadding(textVerticalPadding: Int): ReactionsConfigBuilder =
            this.also { this.textVerticalPadding = textVerticalPadding }

    fun setTextSize(textSize: Float): ReactionsConfigBuilder =
            this.also { this.textSize = textSize }

    fun build(): ReactionsConfig =
            ReactionsConfig(
                    reactions = reactions ?: throw NullPointerException("Empty reactions"),
                    popupColor = popupColor ?: Color.WHITE,
                    reactionSize = reactionSize,
                    horizontalMargin = horizontalMargin,
                    verticalMargin = verticalMargin,
                    reactionTextProvider = reactionTextProvider,
                    textBackground = textBackground
                            ?: ContextCompat.getDrawable(context, R.drawable.reactions_text_background)!!,
                    textColor = textColor ?: Color.WHITE,
                    textHorizontalPadding = textHorizontalPadding
                            ?: context.resources.getDimension(R.dimen.reactions_text_horizontal_padding).roundToInt(),
                    textVerticalPadding = textVerticalPadding
                            ?: context.resources.getDimension(R.dimen.reactions_text_vertical_padding).roundToInt(),
                    textSize = textSize
                            ?: context.resources.getDimension(R.dimen.reactions_text_size)
            )
}
