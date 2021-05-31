package com.munch.test.project.one.anim

import android.graphics.Color
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.ChangeTransform
import android.transition.Fade
import android.transition.TransitionSet
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
                height += AppHelper.PARAMETER.getStatusBarHeight()
            }
            setPadding(0, AppHelper.PARAMETER.getStatusBarHeight(), 0, 0)
            navigationIcon = getBackIconWhite()
            setNavigationOnClickListener { onBackPressed() }
        }
        //未生效的原因可能与动画与behavior的先后顺序有关
        //可能需要监听并等到behavior之后再实现动画
        //或者先固定behavior的位置
        //1. 设置相同的TransitionName
        ViewCompat.setTransitionName(bind.imageTitleTitle, getString(R.string.share_element_title))
        //2. 给未设置ShareElement的元素设置动画
        window.exitTransition = Fade()
        window.enterTransition = Fade()
        //3. 设置ShareElement动画
        val trans = AutoTransition().apply {
            addTransition(ChangeTransform())
            ordering = TransitionSet.ORDERING_TOGETHER
            addTarget(bind.imageTitleTitle)
        }
        window.sharedElementEnterTransition = trans
        window.sharedElementExitTransition = trans
    }
}