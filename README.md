# Facebook like reactions

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Open source implementation of the
[Facebook reactions](https://en.facebookbrand.com/assets/reactions) pattern.

# Usage

[See sample](sample/src/main/java/io/zla/reactions/sample/MainActivity.java)

Popup init:

```java
ReactionsConfigBuilder config = new ReactionsConfigBuilder(context)
    .setReactions(new int[]{
            R.drawable.ic_fb_like,
            R.drawable.ic_fb_love,
            R.drawable.ic_fb_laugh,
            R.drawable.ic_fb_wow,
            R.drawable.ic_fb_sad,
            R.drawable.ic_fb_angry
    }).build()

ReactionPopup reactionPopup = new ReactionPopup(context, config);
reactionPopup.setReactionSelectedListener((reaction, position) -> {
    Toast.makeText(MainActivity.this,
            reaction + " selected at position=" + position,
            Toast.LENGTH_SHORT)
            .show();
});
```

Bind popup with your view:

```java
Button reactionButton = findViewById(R.id.reaction_button);
reactionButton.setOnTouchListener(reactionPopup);
```

Additional config:

```java
// With resources
.setReactions(new int[]{ R.drawable.ic_fb_like, R.drawable.ic_fb_love })
// With drawables
.setReactions(Arrays.asList(drawable1, drawable2))
// item size (default: 24dp)
.setReactionSize(getResources().getDimensionPixelSize(R.dimen.crypto_item_size))
// Horizontal margin (default: 8dp)
.setHorizontalReactionMargin(cryptoMargin)
// Vertical margin (default: 8dp) 
.setVerticalReactionMargin(cryptoMargin / 2)
// Change popup color (default: white)
.setPopupColor(Color.LTGRAY)
```

# Thanks

- [Hardyk/FbReactionDemo](https://github.com/Hardyk/FbReactionDemo)
- [coinranking/cryptocurrency-icons](https://github.com/coinranking/cryptocurrency-icons)
