package com.munch.project.one.skin

import android.Manifest
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.munch.lib.extend.bind
import com.munch.lib.extend.dp2Px
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.base.DataHelper
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

/**
 * Created by munch1182 on 2022/6/17 19:05.
 */
class SkinActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val bind by bind<ActivitySkinBinding>()

    private val skinDir by lazy { File(filesDir, "skin") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.query.setOnClickListener { querySkin() }
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

    private fun updateSkin(file: File) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (SkinHelper.loadSkin(ctx, file.absolutePath)) {
                DataHelper.saveSkinPath(file.absolutePath)
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
                mutableSetOf(SkinHelper.SkinAttr.TextColor(com.munch.lib.fast.R.color.colorText))
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
            mutableSetOf(SkinHelper.SkinAttr.TextColor(com.munch.lib.fast.R.color.colorText))
        )
        bind.container.addView(reset, 3)
    }
}