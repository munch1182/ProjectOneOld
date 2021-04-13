package com.munch.test.project.one.anim

import android.graphics.Color
import android.os.Bundle
import androidx.core.view.ViewCompat
import com.munch.lib.fast.base.activity.BaseActivity
import com.munch.lib.fast.extend.bind
import com.munch.pre.lib.extend.getBackIconWhite
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.helper.BarHelper
import com.munch.test.project.one.R
import com.munch.test.project.one.databinding.ActivityImageTitleBinding

/**
 * Create by munch1182 on 2021/4/13 11:14.
 */
class ImageTitleActivity : BaseActivity() {

    private val bind by bind<ActivityImageTitleBinding>(R.layout.activity_image_title)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        window.navigationBarColor = Color.TRANSPARENT
        BarHelper(this).hideStatusBar(true).colorStatusBar(Color.TRANSPARENT)
        bind.imageTitleTb.apply {
            title = "荷塘月色"
            layoutParams = layoutParams.apply {
                height += AppHelper.PARAMETER.getNavigationBarHeight()
            }
            setPadding(0, AppHelper.PARAMETER.getNavigationBarHeight(), 0, 0)
            navigationIcon = getBackIconWhite()
            setNavigationOnClickListener { onBackPressed() }
        }
        ViewCompat.setTransitionName(bind.imageTitleTitle, getString(R.string.share_element_title))
    }
}