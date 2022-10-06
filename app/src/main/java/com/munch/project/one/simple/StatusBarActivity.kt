package com.munch.project.one.simple

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup.MarginLayoutParams
import com.munch.lib.android.extend.*
import com.munch.lib.android.helper.BarHelper
import com.munch.lib.fast.view.dialog.DialogHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.launch
import com.munch.plugin.annotation.Measure
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.databinding.ActivityStatusBarBinding
import com.munch.project.one.net.BiYing
import com.munch.project.one.other.ImageHelper.load
import kotlinx.coroutines.Dispatchers
import kotlin.random.Random

/**
 * Create by munch1182 on 2022/9/22 16:36.
 */
@Measure
class StatusBarActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    private val bind by bind<ActivityStatusBarBinding>()
    private val bar by lazy { BarHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(bind.barTb)
        extendBar()

        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true) }
        bind.barTb.navigationIcon?.setTint(Color.WHITE)

        bind.apply {
            barExtend.setOnClickListener { extendBar() }
            barColor.setOnClickListener { colorBar() }
            barLight.setOnClickListener { bar.controlLightMode(true) }
            barDark.setOnClickListener { bar.controlLightMode(false) }
            barDialog.setOnClickListener { dialog() }
            barDialogInput.setOnClickListener { dialogInput() }
            barFull.setOnClickListener { full() }
            barWallpaper.setOnClickListener { nextWallpaper() }
        }

        launch(Dispatchers.IO) {
            val f = if (BiYing.curr.exists()) {
                BiYing.curr
            } else {
                val files = BiYing.curr.parentFile?.listFiles()
                val index = files?.size?.let { Random.nextInt(it) } ?: 0
                files?.get(index)
            }
            bind.barImage.load(f)
        }

    }

    private fun nextWallpaper() {
        launch {
            if (!BiYing.curr.exists()) {
                bind.barWallpaperName.text = "None"
            } else {
                catch {
                    val tag = (bind.barWallpaperName.tag?.toOrNull<Int>() ?: -1) + 1
                    val files = BiYing.curr.parentFile?.listFiles()
                    if (files == null) {
                        bind.barWallpaperName.text = "None"
                        return@launch
                    }
                    val index = if (tag >= files.size) 0 else tag
                    files.getOrNull(index)?.let {
                        bind.barWallpaperName.tag = index
                        bind.barWallpaperName.text = it.name
                        bind.barImage.load(it)
                    }
                }
            }
        }
    }

    private fun dialog() {
        DialogHelper.bottom()
            .title("BottomDialog")
            .content("dialog from bottom")
            .cancel()
            .show()
    }

    private fun dialogInput() {
        /*window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        val inputView = AppCompatEditText(this)
        inputView.setBackgroundColor(newRandomColor())
        val lp = FrameLayout.LayoutParams(100, 300)
        lp.gravity = Gravity.BOTTOM
        inputView.layoutParams = lp
        contentView.addView(inputView)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            log(insets.getInsets(Type.ime()), insets.getInsets(Type.statusBars()))
            insets
        }
        WindowCompat.getInsetsController(window, inputView).show(Type.ime())*/
    }

    private fun colorBar() {
        bar.colorStatusBar(newRandomColor(0.5f))
    }

    private fun full() {
        val array = IntArray(4)
        bind.barImage.getLocationInWindow(array)
        bar.controlFullScreen(array[1] > 0)
    }

    private fun extendBar() {
        val lp = bind.barTb.layoutParams.toOrNull<MarginLayoutParams>()
        val extend = (lp?.topMargin ?: -1) == 0
        bar.extendContent2StatusBar(extend)
        if (extend) {
            bind.barTb.margin(t = statusBarHeightFromId)
            bar.colorStatusBar(Color.TRANSPARENT)
        } else {
            bind.barTb.margin(t = 0)
            bar.colorStatusBar(getColorPrimary())
        }
    }

}