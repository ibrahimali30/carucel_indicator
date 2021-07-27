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
    private var activeDotSize = 0
    private var inactiveDotSize = 0
    private var mediumDotSize = 0
    private var smallDotSize = 0
    private var dotMargin = 0
    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var startPosX = 0
    private var posY = 0
    private var previousPage = 0
    private var currentPage = 0
    private var translationAnim: ValueAnimator? = null
    private var dotsList: MutableList<Dot> = ArrayList()
    private var noOfPages = 0
    private var visibleDotCounts = DEFAULT_VISIBLE_DOTS_COUNT

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

    private fun setup(context: Context, attributeSet: AttributeSet?) {
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
                    DEFAULT_VISIBLE_DOTS_COUNT
                )
            )
            ta.recycle()
        }
        posY = activeDotSize / 2
        initCircles()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = (activeDotSize + dotMargin) * (dotsList.size + 1)
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

    private fun initCircles() {
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

    private fun drawCircles(canvas: Canvas) {
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

    private fun getTranslationAnimation(
        from: Int,
        to: Int,
        listener: AnimationListener?
    ): ValueAnimator? {
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

    private fun recreate() {
        initCircles()
        requestLayout()
        invalidate()
    }

    fun getVisibleDotCounts(): Int {
        return visibleDotCounts
    }

    val activeDotStartX: Int
        get() = activeDotSize + dotMargin
    private val inactiveDotStartX: Int
        private get() = inactiveDotSize + dotMargin
    private val mediumDotStartX: Int
        private get() = mediumDotSize + dotMargin
    private val smallDotStartX: Int
        private get() = smallDotSize + dotMargin
    private val activeDotRadius: Int
        private get() = activeDotSize / 2
    private val inactiveDotRadius: Int
        private get() = inactiveDotSize / 2
    private val mediumDotRadius: Int
        private get() = mediumDotSize / 2
    private val smallDotRadius: Int
        private get() = smallDotSize / 2

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

    private fun updateDots() {

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

    private fun setupNormalDots() {
        try {
            dotsList[currentPage].setCurrentState(Dot.State.ACTIVE)
            dotsList[previousPage].setCurrentState(Dot.State.INACTIVE)
            invalidate()
        } catch (e: Exception) {
            e.message
        }
    }

    private fun setupFlexibleCirclesRight(position: Int) {
        //If position exceed last two dots
        if (position >= getVisibleDotCounts() - 3) {
            if (currentPage == getNoOfPages() - 1) {
                //Last item from right
                dotsList[dotsList.size - 1].setCurrentState(Dot.State.ACTIVE)
                invalidate()
            } else if (currentPage == getNoOfPages() - 2) {
                //Second item from right
                dotsList[dotsList.size - 1].setCurrentState(Dot.State.MEDIUM)
                dotsList[dotsList.size - 2].setCurrentState(Dot.State.ACTIVE)
                invalidate()
            } else {
                removeAddRight(position)
            }
        } else {
            dotsList[position + 1].setCurrentState(Dot.State.ACTIVE)
            invalidate()
        }
    }

    private fun removeAddRight(position: Int) {
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

    private fun setupFlexibleCirclesLeft(position: Int) {
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

    private fun removeAddLeft(position: Int) {
        dotsList.removeAt(dotsList.size - 1)
        startPosX = 0
        getTranslationAnimation(startPosX, smallDotStartX, object : AnimationListener {
            override fun onAnimationEnd() {
                dotsList[dotsList.size - 1].setCurrentState(Dot.State.SMALL)
                dotsList[dotsList.size - 2].setCurrentState(Dot.State.MEDIUM)
                val newDot = Dot()
                newDot.setCurrentState(Dot.State.ACTIVE)
                dotsList.add(position, newDot)
                invalidate()
            }
        })!!.start()
    }

    companion object {
        private const val MIN_VISIBLE_DOT_COUNT = 1
        private const val DEFAULT_VISIBLE_DOTS_COUNT = MIN_VISIBLE_DOT_COUNT
    }
}
