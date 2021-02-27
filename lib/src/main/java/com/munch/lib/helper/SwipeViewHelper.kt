package com.munch.lib.helper

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ComponentActivity
import androidx.core.view.contains
import com.munch.lib.base.Orientation
import com.munch.lib.base.removeAllWhenEnd
import com.munch.lib.reset
import kotlin.math.absoluteValue

/**
 *
 * Create by munch1182 on 2021/2/27 9:21.
 */
class SwipeViewHelper(private val activity: ComponentActivity) {

    private val swipeFrameLayout by lazy { SwipeFrameLayout(activity) }

    fun setActivity() {
        val contentView = activity.findViewById<View>(android.R.id.content)
        (contentView.parent as ViewGroup).apply {
            if (contains(swipeFrameLayout)) {
                return@apply
            }
            removeView(contentView)
            swipeFrameLayout.addView(contentView)
            addView(
                swipeFrameLayout,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
        activity.obWhenDestroy {
            swipeFrameLayout.cancelAnim()
        }
    }

    fun getSwipeView(): SwipeFrameLayout = swipeFrameLayout

    /**
     *因为暂时没有其余使用场景，所有没有提出来单独成类
     */
    class SwipeFrameLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : FrameLayout(context, attrs, defStyleAttr) {

        /**
         * 必须要有一个子view
         */
        private val view by lazy { getChildAt(0) }
        private val touchSlop = ViewConfiguration.get(getContext()).scaledTouchSlop
        private var min = touchSlop * 3f
        private var flags = Orientation.LEFT_2_RIGHT
        private var enable = true
        private val downPoint = PointF()
        private val movePoint = PointF()
        private var minRate = 0.35f
        private var animHandle: ((ObjectAnimator) -> Unit)? = null
        private var processCallBack: ((startPoint: PointF, endPoint: PointF, process: Float) -> Unit)? =
            null
        private var isMoveX = false
        private var isMoveY = false
        private var anim: ObjectAnimator? = null

        fun enable(enable: Boolean): SwipeFrameLayout {
            this.enable = enable
            return this
        }

        /**
         * 使用位移运算来标识位置
         *
         * 使用诸如 LEFT_2_RIGHT or TOP_2_BOTTOM 来代表限定两个方向
         */
        fun orientation(orientation: @Orientation Int): SwipeFrameLayout {
            flags = orientation
            return this
        }

        fun clearOrientation(orientation: @Orientation Int): SwipeFrameLayout {
            flags = flags and orientation.inv()
            return this
        }

        fun animHandle(animHandle: (ObjectAnimator) -> Unit): SwipeFrameLayout {
            this.animHandle = animHandle
            return this
        }

        fun process(process: (startPoint: PointF, endPoint: PointF, process: Float) -> Unit): SwipeFrameLayout {
            this.processCallBack = process
            return this
        }

        override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
            ev ?: return super.onInterceptTouchEvent(ev)
            if (!enable) {
                return super.onInterceptTouchEvent(ev)
            }
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    downPoint.set(ev.x, ev.y)
                }
                MotionEvent.ACTION_MOVE -> {
                    val offsetX = ev.x - downPoint.x
                    val offsetXAbs = offsetX.absoluteValue
                    val offsetY = ev.y - downPoint.y
                    val offsetYAbs = offsetY.absoluteValue
                    //视为有效的x轴移动
                    if (offsetXAbs > offsetYAbs && offsetXAbs >= min) {
                        isMoveX = true
                        return (offsetX > 0 && hasOrientation(Orientation.LEFT_2_RIGHT))
                                || (offsetX < 0 && hasOrientation(Orientation.RIGHT_2_LEFT))
                        //视为有效的y轴移动
                    } else if (offsetYAbs > offsetXAbs && offsetYAbs >= min) {
                        isMoveY = true
                        return (offsetY > 0 && hasOrientation(Orientation.TOP_2_BOTTOM))
                                || (offsetY < 0 && hasOrientation(Orientation.BOTTOM_2_TOP))
                    }
                }
                MotionEvent.ACTION_UP -> {
                }
            }
            isMoveX = false
            isMoveY = false
            return super.onInterceptTouchEvent(ev)
        }

        private fun hasOrientation(orientation: @Orientation Int) = (flags and orientation) != 0

        override fun performClick(): Boolean {
            return super.performClick()
        }

        override fun onTouchEvent(ev: MotionEvent?): Boolean {
            ev ?: return super.onTouchEvent(ev)
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                }
                MotionEvent.ACTION_MOVE -> {
                    movePoint.set(ev.x, ev.y)
                    if (isMoveX) {
                        view.translationX = ev.x - downPoint.x
                        processCallBack?.invoke(
                            downPoint,
                            movePoint,
                            view.translationX / view.measuredWidth
                        )
                    } else if (isMoveY) {
                        view.translationY = ev.y - downPoint.y
                        processCallBack?.invoke(
                            downPoint,
                            movePoint,
                            view.translationY / view.measuredHeight
                        )
                    }
                }
                MotionEvent.ACTION_UP -> {
                    val offsetX = ev.x - downPoint.x
                    val offsetXAbs = offsetX.absoluteValue
                    val offsetY = ev.y - downPoint.y
                    val offsetYAbs = offsetY.absoluteValue

                    if (offsetXAbs < min && offsetYAbs < min) {
                        performClick()
                    } else {
                        if (offsetXAbs >= offsetYAbs) {
                            val width = view.measuredWidth
                            if (offsetXAbs < width * minRate) {
                                animMove2End(true, 0f)
                            } else {
                                animMove2End(true, width.toFloat() * (if (offsetX > 0) 1 else -1))
                            }
                        } else if (offsetYAbs >= offsetXAbs) {
                            val height = view.measuredHeight
                            if (offsetYAbs < height * minRate) {
                                animMove2End(false, 0f)
                            } else {
                                animMove2End(false, height.toFloat() * if (offsetY > 0) 1 else -1)
                            }
                        }
                    }
                    downPoint.reset()
                    movePoint.reset()
                }
            }
            return super.onTouchEvent(ev)
        }

        private fun animMove2End(isX: Boolean, dis: Float) {
            anim = ObjectAnimator.ofFloat(view, if (isX) TRANSLATION_X else TRANSLATION_Y, dis)
                .apply {
                    animHandle?.invoke(this)
                    addUpdateListener {
                        processCallBack?.invoke(
                            downPoint, movePoint,
                            if (isX) view.translationX / view.measuredWidth else view.translationY / view.measuredHeight
                        )
                    }
                    removeAllWhenEnd()
                }
            anim?.start()
        }

        fun cancelAnim() {
            anim?.cancel()
        }
    }
}