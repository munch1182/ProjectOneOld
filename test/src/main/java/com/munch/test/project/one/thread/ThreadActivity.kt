package com.munch.test.project.one.thread

import com.munch.test.project.one.base.BaseRvActivity

/**
 * Create by munch1182 on 2021/4/6 11:54.
 */
class ThreadActivity : BaseRvActivity() {
    override fun getClassItem(): MutableList<ItemClassBean> {
        return ItemClassBean.newItems(
            ThreadPoolActivity::class.java,
            ThreadConcurrentActivity::class.java
        )
    }
}