package com.ahmed.testindicator.libkotlin

import android.animation.Animator

abstract class AnimatorListener : Animator.AnimatorListener {
    override fun onAnimationStart(animator: Animator) {}
    override fun onAnimationRepeat(animator: Animator) {}
    override fun onAnimationCancel(animator: Animator) {}
}