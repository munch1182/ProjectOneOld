package com.munch.pre.lib.calender

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

/**
 * Create by munch1182 on 2021/5/6 16:11.
 */
internal class MonthViewPager(
    context: Context,
    private var current: Month,
    private var config: CalendarConfig
) {

    constructor(context: Context) : this(context, Month.now(), CalendarConfig())

    private val vp = ViewPager2(context)

    init {
        initMonthViewPager()
    }

    //除了初始化还应该支持跳转
    fun updateMonth(month: Month) {
        current = month
    }

    private fun initMonthViewPager() {
        vp.adapter = MonthViewPagerAdapter()
    }

    internal class MonthViewPagerViewHolder(val view: MonthView) : RecyclerView.ViewHolder(view)
    internal class MonthViewPagerAdapter : RecyclerView.Adapter<MonthViewPagerViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MonthViewPagerViewHolder {
            return MonthViewPagerViewHolder(MonthView(parent.context))
        }

        override fun onBindViewHolder(holder: MonthViewPagerViewHolder, position: Int) {
            when (position) {
                0 -> holder.view.updateMonth(Month.now().apply { month-- })
                1 -> holder.view.updateMonth(Month.now())
                2 -> holder.view.updateMonth(Month.now().apply { month++ })
                else -> throw IllegalStateException()
            }
        }

        override fun getItemCount(): Int {
            return 3
        }
    }
}