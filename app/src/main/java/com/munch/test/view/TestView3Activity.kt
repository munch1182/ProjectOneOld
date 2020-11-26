package com.munch.test.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.munch.test.R
import com.munch.test.base.BaseActivity
import com.munch.test.view.helper.RippleHelper
import kotlinx.android.synthetic.main.activity_test_view3.*


/**
 * Create by Munch on 2020/09/04
 */
class TestView3Activity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_view3)
        setToolBar(view_tb, "View")


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            RippleHelper.setRipple(view_lv1, ColorDrawable(Color.LTGRAY))
            RippleHelper.setRipple(view_lv2, ColorDrawable(Color.GREEN))
            RippleHelper.setRipple(view_lv3, ColorDrawable(Color.CYAN))
        }

        view_lv1.setOnClickListener { }
        view_lv2.setOnClickListener { }
        view_lv3.setOnClickListener { }
        view_lv4.setOnClickListener { }
        /*view_lv1.setOnTouchListener(TouchListener())
        view_lv2.setOnTouchListener(TouchListener())
        view_lv3.setOnTouchListener(TouchListener())
        view_lv4.setOnTouchListener(TouchListener())*/
        sw_lv1.setOnCheckedChangeListener(CheckListener())
        sw_lv2.setOnCheckedChangeListener(CheckListener())
        sw_lv3.setOnCheckedChangeListener(CheckListener())
    }

    private class CheckListener : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            buttonView?: return
            (buttonView.parent as ViewGroup).isClickable = isChecked
        }
    }

    private class TouchListener : View.OnTouchListener {
        private var lastX = 0f
        private var lastY = 0f
        private var isClick = true

        override fun onTouch(v: View, event: MotionEvent?): Boolean {
            event ?: return false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val inView = isInView(v, event)
                    return if (inView) {
                        lastX = event.x
                        lastY = event.y
                        true
                    } else {
                        false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        v.performClick()
                    }
                    return false
                }
                MotionEvent.ACTION_MOVE -> {
                    isClick = false
                    return true
                }
            }
            return false
        }

        private fun isInView(view: View, event: MotionEvent)
                : Boolean {
            val intArray = IntArray(2)
            view.getLocationInWindow(intArray)
            return intArray[0] <= event.rawX && event.rawX <= intArray[0] + view.width
                    && intArray[1] <= event.rawY && event.rawY <= intArray[1] + view.height
        }
    }
}