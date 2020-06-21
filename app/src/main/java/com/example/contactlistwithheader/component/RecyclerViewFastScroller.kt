package com.example.contactlistwithheader.component

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

open class RecyclerViewFastScroller : LinearLayout {
    private var bubble: TextView? = null
    private var handle: View? = null
    private var recyclerView: RecyclerView? = null
    private var heights = 0
    private val onScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                updateBubbleAndHandlePosition()
            }
        }
    private var isInitialized = false
    private var currentAnimator: ObjectAnimator? = null

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init()
    }

    private fun init() {
        if (isInitialized) return
        isInitialized = true
        orientation = HORIZONTAL
        clipChildren = false
    }

    fun setViewsToUse(
        @LayoutRes layoutResId: Int,
        @IdRes bubbleResId: Int,
        @IdRes handleResId: Int
    ) {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(layoutResId, this, true)
        bubble = findViewById(bubbleResId)
        if (bubble != null) bubble!!.visibility = View.INVISIBLE
        handle = findViewById(handleResId)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        heights = h
        updateBubbleAndHandlePosition()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.x < handle!!.x - ViewCompat.getPaddingStart(handle!!)) return false
                if (currentAnimator != null) currentAnimator!!.cancel()
                if (bubble != null && bubble!!.visibility == View.INVISIBLE) showBubble()
                handle!!.isSelected = true
                val y = event.y
                setBubbleAndHandlePosition(y)
                setRecyclerViewPosition(y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val y = event.y
                setBubbleAndHandlePosition(y)
                setRecyclerViewPosition(y)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handle!!.isSelected = false
                hideBubble()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setRecyclerView(recyclerView: RecyclerView) {
        if (this.recyclerView !== recyclerView) {
            if (this.recyclerView != null) this.recyclerView!!.removeOnScrollListener(
                onScrollListener
            )
            this.recyclerView = recyclerView
            if (this.recyclerView == null) return
            recyclerView.addOnScrollListener(onScrollListener)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (recyclerView != null) {
            recyclerView!!.removeOnScrollListener(onScrollListener)
            recyclerView = null
        }
    }

    private fun setRecyclerViewPosition(y: Float) {
        if (recyclerView != null) {
            val itemCount = recyclerView!!.adapter!!.itemCount
            val proportion: Float =
                if (handle!!.y == 0f) 0f else if (handle!!.y + handle!!.height >= height - TRACK_SNAP_RANGE) 1f else y / height.toFloat()
            val targetPos =
                0.getValueInRange(itemCount - 1, (proportion * itemCount.toFloat()).toInt())
            (recyclerView!!.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(
                targetPos,
                0
            )
            val bubbleText =
                (recyclerView!!.adapter as BubbleTextGetter?)!!.getTextToShowInBubble(targetPos)
            if (bubble != null) {
                bubble!!.text = bubbleText
                if (TextUtils.isEmpty(bubbleText)) {
                    hideBubble()
                } else if (bubble!!.visibility == View.INVISIBLE) {
                    showBubble()
                }
            }
        }
    }

    private fun Int.getValueInRange(max: Int, value: Int): Int {
        val minimum = coerceAtLeast(value)
        return minimum.coerceAtMost(max)
    }

    private fun updateBubbleAndHandlePosition() {
        if (bubble == null || handle!!.isSelected) return
        val verticalScrollOffset = recyclerView!!.computeVerticalScrollOffset()
        val verticalScrollRange = recyclerView!!.computeVerticalScrollRange()
        val proportion =
            verticalScrollOffset.toFloat() / (verticalScrollRange.toFloat() - height)
        setBubbleAndHandlePosition(height * proportion)
    }

    private fun setBubbleAndHandlePosition(y: Float) {
        val handleHeight = handle!!.height
        handle!!.y = 0.getValueInRange(
            height - handleHeight,
            (y - handleHeight / 2).toInt()
        ).toFloat()
        if (bubble != null) {
            val bubbleHeight = bubble!!.height
            bubble!!.y = 0.getValueInRange(
                height - bubbleHeight - handleHeight / 2,
                (y - bubbleHeight).toInt()
            ).toFloat()
        }
    }

    private fun showBubble() {
        if (bubble == null) return
        bubble!!.visibility = View.VISIBLE
        if (currentAnimator != null) currentAnimator!!.cancel()

        currentAnimator = ObjectAnimator.ofFloat(bubble!!, "alpha", 0f, 1f)
            .setDuration(BUBBLE_ANIMATION_DURATION.toLong())
        currentAnimator!!.start()
    }

    private fun hideBubble() {
        if (bubble == null) return
        if (currentAnimator != null) currentAnimator!!.cancel()
        currentAnimator = ObjectAnimator.ofFloat(bubble!!, "alpha", 1f, 0f)
            .setDuration(BUBBLE_ANIMATION_DURATION.toLong())
        currentAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                bubble!!.visibility = View.INVISIBLE
                currentAnimator = null
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                bubble!!.visibility = View.INVISIBLE
                currentAnimator = null
            }
        })
        currentAnimator!!.start()
    }

    interface BubbleTextGetter {
        fun getTextToShowInBubble(pos: Int): String
    }

    companion object {
        private const val BUBBLE_ANIMATION_DURATION = 100
        private const val TRACK_SNAP_RANGE = 5
    }
}