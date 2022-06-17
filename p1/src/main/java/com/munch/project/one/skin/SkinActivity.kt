package com.munch.project.one.skin

import android.Manifest
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.munch.lib.extend.bind
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.supportDef
import com.munch.lib.helper.FileHelper
import com.munch.lib.result.isGrantAll
import com.munch.lib.result.permission
import com.munch.project.one.databinding.ActivitySkinBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by munch1182 on 2022/6/17 19:05.
 */
class SkinActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val bind by bind<ActivitySkinBinding>()
    private val skin by lazy { SkinHelper() }

    override fun onCreate(savedInstanceState: Bundle?) {
        skin.apply(this)
        super.onCreate(savedInstanceState)
        bind.query.setOnClickListener { }
        bind.load.setOnClickListener {
            lifecycleScope.launch {
                if (permission(Manifest.permission.READ_EXTERNAL_STORAGE).isGrantAll()) {
                    val uri = FileHelper.chose(this@SkinActivity) ?: return@launch
                    lifecycleScope.launch(Dispatchers.Default) stop@{
                        val file = FileHelper.uri2File(ctx, uri) ?: return@stop
                        if (SkinHelper.loadSkin(ctx, file.path)) {
                            withContext(Dispatchers.Main) { skin.update() }
                        }
                    }
                }
            }
        }
    }
}