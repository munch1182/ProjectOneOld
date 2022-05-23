package com.munch.project.one.weight

import android.os.Bundle
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.fvClassRv
import com.munch.lib.fast.view.supportDef

/**
 * Create by munch1182 on 2022/4/25 17:54.
 */
class WeightActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val vb by fvClassRv(
        listOf(
            CalendarActivity::class
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb.init()
    }

}