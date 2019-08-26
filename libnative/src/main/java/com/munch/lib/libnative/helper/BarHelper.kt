package com.munch.lib.libnative.helper

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.core.view.isGone
import com.munch.lib.libnative.helper.BarHelper.Companion.ID_CONTENT

/**
 * <p>
 * StatusBarView/Navigation分为两种情形：
 * 1、内容延伸到BarView
 * 2、内容与BarView分开
 * <p>
 * 对状态栏解决方法：
 * 1、使内容延伸到状态栏并使状态栏透明
 * 2、添加一个View模拟状态栏 (被添加到 [ID_CONTENT]下 )
 * 3、根据需求对添加的View设置颜色和透明度
 * <p>
 * 简单来说，添加一个View来模拟颜色、透明等视觉效果
 * <p>
 * 因为在Fragment下，添加的fakeView可能会被盖住，因此需要注意调用的时机
 *
 * Created by Munch on 2019/7/31 8:45
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class BarHelper private constructor(val activity: Activity) {

    private var isTranslucentNavigation: Boolean = false
    private var navigationBarColor: Int? = null

    fun fullScreen() {
        activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    /**
     * 可以动态更改，然后调用[android.app.Activity.recreate]，但是变量要保存到activity外
     */
    @JvmOverloads
    fun isTranslucentNavigation(translucent: Boolean = true): BarHelper {
        this.isTranslucentNavigation = translucent
        return this
    }

    fun setTransparent() {
        translucentBar()
        getFakeView().visibility = View.GONE
    }

    /**
     * 切换状态栏模式，显示效果为状态栏文字白色或者黑色
     */
    @RequiresApi(Build.VERSION_CODES.M)
    @JvmOverloads
    fun setDarkMode(darkMode: Boolean = true, @ColorInt color: Int = Color.TRANSPARENT) {
        val window = activity.window
        if (darkMode) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        //设置模式的时候默认将内容延伸到状态栏以与其他方法保持一致
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

            /*设置状态栏背景色，与非light status模式保持一致*/
            window.statusBarColor = color
            getFakeView().visibility = View.GONE
            setNavigation()
        } else {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and
                        //清除该模式
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            /*将内容延伸到状态栏，与light status模式保持一致*/
            setColor(color)
        }
    }

    @JvmOverloads
    fun setColor(@ColorInt color: Int = DEF_TRANSLUCENT_COLOR) {
        translucentBar()
        getFakeView().apply {
            if (isGone) {
                visibility = View.VISIBLE
            }
        }.setBackgroundColor(color)
    }

    fun setColor(a: Int, r: Int, g: Int, b: Int) {
        setColor(
            argb(
                a.toFloat(),
                r.toFloat(),
                g.toFloat(),
                b.toFloat()
            )
        )
    }

    private fun translucentBar(): BarHelper {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
            /*将内容延伸到Status */
            var visibility = window.decorView.visibility

            visibility = visibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = visibility
            window.statusBarColor = Color.TRANSPARENT

            setNavigation()
        } else {
            val attributes = activity.window.attributes
            /*将内容延伸到Status */
            attributes.flags = attributes.flags or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            /*将内容延伸到Navigation*/
            if (isTranslucentNavigation) {
                attributes.flags = attributes.flags or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            } else {
                attributes.flags = attributes.flags xor WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            }
            activity.window.attributes = attributes
        }
        return this
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setNavigation() {
        val window = activity.window

        /*将内容延伸到Navigation*/
        if (isTranslucentNavigation) {
            /*visibility = visibility or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION*/
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }

        if (isTranslucentNavigation) {
            navigationBarColor = window.navigationBarColor
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            if (navigationBarColor != null) {
                window.navigationBarColor = navigationBarColor!!
            }
        }
    }

    private fun getFakeView(): View {
        val layout = activity.window.decorView.findViewById<FrameLayout>(ID_CONTENT)
        return layout.findViewWithTag<View>(TAG) ?: View(activity).apply {
            tag = TAG
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(activity)
            )
            layout.addView(this)
        }
    }


    companion object {

        /**
         * #7f000000 => 2130706432
         */
        const val DEF_TRANSLUCENT_COLOR = 2130706432
        const val ID_CONTENT = android.R.id.content
        const val TAG = "com.munch.lib.bar.BarHelper"

        @JvmStatic
        fun with(activity: Activity): BarHelper {
            return BarHelper(activity)
        }

        fun getStatusBarHeight(context: Context): Int {
            val resources = context.resources
            val id = resources.getIdentifier("status_bar_height", "dimen", "android")
            return try {
                resources.getDimensionPixelSize(id)
            } catch (e: Resources.NotFoundException) {
                -1
            }
        }

        /**
         * 混合颜色
         *
         * @see [android.graphics.Color.valueOf]
         * @see [android.graphics.Color.argb]
         * (0xff << 24) ==> -16777216
         */
        @JvmStatic
        @ColorInt
        fun calculateColor(@IntRange(from = 0, to = 255) alpha: Int, @ColorInt color: Int): Int {
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
        @JvmStatic
        @ColorInt
        fun argb(alpha: Float, red: Float, green: Float, blue: Float): Int {
            return ((alpha * 255.0f + 0.5f).toInt() shl 24) or
                    ((red * 255.0f + 0.5f).toInt() shl 16) or
                    ((green * 255.0f + 0.5f).toInt() shl 8) or
                    ((blue * 255.0f + 0.5f).toInt())
        }
    }

}