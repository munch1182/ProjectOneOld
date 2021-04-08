package com.munch.test.lib.pre.view

import android.os.Bundle
import com.munch.test.lib.pre.R
import com.munch.test.lib.pre.base.BaseTopActivity
import com.munch.test.lib.pre.databinding.ActivityWeightBinding

/**
 * Create by munch1182 on 2021/4/8 17:32.
 */
class WeightActivity : BaseTopActivity() {

    private val bind by bind<ActivityWeightBinding>(R.layout.activity_weight)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            lifecycleOwner = this@WeightActivity
            weightAdd.setOnClickListener { weightCv.countAdd() }
            weightReduce.setOnClickListener { weightCv.countSub() }
        }
    }
}