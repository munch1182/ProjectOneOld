package com.munch.pre.lib.calender

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.munch.pre.lib.extend.ViewHelper
import com.munch.pre.lib.log.log

/**
 * Create by munch1182 on 2021/5/6 16:11.
 */
internal class MonthViewPager(
    context: Context,
    private var current: Month,
    private var config: CalendarConfig?
) {

    val vp by lazy { ViewPager2(context) }

    init {
        initMonthViewPager()
    }

    //除了初始化还应该支持跳转
    fun updateMonth(month: Month, config: CalendarConfig) {
        current = month
        this.config = config
        initMonthViewPager()
    }


    private fun initMonthViewPager() {
        if (config != null) {
            vp.adapter = MonthViewPagerAdapter()
            vp.setCurrentItem(1, false)
            vp.offscreenPageLimit = 3
            vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        val position = vp.currentItem
                        if (position == 1) {
                            return
                        }
                        //1->0
                        if (position < 1) {
                            current--
                            //2->1
                        } else {
                            current++
                        }
                        vp.setCurrentItem(1, false)
                    }
                }
            })
        }
    }

    internal class MonthViewPagerViewHolder(val view: MonthView) : RecyclerView.ViewHolder(view)
    inner class MonthViewPagerAdapter : RecyclerView.Adapter<MonthViewPagerViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MonthViewPagerViewHolder {
            return MonthViewPagerViewHolder(MonthView(parent.context, current, config).apply {
                layoutParams = ViewHelper.newParamsMM()
            })
        }

        override fun onBindViewHolder(holder: MonthViewPagerViewHolder, position: Int) {
            log("onBindViewHolder", holder.view)
            when (position) {
                0 -> holder.view.updateMonth(current.beMonth() - 1)
                1 -> holder.view.updateMonth(current)
                2 -> holder.view.updateMonth(current.beMonth() + 1)
                else -> throw IllegalStateException()
            }
        }

        override fun getItemCount(): Int {
            return 3
        }
    }
}