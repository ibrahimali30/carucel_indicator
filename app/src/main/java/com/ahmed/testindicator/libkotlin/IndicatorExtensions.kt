package com.ahmed.testindicator.libkotlin

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import com.ahmed.testindicator.R


fun InstaDotView.setup(context: Context, attributeSet: AttributeSet?) {
    val resources = resources
    if (attributeSet != null) {
        val ta = context.obtainStyledAttributes(attributeSet, R.styleable.InstaDotView)
        activePaint.style = Paint.Style.FILL
        activePaint.color = ta.getColor(
            R.styleable.InstaDotView_dot_activeColor,
            resources.getColor(R.color.active)
        )
        inactivePaint.style = Paint.Style.FILL
        inactivePaint.color = ta.getColor(
            R.styleable.InstaDotView_dot_inactiveColor,
            resources.getColor(R.color.inactive)
        )
        activeDotSize = ta.getDimensionPixelSize(
            R.styleable.InstaDotView_dot_activeSize,
            resources.getDimensionPixelSize(R.dimen.dot_active_size)
        )
        inactiveDotSize = ta.getDimensionPixelSize(
            R.styleable.InstaDotView_dot_inactiveSize,
            resources.getDimensionPixelSize(R.dimen.dot_inactive_size)
        )
        mediumDotSize = ta.getDimensionPixelSize(
            R.styleable.InstaDotView_dot_mediumSize,
            resources.getDimensionPixelSize(R.dimen.dot_medium_size)
        )
        smallDotSize = ta.getDimensionPixelSize(
            R.styleable.InstaDotView_dot_smallSize,
            resources.getDimensionPixelSize(R.dimen.dot_small_size)
        )
        dotMargin = ta.getDimensionPixelSize(
            R.styleable.InstaDotView_dot_margin,
            resources.getDimensionPixelSize(R.dimen.dot_margin)
        )
        setVisibleDotCounts(
            ta.getInteger(
                R.styleable.InstaDotView_dots_visible,
                InstaDotView.DEFAULT_VISIBLE_DOTS_COUNT
            )
        )
        ta.recycle()
    }
    posY = activeDotSize / 2
    initCircles()
}
