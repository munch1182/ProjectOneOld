package com.munch.test.project.one.view

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.munch.pre.lib.extend.digitsInput
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityColorPaletteBinding

/**
 * Create by munch1182 on 2021/5/19 16:53.
 */
class ColorPaletteActivity : BaseTopActivity() {

    private val bind by bind<ActivityColorPaletteBinding>(R.layout.activity_color_palette)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            lifecycleOwner = this@ColorPaletteActivity
            colorPaletteRgb.apply {
                digitsInput("ABCDEFabcdef1234567890,")
                addTextChangedListener(afterTextChanged = newTextWatcher())
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun EditText.newTextWatcher(): (text: Editable?) -> Unit = {
        var text = this.text.toString()
        var color = -1
        if (text.contains(",")) {
            val colors = text.split(",")
            try {
                if (colors.size == 3) {
                    color = Color.rgb(colors[0].toInt(), colors[1].toInt(), colors[2].toInt())
                } else if (colors.size == 4) {
                    val a = colors[0].toInt()
                    color = Color.argb(a, colors[1].toInt(), colors[2].toInt(), colors[3].toInt())
                }
            } catch (e: Exception) {
                //ignore
            }
            if (color != -1) {
                bind.colorPaletteView.setColor(color)
                bind.colorPaletteValue.text = String.format("#%06X", color)
            } else {
                bind.colorPaletteView.reset()
                bind.colorPaletteValue.text = ""
            }
        } else {
            if (!text.startsWith("#")) {
                text = "#$text"
            }
            try {
                color = Color.parseColor(text)
            } catch (e: Exception) {
                //ignore
            }
            if (color != -1) {
                bind.colorPaletteView.setColor(color)
                val a = color shr 24 and 0xff
                val r = color shr 16 and 0xff
                val g = color shr 8 and 0xff
                val b = color and 0xff
                bind.colorPaletteValue.text = "$a,$r,$g,$b"
            } else {
                bind.colorPaletteView.reset()
                bind.colorPaletteValue.text = ""
            }
        }
    }

}