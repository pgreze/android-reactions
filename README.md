# Android reactions

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Android reactions is an open source and fully customizable implementation of the
[Facebook reactions](https://en.facebookbrand.com/assets/reactions) pattern.

<p align="center">
  <img src="https://raw.githubusercontent.com/pgreze/android-reactions/master/resources/demo.gif" alt="Demo">
</p>

# Installation

```groovy
repositories {
    jcenter()
}

dependencies {
    implementation 'io.zla:android-reactions:1.0'
}
```

# Usage

See [Java](sample/src/main/java/io/zla/reactions/sample/MainActivity.java)
or [Kotlin](sample/src/main/java/io/zla/reactions/sample/KotlinSamples.kt) samples.

1. Popup creation:

Kotlin DSL:

```kotlin
val config = reactionConfig(context) {
    reactions {
        resId    { R.drawable.ic_fb_like }
        resId    { R.drawable.ic_fb_love }
        resId    { R.drawable.ic_fb_laugh }
        reaction { R.drawable.ic_fb_wow scale ImageView.ScaleType.FIT_XY }
        reaction { R.drawable.ic_fb_sad scale ImageView.ScaleType.FIT_XY }
        reaction { R.drawable.ic_fb_angry scale ImageView.ScaleType.FIT_XY }
    }
}

val popup = ReactionPopup(context, config) { position -> true.also {
    // position = -1 if no selection
} }
```

Java:

```java
ReactionsConfig config = new ReactionsConfigBuilder(context)
    .withReactions(new int[]{
            R.drawable.ic_fb_like,
            R.drawable.ic_fb_love,
            R.drawable.ic_fb_laugh,
            R.drawable.ic_fb_wow,
            R.drawable.ic_fb_sad,
            R.drawable.ic_fb_angry
    })
    .build()

ReactionPopup popup = new ReactionPopup(context, config, (position) -> {
    return true; // true is closing popup, false is requesting a new selection
});
```

2. Bind popup with a button/view:

```java
View reactionButton = findViewById(R.id.reaction_button);
reactionButton.setOnTouchListener(popup);
```

3. Additional config:

Kotlin:

```kotlin
val popup = reactionPopup(this, ::onReactionSelected) {
    // Reaction DSL
    reactions(scaleType = ImageView.ScaleType.FIT_XY) {
        resId    { R.drawable.img1 }
        drawable { Drawable(...) }
        reaction { R.drawable.img3 scale ImageView.ScaleType.FIT_CENTER }
        reaction { Drawable(...) scale ImageView.ScaleType.FIT_CENTER }
    }
    // Alternative with drawable resource id array
    reactionsIds = intArrayOf(R.drawable.img1, R.drawable.img2, R.drawable.img3)

    // Optional popup/item styles
    popupColor = Color.WHITE
    reactionSize = resources.getDimensionPixelSize(R.dimen.item_size)
    horizontalMargin = resources.getDimensionPixelSize(R.dimen.item_margin)
    verticalMargin = resources.getDimensionPixelSize(R.dimen.item_margin)

    // Text provider
    reactionTextProvider = { position -> "Item $position" }
    reactionTexts = R.array.descriptions
    // Text styles
    textBackground = ColorDrawable(Color.TRANSPARENT)
    textColor = Color.BLACK
    textHorizontalPadding = resources.getDimension(R.dimen.text_padding)
    textVerticalPadding = resources.getDimension(R.dimen.text_padding)
}
```

Java:

```java
// With resources
.withReactions(new int[]{ R.drawable.ic_fb_like, R.drawable.ic_fb_love })
// With drawables
.withReactions(Arrays.asList(drawable1, drawable2))
// item size (default: 24dp)
.withReactionSize(getResources().getDimensionPixelSize(R.dimen.item_size))
// Horizontal margin (default: 8dp)
.withHorizontalReactionMargin(margin)
// Vertical margin (default: 8dp)
.withVerticalReactionMargin(margin / 2)
// Change popup color (default: white)
.withPopupColor(Color.LTGRAY)
// Item text provider / string array (default: no texts)
.withReactionTexts(position -> descriptions[position])
.withReactionTexts(R.array.descriptions)
// Text popup background (default: #44000000 circle)
.withTextBackground(new ColorDrawable(Color.TRANSPARENT))
// Text color (default: white)
.withTextColor(Color.BLACK)
// Text horizontal margin (default: 12dp)
.withTextHorizontalPadding(0)
// Text vertical margin (default: 4dp)
.withTextVerticalPadding(0)
// Text size (default: 8sp)
.withTextSize(getResources().getDimension(R.dimen.text_size))
```

# Credits

- [Hardyk/FbReactionDemo](https://github.com/Hardyk/FbReactionDemo)
- [coinranking/cryptocurrency-icons](https://github.com/coinranking/cryptocurrency-icons)
- [LICEcap](https://www.cockos.com/licecap/)
