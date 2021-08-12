package com.munch.project.one.applib.weight

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import com.google.android.material.button.MaterialButton
import com.munch.lib.base.OnViewIndexClickListener
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.weight.FlowLayout
import com.munch.project.one.applib.R
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/8/11 14:13.
 */
class FlowLayoutActivity : BaseBigTextTitleActivity() {

    private val flowLayout by lazy { findViewById<FlowLayout>(R.id.flow_view) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow_layout)
        addContent()
    }

    private fun addContent() {
        flowLayout.apply {
            var index = 0
            repeat(10) {
                addView(newButton(this.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        Random.nextInt(180, 500), Random.nextInt(180, 260)
                    ).apply { setPadding(8, 0, 8, 0) }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        TextViewCompat.setAutoSizeTextTypeWithDefaults(
                            this, AppCompatTextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
                        )
                    }
                    val length = Random.nextInt(3, 9)
                    text = PI.subSequence(index, index + length)
                    index += length
                })
            }
        }
    }

    private fun newGravityButton(context: Context, @FlowLayout.Gravity type: Int) =
        newButton(context).apply {
            tag = type
            textSize = 12f
            text = when (type) {
                FlowLayout.START -> "START"
                FlowLayout.END -> "END"
                FlowLayout.CENTER -> "CENTER"
                FlowLayout.CENTER_HORIZONTAL -> "CENTER_HORIZONTAL"
                FlowLayout.CENTER_VERTICAL -> "CENTER_VERTICAL"
                FlowLayout.END_CENTER_VERTICAL -> "END_CENTER_VERTICAL"
                else -> ""
            }
            setOnClickListener(onGravityClickListener)
        }

    private val onGravityClickListener = object : OnViewIndexClickListener {
        override fun onClick(v: View?, pos: Int) {
            super.onClick(v, pos)
            flowLayout.setGravity(pos)
        }
    }

    private fun newButton(context: Context) =
        MaterialButton(context, null, R.attr.btnOutlineStyle).apply {
            insetTop = 0
            insetBottom = 0
            minWidth = 0
            gravity = Gravity.CENTER
            maxLines = 1
        }


    companion object {
        private const val PI =
            "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651328230664709384460955058223172535940812848111745028410270193852110555964462294895493038196442881097566593344612847564823378678316527120190914564856692346034861045432664821339360726024914127372458700660631558817488152092096282925409171536436"
    }
}