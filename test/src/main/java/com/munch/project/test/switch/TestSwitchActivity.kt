package com.munch.project.test.switch

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.munch.lib.helper.AppHelper
import com.munch.lib.helper.startActivity
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.test.R
import com.munch.project.test.TestMainActivity

/**
 * Create by munch1182 on 2020/12/30 11:16.
 */
class TestSwitchActivity : TestRvActivity() {

    companion object {

        private const val KEY_RESTART = "key_restart"

        /**
         * 因为[recreate]会闪烁，所以这里新启动一个[TestSwitchActivity]并传入动画来让这个过程无感知
         *
         * 这种方法要通过参数传递来还原当前activity的状态以显得一模一样，例如recyclerview的滑动距离
         */
        fun switch(context: Activity, needRestart: Boolean = false) {
            context.startActivity(
                TestSwitchActivity::class.java,
                Bundle().apply { putBoolean(KEY_RESTART, needRestart) })
            context.overridePendingTransition(R.anim.anim_mask_in, R.anim.anim_mask_out)
        }
    }

    private var language = true
    private val instance = SwitchHelper.INSTANCE

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SwitchHelper.INSTANCE.attachBaseContent(newBase))
    }

    override fun isBtn() = true
    override fun notShowBack() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.setTheme(this)
        super.onCreate(savedInstanceState)

        //这里是因为基类有颜色和主题无法被影响所以要手动设置
        findViewById<ViewGroup>(R.id.rv_test_srl).setBackgroundColor(attr2Color(R.attr.pageBg))

        language = instance.getNowLanguage() == "en"
        title = if (language) "LanguageEn" else "Language"

        addEndView(getTestView().apply {
            setBackgroundColor(Color.TRANSPARENT)
            findViewById<TextView>(R.id.item_rv_test_tv).text =
                getString(R.string.str_test_language)
        })
    }

    override fun getItems(): MutableList<TestRvItemBean> {
        return TestRvItemBean.newArray(
            getString(R.string.str_switch_language),
            getString(R.string.str_switch_theme),
            getString(R.string.str_switch_day_night),
            getString(R.string.str_jump),
        )
    }

    override fun clickItem(view: View, pos: Int) {
        super.clickItem(view, pos)
        when (pos) {
            0 -> {
                instance.switchLanguage(if (language) "zh" else "en")
                switch(this, true)
                finish()
            }
            1 -> {
                instance.switchThemeMode(if (instance.getThemeMode() == ThemeHelper.THEME_DEF) ThemeHelper.THEME_MODE_1 else ThemeHelper.THEME_DEF)
                switch(this, true)
                finish()
            }
            2 -> {
                //先启动activity
                switch(this)
                instance.switchDayNight()
                finish()
            }
            3 -> {
                startActivity(TestSwitchActivity::class.java)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //不写在[finish()]是避免切换多次直接restart
        if (intent?.extras?.getBoolean(KEY_RESTART, false) == true) {
            AppHelper.resetApp2Activity(this, TestMainActivity::class.java)
            //类似的动画效果可以掩盖重启感知
            overridePendingTransition(R.anim.anim_close_in, R.anim.anim_close_out)
        }
    }

}