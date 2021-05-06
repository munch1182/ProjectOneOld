package com.munch.pre.lib.calender

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import java.util.*

/**
 * Create by munch1182 on 2021/5/6 16:11.
 */
class MonthViewPager(context: Context, private var current: Calendar, attr: AttributeSet? = null) :
    ViewPager(context, attr) {

    constructor(context: Context) : this(context, Calendar.getInstance())

    init {
        initMonthViewPager()
    }

    //除了初始化还应该支持跳转
    fun updateMonth(month: Calendar) {
        current = month
    }

    private fun initMonthViewPager() {
        adapter = object : PagerAdapter() {
            override fun getCount(): Int {
                return 3
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                if (`object` is MonthView) {
                    return `object` == view
                }
                return false
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                /*return when (position) {
                    0 -> MonthView(context, Calendar.getInstance().apply {
                        set(Calendar.MONTH, current.get(Calendar.MONTH) - 1)
                    })
                    1 -> MonthView(context, current)
                    2 -> MonthView(context, Calendar.getInstance().apply {
                        set(Calendar.MONTH, current.get(Calendar.MONTH) + 1)
                    })
                    else -> throw IllegalStateException()
                }*/
                throw IllegalStateException()
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                super.destroyItem(container, position, `object`)
            }
        }
        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }
}