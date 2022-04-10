package com.munch.lib.helper

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.munch.lib.extend.getStatusBarHeight

/**
 * Create by munch1182 on 2020/12/12 21:03.
 */
@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class BarHelper(activity: Activity) {

    constructor(fragment: Fragment) : this(fragment.requireActivity())

    private val window = activity.window

    /**
     * 将view适应状态栏延伸，注意：传入的view不同，可能会有不同的效果
     */
    fun fitView(view: View) {
        view.addPadding(t = view.context.getStatusBarHeight() ?: 0)
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

    /**
     * 设置状态栏颜色
     *
     * 当状态栏颜色与底下空间颜色一致时，就表现为透明状态栏
     */
    fun colorStatusBar(@ColorInt color: Int): BarHelper {
        window.run {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = color
        }
        return this
    }

    fun colorNavigation(@ColorInt color: Int): BarHelper {
        window.run {
            navigationBarColor = color
        }
        return this
    }

    fun colorStatusBarByRes(@ColorRes res: Int) =
        colorStatusBar(ContextCompat.getColor(window.context, res))

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

    @RequiresApi(Build.VERSION_CODES.M)
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
                    systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
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

        /**
         * 混合颜色
         *
         * @see [android.graphics.Color.valueOf]
         * @see [android.graphics.Color.argb]
         * (0xff << 24) ==> -16777216
         */
        @ColorInt
        fun calculateColor(@IntRange(from = 0L, to = 255L) alpha: Int, @ColorInt color: Int): Int {
            if (alpha == 0) {
                return color
            }
            //带透明度的颜色
            if (0xff != (color shr 24 and 0xff)) {
                return color
            }
            val r = color shr 16 and 0xff
            val g = color shr 8 and 0xff
            val b = color and 0xff
            val a = 1 - alpha / 255.0f
            return -16777216 or
                    ((r * a + 0.5f).toInt() shl 16) or
                    ((g * a + 0.5f).toInt() shl 8) or
                    (b * a + 0.5f).toInt()
        }

        /**
         * @see [android.graphics.Color.argb]
         */
        @ColorInt
        fun argb(alpha: Float, red: Float, green: Float, blue: Float): Int {
            return ((alpha * 255.0f + 0.5f).toInt() shl 24) or
                    ((red * 255.0f + 0.5f).toInt() shl 16) or
                    ((green * 255.0f + 0.5f).toInt() shl 8) or
                    ((blue * 255.0f + 0.5f).toInt())
        }
    }
}
