package com.munch.test.project.one.recyclerview

import android.os.Bundle
import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import com.munch.pre.lib.base.BaseApp
import com.munch.test.project.one.base.BaseRvActivity

/**
 * Create by munch1182 on 2021/4/9 15:24.
 */
class RecyclerViewActivity : BaseRvActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(BaseApp.getInstance())))
    }
    override fun getClassItem(): MutableList<ItemClassBean> {
        return ItemClassBean.newItems(HeaderRvActivity::class.java, GroupRvActivity::class.java)
    }
}