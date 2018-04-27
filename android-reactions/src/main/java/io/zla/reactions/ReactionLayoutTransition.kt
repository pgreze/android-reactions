package io.zla.reactions

import android.animation.LayoutTransition
import android.view.View
import android.view.ViewGroup

class ReactionLayoutTransition : LayoutTransition() {

    override fun addChild(parent: ViewGroup, child: View) {
        if (child is RoundedView) {
            child.appear()
        } else {
            //TODO
        }
    }

    override fun showChild(parent: ViewGroup, child: View, oldVisibility: Int) {
        if (child is RoundedView) {
            child.appear()
        } else {
            //TODO
        }
    }

    override fun removeChild(parent: ViewGroup, child: View) {
        if (child is RoundedView) {
            child.disappear()
        } else {
            //TODO
        }
    }

    override fun hideChild(parent: ViewGroup, child: View, newVisibility: Int) {
        if (child is RoundedView) {
            child.disappear()
        } else {
            //TODO
        }
    }

    private fun RoundedView.appear() {
        animate().alpha(1.0f).translationY(this.height.toFloat())
    }

    private fun RoundedView.disappear() {
        animate().alpha(0.0f).translationY(0f)
    }
}
