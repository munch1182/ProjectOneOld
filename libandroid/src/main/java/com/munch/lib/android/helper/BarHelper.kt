package com.munch.lib.android.helper

import android.app.Activity
import android.graphics.Color
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.munch.lib.android.extend.contentView
import com.munch.lib.android.extend.lazy

interface IBarHelper {

    /**
     * 控制内容显示是否延伸到状态栏
     *
     * @param extend true则延伸
     */
    fun extendContent2StatusBar(extend: Boolean = true): IBarHelper

    /**
     * 给状态栏设置指定颜色
     */
    fun colorStatusBar(@ColorInt color: Int): IBarHelper

    /**
     * 给导航栏设置指定颜色
     */
    fun colorNavigationBar(@ColorInt color: Int): IBarHelper

    /**
     * 将状态栏背景设置透明, 以显示底下的内容
     */
    fun transparentStatusBar() = colorStatusBar(Color.TRANSPARENT)

    /**
     * 全屏显示: 隐藏状态栏导航栏并将内容延伸
     *
     * 注意: 当退出全屏时会更改为默认状态, 需要重新设置
     *
     * 对于挖孔屏, 全屏也会延伸内容显示, 注意横竖屏的切换可能导致内容无法显示或者点击
     *
     * @param isFull 是否全屏显示
     */
    fun controlFullScreen(isFull: Boolean): IBarHelper

    /**
     * 控制状态栏是否显示为LightMode
     */
    fun controlLightMode(lightMode: Boolean): IBarHelper
}

/**
 * Create by munch1182 on 2020/12/12 21:03.
 */
class BarHelper(private val activity: Activity) : IBarHelper {

    constructor(fragment: Fragment) : this(fragment.requireActivity())

    private val content = activity.contentView
    private val window = activity.window
    private val controllerCompat by lazy { WindowCompat.getInsetsController(window, content) }

    override fun extendContent2StatusBar(extend: Boolean): IBarHelper {
        WindowCompat.setDecorFitsSystemWindows(window, !extend)
        return this
    }

    override fun colorStatusBar(@ColorInt color: Int): IBarHelper {
        window.statusBarColor = color
        return this
    }

    override fun colorNavigationBar(color: Int): IBarHelper {
        window.navigationBarColor = color
        return this
    }

    override fun controlFullScreen(isFull: Boolean): IBarHelper {
        if (isFull) {
            extendContent2Cutout(true)

            extendContent2StatusBar(true)

            controllerCompat.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            controllerCompat.hide(Type.systemBars())
        } else {
            extendContent2Cutout(false)

            extendContent2StatusBar(false)

            controllerCompat.show(Type.systemBars())
        }
        return this
    }

    /**
     * 将内容延伸到挖孔或者刘海区域
     */
    private fun extendContent2Cutout(extend: Boolean) {
        val lp = window.attributes
        lp.layoutInDisplayCutoutMode = if (extend) {
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        } else {
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        }
        window.attributes = lp
    }

    override fun controlLightMode(lightMode: Boolean): IBarHelper {
        controllerCompat.isAppearanceLightStatusBars = lightMode
        return this
    }
}