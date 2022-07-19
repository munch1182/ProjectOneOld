package com.munch.project.one.skin

import android.Manifest
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.munch.lib.extend.*
import com.munch.lib.fast.R
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.helper.ViewColorHelper
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.supportDef
import com.munch.lib.helper.FileHelper
import com.munch.lib.helper.SkinHelper
import com.munch.lib.result.isGrantAll
import com.munch.lib.result.permission
import com.munch.project.one.databinding.ActivitySkinBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.lazy
import kotlin.random.Random

/**
 * Created by munch1182 on 2022/6/17 19:05.
 */
class SkinActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val bind by bind<ActivitySkinBinding>()
    private val skin by lazy { SkinHelper() }

    private val skinDir by lazy { File(filesDir, "skin") }

    override fun onCreate(savedInstanceState: Bundle?) {
        skin.apply(this)
        super.onCreate(savedInstanceState)
        skinUpdate()
        bind.query.setOnClickListener { querySkin() }
        bind.generateColor.setOnClickListener { generateColor() }
        bind.load.setOnClickListener {
            lifecycleScope.launch {
                if (permission(Manifest.permission.READ_EXTERNAL_STORAGE).isGrantAll()) {
                    val uri = FileHelper.chose(this@SkinActivity) ?: return@launch
                    lifecycleScope.launch(Dispatchers.Default) stop@{
                        val file = FileHelper.uri2File(ctx, uri) ?: return@stop
                        updateSkin(file)
                    }
                }
            }
        }
    }

    private fun skinUpdate() {
        skin.onUpdate {
            val primary = SkinHelper.getColor(ctx, R.color.colorPrimary)
            val onPrimary = SkinHelper.getColor(ctx, R.color.colorOnPrimary)
            supportActionBar?.apply {
                setBackgroundDrawable(ColorDrawable(primary))
                title = title?.color(onPrimary)
                val home =
                    getAttrArrayFromTheme(android.R.attr.homeAsUpIndicator) {
                        getDrawable(0)?.apply { setTint(onPrimary) }
                    }
                setHomeAsUpIndicator(home)
            }
            bar.colorStatusBar(primary).fitStatusTextColor()
        }
    }

    private fun generateColor() {
        val r = Random
        val color = Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255))
        updateColor(color)
    }

    private fun updateColor(color: Int) {
        val needBlack = color.isLight()
        val textColor = if (needBlack) Color.BLACK else Color.WHITE
        ViewColorHelper.setColor(color)

        val cs = color.toColorStr()
        bind.color.text = cs
        bind.color.setTextColor(Color.parseColor(cs))

        //手动更改当前页面, 否则调用ViewColorHelper.updateColor(this)
        val btn = arrayOf(bind.load, bind.query, bind.generateColor)

        btn.forEach {
            it.backgroundTintList = ColorStateList(arrayOf(intArrayOf()), intArrayOf(color))
            it.setTextColor(textColor)
        }

        supportActionBar?.apply {
            setBackgroundDrawable(ColorDrawable(color))
            title = title?.color(textColor)
            val home = getAttrArrayFromTheme(android.R.attr.homeAsUpIndicator) {
                getDrawable(0)?.apply { setTint(textColor) }
            }
            setHomeAsUpIndicator(home)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            setDisplayShowCustomEnabled(true)
        }
        bar.colorStatusBar(color).setTextColorBlack(needBlack)
    }

    private fun updateSkin(file: File) {
        lifecycleScope.launch(Dispatchers.IO) {
            val path = file.absolutePath
            if (SkinHelper.loadSkin(ctx, path)) {
                withContext(Dispatchers.Main) { skin.update() }
            }
        }
    }

    private fun querySkin() {
        lifecycleScope.launch(Dispatchers.IO) {
            val skinInDir = skinDir.list()
            assets.list("skin")?.forEach {
                if (skinInDir?.contains(it) == true) {
                    return@forEach
                }
                assets.open("skin/$it").also { ins ->
                    FileHelper.copy2File(ins, File(skinDir, it))
                }
            }
            withContext(Dispatchers.Main) { showSkinInView() }
        }
    }

    private fun showSkinInView() {
        skinDir.list()?.reversed()?.forEach {
            val view = TextView(this).apply {
                text = it
                setPadding(dp2Px(8f).toInt())
                setOnClickListener { _ -> updateSkin(File(skinDir, it)) }
            }
            skin.add(
                view,
                mutableSetOf(SkinHelper.SkinAttr.TextColor(R.color.colorText))
            )
            bind.container.addView(view, 3)
        }
        val reset = TextView(this).apply {
            text = "reset"
            setPadding(dp2Px(8f).toInt())
            setOnClickListener { skin.reset() }
        }
        skin.add(
            reset,
            mutableSetOf(SkinHelper.SkinAttr.TextColor(R.color.colorText))
        )
        bind.container.addView(reset, 3)
    }
}