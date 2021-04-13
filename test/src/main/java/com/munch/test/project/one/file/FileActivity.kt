package com.munch.test.project.one.file

import com.munch.test.project.one.base.BaseRvActivity

/**
 * Create by munch1182 on 2021/4/13 13:36.
 */
class FileActivity : BaseRvActivity() {
    override fun getClassItem(): MutableList<ItemClassBean> {
        return ItemClassBean.newItems(FileChoseActivity::class.java, FileCopyActivity::class.java)
    }
}