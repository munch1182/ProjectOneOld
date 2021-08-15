package com.munch.project.one.applib.weight

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import com.google.android.material.button.MaterialButton
import com.munch.lib.base.OnViewIntClickListener
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.weight.Gravity
import com.munch.lib.weight.debug.DebugFlowLayout
import com.munch.project.one.applib.R
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/8/11 14:13.
 */
class FlowLayoutActivity : BaseBigTextTitleActivity() {

    private val flowLayout by lazy { findViewById<DebugFlowLayout>(R.id.flow_view) }
    private val gravity by lazy { findViewById<TextView>(R.id.flow_gravity_view) }
    private val maxCount by lazy { findViewById<TextView>(R.id.flow_max_count_view) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow_layout)
        addContent()
        gravity.setOnClickListener { changeGravity() }
        gravity.tag = Gravity.all.indexOf(flowLayout.gravityFlags)
        maxCount.setOnClickListener(object : OnViewIntClickListener {
            override fun onClick(v: View?, intVal: Int) {
                super.onClick(v, intVal)
                var count = intVal + 1
                if (count == 0) {
                    count = 1
                } else if (count > 6) {
                    count = -1
                }
                flowLayout.set { maxCountInLine = count }
                maxCount.text = String.format("%s%s", "Max Count: ", count)
                maxCount.tag = count
            }
        })
        maxCount.tag = -1
    }

    private fun changeGravity() {
        var index = (gravity.tag as? Int) ?: return
        index++
        if (index > Gravity.all.size || index < 0) {
            index = 0
        }
        val type = Gravity.all[index]
        var typeStr = ""
        when {
            Gravity.hasFlag(type, Gravity.CENTER_HORIZONTAL) -> typeStr = "CENTER_HORIZONTAL"
            Gravity.hasFlag(type, Gravity.START) -> typeStr = "START"
            Gravity.hasFlag(type, Gravity.END) -> typeStr = "END"
        }
        when {
            Gravity.hasFlag(type, Gravity.CENTER_VERTICAL) -> typeStr = "$typeStr | CENTER_VERTICAL"
            Gravity.hasFlag(type, Gravity.TOP) -> typeStr = "$typeStr | TOP"
            Gravity.hasFlag(type, Gravity.BOTTOM) -> typeStr = "$typeStr | BOTTOM"
        }
        gravity.text = String.format("%s\r\n%s", "Gravity: ", typeStr)
        gravity.tag = type

        flowLayout.set { gravityFlags = type }
    }

    private fun addContent() {
        flowLayout.apply {
            var index = 0
            repeat(30) {
                addView(MaterialButton(context, null, R.attr.btnOutlineStyle).apply {
                    val length = Random.nextInt(2, 8)
                    text = PI.subSequence(index, index + length)
                    index += length
                    layoutParams = ViewGroup.LayoutParams(
                        length * 30, Random.nextInt(60, 130)
                    ).apply { setPadding(8, 0, 8, 0) }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        TextViewCompat.setAutoSizeTextTypeWithDefaults(
                            this, AppCompatTextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
                        )
                    }
                    insetTop = 0
                    insetBottom = 0
                    maxLines = 1
                })
            }
        }
    }

    companion object {
        private const val PI =
            "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651328230664709384460955058223172535940812848111745028410270193852110555964462294895493038196442881097566593344612847564823378678316527120190914564856692346034861045432664821339360726024914127372458700660631558817488152092096282925409171536436"
    }
}