package com.ahmed.testindicator.libkotlin

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.RequiresApi
import com.ahmed.testindicator.R
import java.util.*


class InstaDotView : View {
    var activeDotSize = 0
    var inactiveDotSize = 0
    var mediumDotSize = 0
    var smallDotSize = 0
    var dotMargin = 0
    val activePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var startPosX = 0
    var posY = 0
    var previousPage = 0
    var currentPage = 0
    var translationAnim: ValueAnimator? = null
    var dotsList: MutableList<Dot> = ArrayList()
    private var noOfPages = 0
    private var visibleDotCounts = 5

    constructor(context: Context) : super(context) {
        setup(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setup(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setup(context, attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        setup(context, attrs)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = (activeDotSize + dotMargin) * (size + 1)
        val desiredHeight = activeDotSize
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int
        val height: Int
        width =
            if (widthMode == MeasureSpec.EXACTLY) widthSize else if (widthMode == MeasureSpec.AT_MOST) Math.min(
                desiredWidth,
                widthSize
            ) else desiredWidth
        height =
            if (heightMode == MeasureSpec.EXACTLY) heightSize else if (heightMode == MeasureSpec.AT_MOST) Math.min(
                desiredHeight,
                heightSize
            ) else desiredHeight
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCircles(canvas)
    }

    fun initCircles() {
        val viewCount = Math.min(getNoOfPages(), getVisibleDotCounts())
        if (viewCount < 1) return
        startPosX = if (noOfPages > visibleDotCounts) smallDotStartX else 0
        dotsList = ArrayList(viewCount)
        for (i in 0 until viewCount) {
            val dot = Dot()
            var state: Dot.State
            state = if (noOfPages > visibleDotCounts) {
                if (i == getVisibleDotCounts() - 1) Dot.State.SMALL else if (i == getVisibleDotCounts() - 2) Dot.State.MEDIUM else if (i == 0) Dot.State.ACTIVE else Dot.State.INACTIVE
            } else {
                if (i == 0) Dot.State.ACTIVE else Dot.State.INACTIVE
            }
            dot.setCurrentState(state)
            dotsList.add(dot)
        }
        invalidate()
    }

    fun drawCircles(canvas: Canvas) {
        var posX = startPosX
        for (i in dotsList.indices) {
            val d = dotsList[i]
            var paint = inactivePaint
            var radius: Int
            when (d.getCurrentState()) {
                Dot.State.ACTIVE -> {
                    paint = activePaint
                    radius = activeDotRadius
                    posX += activeDotStartX
                }
                Dot.State.INACTIVE -> {
                    radius = inactiveDotRadius
                    posX += inactiveDotStartX
                }
                Dot.State.MEDIUM -> {
                    radius = mediumDotRadius
                    posX += mediumDotStartX
                }
                Dot.State.SMALL -> {
                    radius = smallDotRadius
                    posX += smallDotStartX
                }
                else -> {
                    radius = 0
                    posX = 0
                }
            }
            canvas.drawCircle(posX.toFloat(), posY.toFloat(), radius.toFloat(), paint)
        }
    }

    fun getTranslationAnimation(from: Int, to: Int, listener: AnimationListener?): ValueAnimator? {
        if (translationAnim != null) translationAnim!!.cancel()
        translationAnim = ValueAnimator.ofInt(from, to)
        translationAnim?.setDuration(120)
        translationAnim?.setInterpolator(AccelerateDecelerateInterpolator())
        translationAnim?.addUpdateListener(AnimatorUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            if (startPosX != `val`) {
                startPosX = `val`
                invalidate()
            }
        })
        translationAnim?.addListener(object : AnimatorListener() {
            override fun onAnimationEnd(animator: Animator?) {
                listener?.onAnimationEnd()
            }
        })
        return translationAnim
    }

    fun setNoOfPages(noOfPages: Int) {
        //Hide if noOfPages is 0 or 1
        visibility = if (noOfPages <= 1) GONE else VISIBLE
        this.noOfPages = noOfPages
        recreate()
    }

    fun getNoOfPages(): Int {
        return noOfPages
    }

    fun setVisibleDotCounts(visibleDotCounts: Int) {
        if (visibleDotCounts < MIN_VISIBLE_DOT_COUNT) throw RuntimeException("Visible Dot count cannot be smaller than " + MIN_VISIBLE_DOT_COUNT)
        this.visibleDotCounts = visibleDotCounts
        recreate()
    }

    fun recreate() {
        initCircles()
        requestLayout()
        invalidate()
    }

    fun getVisibleDotCounts(): Int {
        return visibleDotCounts
    }

    val activeDotStartX: Int
        get() = activeDotSize + dotMargin
    val inactiveDotStartX: Int
        get() = inactiveDotSize + dotMargin
    val mediumDotStartX: Int
        get() = mediumDotSize + dotMargin
    val smallDotStartX: Int
        get() = smallDotSize + dotMargin
    val activeDotRadius: Int
        get() = activeDotSize / 2
    val inactiveDotRadius: Int
        get() = inactiveDotSize / 2
    val mediumDotRadius: Int
        get() = mediumDotSize / 2
    val smallDotRadius: Int
        get() = smallDotSize / 2

    fun onPageChange(page: Int) {
        currentPage = page
        if (page != previousPage && page >= 0 && page <= getNoOfPages() - 1) {
            updateDots()
            previousPage = currentPage
        }
    }

    fun onPageChange() {
        val page = currentPage + 1
        currentPage = page
        if (page != previousPage && page >= 0 && page <= getNoOfPages() - 1) {
            updateDots()
            previousPage = currentPage
        }
    }

    fun updateDots() {

        //If pages does not exceed DOT COUNT limit
        if (noOfPages <= visibleDotCounts) {
            setupNormalDots()
            return
        }

        //If page exceed DOT COUNT limit - 2 last dots
        for (i in dotsList.indices) {
            val currentDot = dotsList[i]
            //Active dot
            if (currentDot.getCurrentState().equals(Dot.State.ACTIVE)) {
                //Set current active dot to passive
                currentDot.setCurrentState(Dot.State.INACTIVE)
                //Left to right
                if (currentPage > previousPage) {
                    setupFlexibleCirclesRight(i)
                } else {
                    //Right to left
                    setupFlexibleCirclesLeft(i)
                }
                return
            }
        }
    }

    fun setupNormalDots() {
        try {
            dotsList[currentPage].setCurrentState(Dot.State.ACTIVE)
            dotsList[previousPage].setCurrentState(Dot.State.INACTIVE)
            invalidate()
        } catch (e: Exception) {
            e.message
        }
    }
    
    var size = 0
    get() = dotsList.size

    fun setupFlexibleCirclesRight(position: Int) {
        //If position exceed last two dots
        if (position >= getVisibleDotCounts() - 3) {
            if (currentPage == getNoOfPages() - 1) {
                //Last item from right
                dotsList[size - 1].setCurrentState(Dot.State.ACTIVE)
                invalidate()
            } else if (currentPage == getNoOfPages() - 2) {
                //Second item from right
                dotsList[size - 1].setCurrentState(Dot.State.MEDIUM)
                dotsList[size - 2].setCurrentState(Dot.State.ACTIVE)
                invalidate()
            } else {
                removeAddRight(position)
            }
        } else {
            dotsList[position + 1].setCurrentState(Dot.State.ACTIVE)
            invalidate()
        }
    }

    fun removeAddRight(position: Int) {
        dotsList.removeAt(0)
        startPosX = startPosX + smallDotStartX
        getTranslationAnimation(startPosX, smallDotStartX, object : AnimationListener {
            override fun onAnimationEnd() {
                dotsList[0].setCurrentState(Dot.State.SMALL)
                dotsList[1].setCurrentState(Dot.State.MEDIUM)
                val newDot = Dot()
                newDot.setCurrentState(Dot.State.ACTIVE)
                dotsList.add(position, newDot)
                invalidate()
            }
        })!!.start()
    }

    fun setupFlexibleCirclesLeft(position: Int) {
        //If position exceed first two dots
        if (position <= 2) {
            if (currentPage == 0) {
                //First item from left
                dotsList[0].setCurrentState(Dot.State.ACTIVE)
                invalidate()
            } else if (currentPage == 1) {
                //Second item from left
                dotsList[0].setCurrentState(Dot.State.MEDIUM)
                dotsList[1].setCurrentState(Dot.State.ACTIVE)
                invalidate()
            } else {
                removeAddLeft(position)
            }
        } else {
            dotsList[position - 1].setCurrentState(Dot.State.ACTIVE)
            invalidate()
        }
    }

    fun removeAddLeft(position: Int) {
        dotsList.removeAt(size - 1)
        startPosX = 0
        getTranslationAnimation(startPosX, smallDotStartX, object : AnimationListener {
            override fun onAnimationEnd() {
                dotsList[size - 1].setCurrentState(Dot.State.SMALL)
                dotsList[size - 2].setCurrentState(Dot.State.MEDIUM)
                val newDot = Dot()
                newDot.setCurrentState(Dot.State.ACTIVE)
                dotsList.add(position, newDot)
                invalidate()
            }
        })!!.start()
    }

    companion object {
        const val MIN_VISIBLE_DOT_COUNT = 1
        const val DEFAULT_VISIBLE_DOTS_COUNT = MIN_VISIBLE_DOT_COUNT
    }
}
