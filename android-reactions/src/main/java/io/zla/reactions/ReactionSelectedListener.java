package io.zla.reactions;

import android.support.annotation.Nullable;

// Java interface is Kotlin lambda friendly
// and contrary to typealias accessible from Java
public interface ReactionSelectedListener {
    /**
     * Selected reaction callback.
     * @param reaction selected item, or null if no selection.
     * @param position selected item position, or -1.
     */
    void onReactionSelected(@Nullable Reaction reaction, int position);
}
