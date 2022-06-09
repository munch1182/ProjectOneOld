package com.munch.project.one.weight

import android.os.Bundle
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.supportDef
import com.munch.lib.weight.gesture.GestureView

/**
 * Created by munch1182 on 2022/6/11 3:45.
 */
class GestureActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = GestureView(this)
        setContentView(view)
    }

}