package com.munch.project.one

import android.os.Bundle
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.fastview.fvFvBtn
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef

/**
 * Create by munch on 2022/9/18 4:24.
 */
class TestActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    private fun click0() {

    }

    //<editor-fold desc="1">
    private fun click1() {}

    //</editor-fold>
    //<editor-fold desc="2">
    private fun click2() {}

    //</editor-fold>
    //<editor-fold desc="3">
    private fun click3() {}

    //<editor-fold desc="view">
    private val bind by fvFvBtn("test0", "test1", "", "test2", "test3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.click {
            when (it) {
                0 -> click0()
                1 -> click1()
                2 -> click2()
                3 -> click3()
            }
        }
    }
    //</editor-fold>

}