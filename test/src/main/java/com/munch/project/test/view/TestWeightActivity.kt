package com.munch.project.test.view

import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.test.R

/**
 * Create by munch1182 on 2020/12/10 21:53.
 */
class TestWeightActivity : TestBaseTopActivity() {

    private val countView by lazy { findViewById<CountView>(R.id.view_count) }
    private val cb by lazy { findViewById<CheckBox>(R.id.view_cb) }
    private val rulerRes by lazy { findViewById<TextView>(R.id.view_ruler_tv) }
    private val ruler by lazy { findViewById<RulerView>(R.id.view_ruler_view) }
    private val teardrop by lazy { findViewById<TeardropView>(R.id.view_teardrop) }
    private val teardropAngle by lazy { findViewById<TeardropAngleView>(R.id.view_teardrop_angle) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity_show_weight)

        countView.setOnClickListener {
            if (!cb.isChecked) {
                countView.countAdd()
            } else {
                countView.countSub()
            }
        }
        ruler.setUpdateListener(object : RulerView.UpdateListener {
            override fun update(num: Float) {
                rulerRes.text = num.toString()
            }
        })
        teardrop.setOnClickListener {
            teardrop.setProperty {
                this.angle += 90
            }
        }
        teardropAngle.setOnClickListener {
            teardropAngle.setProperty {
                this.angle += 45
            }
        }
    }
}