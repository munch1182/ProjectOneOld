package com.munch.test.project.one.view

import com.munch.test.project.one.base.BaseRvActivity

/**
 * Create by munch1182 on 2021/4/14 9:57.
 */
class ViewActivity : BaseRvActivity() {
    override fun getClassItem(): MutableList<ItemClassBean> {
        return ItemClassBean.newItems(
            WeightActivity::class.java,
            FlowActivity::class.java,
            BookActivity::class.java,
            FishActivity::class.java,
            PaintModeActivity::class.java,
            CalendarActivity::class.java
        )
    }
}