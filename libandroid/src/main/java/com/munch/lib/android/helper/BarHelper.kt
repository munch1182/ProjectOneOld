package com.munch.lib.android.helper


import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import com.munch.lib.android.extend.addPadding
import com.munch.lib.android.extend.hasFlag
import com.munch.lib.android.extend.isLight
import com.munch.lib.android.extend.statusBarHeight

/**
 * Create by munch1182 on 2020/12/12 21:03.
 */
@Suppress("DEPRECATION")
class BarHelper(activity: Activity) {

    constructor(fragment: Fragment) : this(fragment.requireActivity())

    private val window = activity.window

    /**
     * 将view适应状态栏延伸，注意：传入的view不同，可能会有不同的效果
     */
    fun fitView(view: View) {
        view.addPadding(t = statusBarHeight)
    }

    /**
     * @param extend 使内容延伸到状态栏
     */
    fun extendStatusBar(extend: Boolean = true): BarHelper {
        window.decorView.run {
            systemUiVisibility = if (extend) {
                systemUiVisibility or FLAGS_STATUS_BAR
            } else {
                systemUiVisibility and FLAGS_STATUS_BAR.inv()
            }
        }
        return this
    }

    val isExtendStatusBar: Boolean
        get() = window.decorView.systemUiVisibility.hasFlag(FLAGS_STATUS_BAR)

    /**
     * 设置状态栏颜色
     *
     * 当状态栏颜色与底下空间颜色一致时，就表现为透明状态栏
     */
    fun colorStatusBar(@ColorInt color: Int = Color.TRANSPARENT): BarHelper {
        window.run {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = color
        }
        return this
    }

    /**
     * 根据状态栏颜色自动设置状态栏文字颜色
     */
    fun fitStatusTextColor() {
        setTextColorBlack(window.statusBarColor.isLight())
    }

    fun colorNavigation(@ColorInt color: Int): BarHelper {
        window.run {
            navigationBarColor = color
        }
        return this
    }

    /**
     * 隐藏状态栏和导航栏，使内容全屏
     */
    fun fullScreen(full: Boolean = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            window.insetsController?.let {
                if (full) {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    it.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                }
            }
        } else {
            window.decorView.apply {
                systemUiVisibility = if (full) {
                    systemUiVisibility or FLAG_FULL or FLAGS_STATUS_BAR or FLAGS_NAVIGATION_BAR or FLAG_STICKY or FLAGS_STABLE
                } else {
                    systemUiVisibility and FLAG_FULL and FLAGS_STATUS_BAR and FLAGS_NAVIGATION_BAR and FLAG_STICKY and FLAGS_STABLE
                }
            }
        }
    }

    val isFullScreen: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // TODO: 111
            false
        } else {
            window.decorView.systemUiVisibility.hasFlag(FLAG_FULL)
        }

    fun setTextColorBlack(black: Boolean = true): BarHelper {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                if (black) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            window.decorView.run {
                systemUiVisibility = if (black) {
                    systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    (systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
        }
        return this
    }

    companion object {

        private const val FLAGS_STATUS_BAR = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        private const val FLAGS_NAVIGATION_BAR = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        private const val FLAG_STICKY = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        private const val FLAG_FULL = View.SYSTEM_UI_FLAG_FULLSCREEN

        /**
         * 保持布局，不随系统栏调整而变化
         */
        private const val FLAGS_STABLE = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}