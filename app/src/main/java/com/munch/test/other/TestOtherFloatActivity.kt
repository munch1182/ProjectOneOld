package com.munch.test.other

import android.annotation.SuppressLint
import android.os.Bundle
import com.munch.test.R
import com.munch.test.base.BaseActivity
import kotlinx.android.synthetic.main.activity_test_other_float.*
import java.math.BigDecimal
import java.text.NumberFormat

/**
 * Create by Munch on 2020/09/08
 */
class TestOtherFloatActivity : BaseActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_other_float)

        setToolBar(view_tb)

        val numberFormatInstance = NumberFormat.getInstance()
        numberFormatInstance.maximumFractionDigits = 1
        test_other_float_tv.text = "3f*0.1=${3f * 0.1}\n\n" +
                "BigDecimal(3.toString()).divide(BigDecimal(0.1.toString())).toDouble()=${
                    multi(3f, 0.1f)
                }\n\n" + "(3.556 * 100f).toInt() * 0.01f=${(3.556 * 100f).toInt() * 0.01}\n" +
                "(3.000 * 100f).toInt() * 0.01f=${(3.0001 * 100f).toInt() * 0.01f}\n" +
                "(3.556 * 10f).toInt() * 0.1f=${(3.556 * 10f).toInt() * 0.1}\n\n" +
                "numberFormatInstance.format(3.556)=${numberFormatInstance.format(3.556)}\n" +
                "String.format(\"%.1f\",3.556)=${String.format("%.1f", 3.556)}\n\n" +
                "3.556 * 10f=${3.556 * 10f}==>(35.56/10f).toString().subSequence(0, (35.56/10f).toString().indexOf('.') + 2)=" +
                "${
                    (3.556 * 10f / 10f).toString()
                        .subSequence(0, (3.556 * 10f / 10f).toString().indexOf('.') + 2)
                }"
    }

    private fun multi(a: Float, b: Float): Double {
        return BigDecimal(a.toString()).multiply(BigDecimal(b.toString())).toDouble()
    }
}