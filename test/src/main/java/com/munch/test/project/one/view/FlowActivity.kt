package com.munch.test.project.one.view

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import com.munch.lib.fast.base.dialog.SimpleDialog
import com.munch.lib.fast.weight.FlowLayout
import com.munch.pre.lib.extend.getAttrFromTheme
import com.munch.pre.lib.extend.getColorCompat
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityFlowBinding
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/4/14 9:58.
 */
class FlowActivity : BaseTopActivity() {

    private val bind by bind<ActivityFlowBinding>(R.layout.activity_flow)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            lifecycleOwner = this@FlowActivity
            flowBtn.setOnClickListener { showBottomDialog() }
        }
        addItemView()
    }

    private fun showBottomDialog() {
        SimpleDialog.Bottom(this)
            .addItems("START", "END", "CENTER", "CENTER_HORIZONTAL", "CENTER_VERTICAL")
            .setOnItemClickListener { _, i ->
                bind.flowFlow.apply {
                    when (i) {
                        0 -> setGravity(FlowLayout.START)
                        1 -> setGravity(FlowLayout.END)
                        2 -> setGravity(FlowLayout.CENTER)
                        3 -> setGravity(FlowLayout.CENTER_HORIZONTAL)
                        4 -> setGravity(FlowLayout.CENTER_VERTICAL)
                        else -> {
                        }
                    }
                }
            }.show()
    }

    private fun addItemView() {
        var textView: TextView
        var index = 0
        val color = getColorCompat(getAttrFromTheme(R.attr.colorPrimary).resourceId)
        for (i in 0..22) {
            textView = AppCompatTextView(this).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    90 * Random.nextDouble(1.2, 3.5).toInt(),
                    60 * Random.nextDouble(1.0, 4.5).toInt()
                ).apply { setMargins(8, 4, 8, 4) }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(
                        this,
                        AppCompatTextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
                    )
                }
                gravity = Gravity.CENTER
                maxLines = 1
                val length = Random.nextInt(3, 7)
                text = PI.subSequence(index, index + length)
                index += length
                setBackgroundColor(color)
            }
            bind.flowFlow.addView(textView)
        }
    }

    companion object {
        private const val PI =
            "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651328230664709384460955058223172535940812848111745028410270193852110555964462294895493038196442881097566593344612847564823378678316527120190914564856692346034861045432664821339360726024914127372458700660631558817488152092096282925409171536436"
    }
}