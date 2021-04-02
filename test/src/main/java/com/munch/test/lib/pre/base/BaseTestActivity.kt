package com.munch.test.lib.pre.base

import com.munch.lib.fast.base.activity.BaseItemActivity

/**
 * Create by munch1182 on 2021/4/2 10:30.
 */
abstract class BaseTestActivity : BaseItemActivity() {

    override fun clickItem(pos: Int) {
        when (pos) {
            0 -> testFun0()
            1 -> testFun1()
            2 -> testFun2()
            3 -> testFun3()
            4 -> testFun4()
            5 -> testFun5()
            6 -> testFun6()
            else -> {
            }
        }
    }

    abstract fun testFun0()
    protected open fun testFun1() {}
    protected open fun testFun2() {}
    protected open fun testFun3() {}
    protected open fun testFun4() {}
    protected open fun testFun5() {}
    protected open fun testFun6() {}

    override fun getItem() =
        mutableListOf("test0", "test1", "test2", "test3", "test4", "test5", "test6")
}