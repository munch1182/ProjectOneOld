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

        /**
         * 用于标记方向
         */
        private var flags = Orientation.LR
        private var enable = true
        private val downPoint = PointF()
        private var downTime: Long = 0L
        private val movePoint = PointF()

        /**
         * 在两个方向上最小移动view的边长的最小比率
         * 超过这个比率的长被视为完全滑动，后续部分动画自动实现
         */
        private val minRate = 0.35f

        /**
         * 按下后在此最小时间内抬起，如果滑动了至少最短距离，则被视为快速滑动
         */
        private val minTime = 300L

        /**
         * 在最小时间内必须要移动的最短距离才被视为快速滑动
         */
        private val minTimeDis = 200
        private var animHandle: ((ObjectAnimator) -> Unit)? = null

        /**
         * 滑动进度回调，即使时动画完成的部分也会回调，但完成动画的最后一小部分则不会回调，即process可能不会到达0或1
         */
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
         * 使用诸如 LR or TB 来代表限定两个方向
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
                    isMoveX = false
                    isMoveY = false
                    downTime = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> {
                    val offsetX = ev.x - downPoint.x
                    val offsetXAbs = offsetX.absoluteValue
                    val offsetY = ev.y - downPoint.y
                    val offsetYAbs = offsetY.absoluteValue
                    //视为有效的x轴移动
                    if (offsetXAbs > offsetYAbs && offsetXAbs >= min) {
                        isMoveX = (offsetX > 0 && hasOrientation(Orientation.LR))
                                || (offsetX < 0 && hasOrientation(Orientation.RL))
                        return isMoveX
                        //视为有效的y轴移动
                    } else if (offsetYAbs > offsetXAbs && offsetYAbs >= min) {
                        isMoveY = (offsetY > 0 && hasOrientation(Orientation.TB))
                                || (offsetY < 0 && hasOrientation(Orientation.BT))
                        return isMoveY
                    }
                }
                MotionEvent.ACTION_UP -> {
                }
            }
            return super.onInterceptTouchEvent(ev)
        }

        private fun hasOrientation(orientation: @Orientation Int) = (flags and orientation) != 0

        override fun performClick(): Boolean {
            return super.performClick()
        }

        override fun onTouchEvent(ev: MotionEvent?): Boolean {
            ev ?: return super.onTouchEvent(ev)
            when (ev.action) {
                MotionEvent.ACTION_MOVE -> {
                    movePoint.set(ev.x, ev.y)
                    if (isMoveX) {
                        view.translationX = ev.x - downPoint.x
                        processCallBack?.invoke(
                            downPoint, movePoint, view.translationX / view.measuredWidth
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
                        //快速滑动直接关闭
                    } else if (System.currentTimeMillis() - downTime < minTime
                        && ((isMoveX && offsetXAbs > minTimeDis) || (isMoveY && offsetYAbs > minTimeDis))
                    ) {
                        if (isMoveX) {
                            val width = view.measuredWidth
                            animMove2End(true, width.toFloat() * (if (offsetX > 0) 1 else -1))
                        } else if (isMoveY) {
                            val height = view.measuredHeight
                            animMove2End(false, height.toFloat() * if (offsetY > 0) 1 else -1)
                        }
                    } else {
                        if (isMoveX) {
                            val width = view.measuredWidth
                            if (offsetXAbs < width * minRate) {
                                animMove2End(true, 0f)
                            } else {
                                animMove2End(true, width.toFloat() * (if (offsetX > 0) 1 else -1))
                            }
                        } else if (isMoveY) {
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
                    isMoveX = false
                    isMoveY = false
                    downTime = 0L
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