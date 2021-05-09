package com.munch.pre.lib.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ComponentActivity
import androidx.core.view.contains
import com.munch.pre.lib.base.Orientation
import com.munch.pre.lib.extend.ViewHelper
import com.munch.pre.lib.extend.obOnDestroy
import kotlin.math.absoluteValue

/**
 * Create by munch1182 on 2021/5/8 15:05.
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
            addView(swipeFrameLayout, ViewHelper.newParamsMM())
        }
        activity.obOnDestroy {
            /*swipeFrameLayout.cancelAnim()*/
        }
    }

    fun getSwipeView(): SwipeFrameLayout = swipeFrameLayout

    class SwipeFrameLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : FrameLayout(context, attrs, defStyleAttr) {

        /**
         * 用于标记方向
         */
        private var flags = Orientation.LR

        /**
         * 有且只有一个子view
         */
        private val view by lazy { getChildAt(0) }
        var enable = true

        /**
         * 在移动一个方向后，在一次移动中，能够移动相反方向且超出边界
         */
        var opposite = false

        /**
         * 在两个方向上最小移动view的边长的最小比率
         * 超过这个比率的长被视为完全滑动，后续部分动画自动实现
         */
        var minRate = 0.35f
            set(value) {
                if (field == value) {
                    return
                }
                field = value
                minWidthMove = view.measuredWidth * field
                minHeightMove = view.measuredHeight * field
            }

        /**
         * 按下后在此最小时间内抬起，如果滑动了至少最短距离，则被视为快速滑动
         */
        var minTime = 300L

        /**
         * 在最小时间内必须要移动的最短距离才被视为快速滑动
         */
        var minTimeDis = 200

        private var minWidthMove = 0f
        private var minHeightMove = 0f
        private var anim: ObjectAnimator? = null
        private var downTime = 0L

        /**
         * 滑动进度回调，即使时动画完成的部分也会回调
         */
        var processListener: ((process: Float) -> Unit)? = null

        /**
         * 动画完成回调，moved表示是否移动到顶点，false表示回弹
         */
        var animEndListener: ((moved: Boolean) -> Unit)? = null

        /**
         * 用以保持单方向移动，因此此类不适应非四向移动
         */
        private var currentOrientation = -1
        var animHandle: ((ObjectAnimator) -> Unit)? = null
        private var totalMove = 0f
        private val gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

                override fun onDown(e: MotionEvent?): Boolean {
                    totalMove = 0f
                    currentOrientation = -1
                    downTime = System.currentTimeMillis()
                    return enable
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent?,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    e1 ?: return false
                    when (currentOrientation) {
                        //如果是按下后的第一此移动，则需要据此计算出此次移动的方向
                        -1 -> return setOrientationAndMove(distanceX, distanceY)
                        //根据第一次移动的方向继续移动
                        Orientation.RL, Orientation.LR -> scrollX(distanceX)
                        Orientation.TB, Orientation.BT -> scrollY(distanceY)
                        else -> return false
                    }
                    return true
                }

                private fun setOrientationAndMove(distanceX: Float, distanceY: Float): Boolean {
                    //两方向同时移动
                    return if (distanceX != 0f && distanceY != 0f) {
                        //则取最大移动距离的方向
                        return if (distanceX.absoluteValue > distanceY.absoluteValue) {
                            //水平移动，则先尝试水平移动，如果不符合设置，则换方向移动
                            setHorizontalAndMove(distanceX) ||
                                    (distanceY.absoluteValue > minTimeDis &&
                                            setVerticalAndMove(distanceY))
                        } else {
                            //垂直移动
                            setVerticalAndMove(distanceY) ||
                                    (distanceX.absoluteValue > minTimeDis &&
                                            setHorizontalAndMove(distanceX))
                        }
                    }
                    //水平单向移动
                    else if (distanceX > 0f) {
                        setHorizontalAndMove(distanceX)
                    }
                    //垂直单向移动
                    else {
                        setVerticalAndMove(distanceY)
                    }
                }

                private fun setVerticalAndMove(distanceY: Float): Boolean {
                    return if (distanceY > 0 && hasOrientation(Orientation.BT)) {
                        currentOrientation = Orientation.BT
                        scrollX(distanceY)
                        true
                    } else if (distanceY < 0 && hasOrientation(Orientation.TB)) {
                        currentOrientation = Orientation.TB
                        scrollX(distanceY)
                        true
                    } else {
                        //此次移动不再处理
                        currentOrientation = -2
                        false
                    }
                }

                private fun setHorizontalAndMove(distanceX: Float): Boolean {
                    return if (distanceX > 0 && hasOrientation(Orientation.RL)) {
                        currentOrientation = Orientation.RL
                        scrollX(distanceX)
                        true
                    } else if (distanceX < 0 && hasOrientation(Orientation.LR)) {
                        currentOrientation = Orientation.LR
                        scrollX(distanceX)
                        true
                    } else {
                        //此次移动不再处理
                        currentOrientation = -2
                        false
                    }
                }
            })

        private fun onUp(): Boolean {
            if (currentOrientation == -1) {
                return false
            }
            //是否是快速滑动
            val isFastMove =
                System.currentTimeMillis() - downTime < minTime && totalMove.absoluteValue >= minTimeDis
            if (isFastMove) {
                anim2End()
                return true
            } else {
                //有一个方向移动了最小距离
                if ((isHorizontal(currentOrientation) && totalMove.absoluteValue > minWidthMove) ||
                    (isVertical(currentOrientation) && totalMove.absoluteValue > minHeightMove)
                ) {
                    anim2End()
                    return true
                }
            }
            //如果不是快速滑动且没有移动最小距离，则弹回起点
            animMove(0f)
            return true
        }

        private fun anim2End() {
            val end = when (currentOrientation) {
                Orientation.LR, Orientation.RL -> view.width * if (totalMove < 0) 1f else -1f
                Orientation.TB, Orientation.BT -> view.height * if (totalMove < 0) 1f else -1f
                else -> return
            }
            animMove(end)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            minWidthMove = w * minRate
            minHeightMove = h * minRate
        }

        private fun animMove(dis: Float) {
            val isX = (Orientation.HORIZONTAL and currentOrientation) != 0
            anim = ObjectAnimator.ofFloat(view, if (isX) TRANSLATION_X else TRANSLATION_Y, dis)
                .apply {
                    animHandle?.invoke(this)
                    addUpdateListener {
                        val process = if (isX) {
                            view.translationX / view.width.toFloat()
                        } else {
                            view.translationY / view.height.toFloat()
                        }
                        processListener?.invoke(process)
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            val end = when {
                                dis > 0.9f -> 1f
                                dis < -0.9f -> -1f
                                else -> 0f
                            }
                            processListener?.invoke(end)
                            animEndListener?.invoke(end != 0f)
                            animation?.removeAllListeners()
                        }
                    })
                }
            anim?.start()
        }

        private fun scrollY(distanceY: Float) {
            val preMove = totalMove + distanceY
            val valid = opposite || (preMove > 0f && currentOrientation == Orientation.BT) ||
                    (preMove < 0f && currentOrientation == Orientation.TB)
            if (!valid) {
                return
            }
            totalMove = preMove
            view.translationY = -totalMove
            processListener?.invoke(totalMove.absoluteValue / height)
        }

        private fun scrollX(distanceX: Float) {
            val preMove = totalMove + distanceX
            val valid = opposite || (preMove > 0f && currentOrientation == Orientation.RL) ||
                    (preMove < 0f && currentOrientation == Orientation.LR)
            if (!valid) {
                return
            }
            totalMove = preMove
            view.translationX = -totalMove
            processListener?.invoke(totalMove.absoluteValue / width)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent?): Boolean {
            event ?: return false
            return if (event.action == MotionEvent.ACTION_UP) {
                onUp()
            } else {
                gestureDetector.onTouchEvent(event)
            }
        }

        private fun hasOrientation(orientation: @Orientation Int) =
            (flags and orientation) != 0

        /**
         * 使用位移运算来标识位置
         *
         * 使用诸如 LR or TB 来代表限定两个方向
         */
        fun orientation(orientation: @Orientation Int): SwipeFrameLayout {
            flags = orientation
            return this
        }

        private fun isHorizontal(orientation: @Orientation Int) =
            orientation == Orientation.HORIZONTAL

        private fun isVertical(orientation: @Orientation Int) =
            orientation == Orientation.VERTICAL
    }
}