package com.munch.project.test.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.munch.lib.test.TestBaseTopActivity
import com.munch.lib.test.TestDialog
import com.munch.lib.test.view.FlowLayout
import com.munch.project.test.R
import kotlin.random.Random

/**
 * Create by munch1182 on 2020/12/10 22:37.
 */
class TestFlowLayoutActivity : TestBaseTopActivity() {

    private val align by lazy { findViewById<Button>(R.id.flow_layout_btn) }
    private val container by lazy { findViewById<FlowLayout>(R.id.flow_layout_container) }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity_test_flow_layout)

        addItemView()
        container.setGravity(FlowLayout.CENTER)
        align.setOnClickListener {
            showBottomDialog()
        }
    }

    private fun showBottomDialog() {
        TestDialog.bottom(this)
            .addItems("START", "END", "CENTER", "CENTER_HORIZONTAL", "CENTER_VERTICAL")
            .setOnClickListener { dialog, pos ->
                when (pos) {
                    0 -> container.setGravity(FlowLayout.START)
                    1 -> container.setGravity(FlowLayout.END)
                    2 -> container.setGravity(FlowLayout.CENTER)
                    3 -> container.setGravity(FlowLayout.CENTER_HORIZONTAL)
                    4 -> container.setGravity(FlowLayout.CENTER_VERTICAL)
                    else -> {
                    }
                }
                dialog.cancel()
            }.show()
    }

    private fun addItemView() {
        var textView: TextView
        var index = 0
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
                setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
            }
            container.addView(textView)
        }
    }

    companion object {
        private const val PI =
            "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651328230664709384460955058223172535940812848111745028410270193852110555964462294895493038196442881097566593344612847564823378678316527120190914564856692346034861045432664821339360726024914127372458700660631558817488152092096282925409171536436"
    }

}