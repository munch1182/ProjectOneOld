package com.munch.project.one.weight

import android.os.Bundle
import android.view.View
import com.munch.lib.base.ViewHelper
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.state.ViewNoticeHelper
import com.munch.lib.weight.LoadingView
import com.munch.project.one.databinding.ActivityLoadingBinding
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/11/12 11:40.
 */
class LoadingActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityLoadingBinding>()
    private val noticeHelper by lazy {
        ViewNoticeHelper(
            this,
            loading = bind.loadContainer to LoadingView(this, LoadingView.STYLE_TEXT)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            showLoad()
            loadContainer.setOnClickListener { showLoad() }
        }
    }

    private fun showLoad() {
        noticeHelper.loading { delay(Random.nextLong(800, 5000L)) }
    }

    override fun setContentView(view: View?) {
        setContentView(view, ViewHelper.newMMLayoutParams())
    }
}