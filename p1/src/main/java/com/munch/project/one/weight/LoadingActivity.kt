package com.munch.project.one.weight

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.munch.lib.base.invisible
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.state.ViewNoticeHelper
import com.munch.project.one.databinding.ActivityLoadingBinding

/**
 * Create by munch1182 on 2021/11/12 11:40.
 */
class LoadingActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityLoadingBinding>()
    private val noticeHelper by lazy {
        ViewNoticeHelper(this, bind.loadContainer to View(this).apply {
            setBackgroundColor(Color.parseColor("#60d6d6d6"))
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.let {
            noticeHelper.apply {  }
            bind.loadView.postDelayed({
                bind.loadView.invisible()
            }, 1000L)
        }
    }
}