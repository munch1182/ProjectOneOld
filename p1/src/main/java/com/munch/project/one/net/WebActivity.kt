package com.munch.project.one.net

import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import com.bumptech.glide.Glide
import com.munch.lib.extend.bind
import com.munch.lib.extend.get
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.helper.ViewColorHelper
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.supportDef
import com.munch.lib.log.log
import com.munch.project.one.databinding.ActivityWebBinding

class WebActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    companion object {
        private const val LOGO =
            "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png"
    }

    private val bind by bind<ActivityWebBinding>()
    private val vm by get<WebVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewColorHelper.getColor(this)?.let { bind.shape.update { startColor = it } }
        bind.shape.setColor(Color.WHITE)

        Glide.with(this).load(LOGO).into(bind.logo)

        bind.search.setOnEditorActionListener a@{ _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                analyzeInput(bind.search.text?.toString() ?: return@a false)
                return@a true
            }
            return@a false
        }
    }

    private fun analyzeInput(s: String) {
        log(s)
    }
}