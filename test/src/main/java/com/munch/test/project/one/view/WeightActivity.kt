package com.munch.test.project.one.view

import android.os.Bundle
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityWeightBinding

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
            weightTeardropAngle.setOnClickListener {
                weightTeardropAngle.setProperty { angle += 45 }
            }
            weightTeardrop.setOnClickListener {
                weightTeardrop.setProperty { this.angle += 90 }
            }
            weightSwitch.setOnCheckedChangeListener { _, isChecked ->
                weightSwitchView.isChecked = isChecked
            }
        }
    }
}