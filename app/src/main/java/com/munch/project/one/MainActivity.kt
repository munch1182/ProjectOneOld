package com.munch.project.one

import android.os.Bundle
import com.munch.lib.fast.view.fvRvTv
import com.munch.plugin.annotation.Measure
import com.munch.project.one.base.BaseActivity

@Measure
class MainActivity : BaseActivity() {

    private val bind by fvRvTv(
        arrayOf(
            RecyclerViewActivity::class,
            PhoneInfoActivity::class,
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.init()
    }

    override val showHome: Boolean = false

}
