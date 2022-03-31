package com.munch.lib.android.recyclerview

/**
 * Create by munch1182 on 2021/8/6 10:20.
 */
interface IsAdapter {

    /**
     * 声明一个没有类型的[BaseRecyclerViewAdapter]对象
     *
     * 与类型无关的相关接口可实现此类来获取[BaseRecyclerViewAdapter]对象
     * 最后该接口和此方法由[BaseRecyclerViewAdapter]实现，则此对象即可获取
     */
    val adapter: BaseRecyclerViewAdapter<*, *>
}