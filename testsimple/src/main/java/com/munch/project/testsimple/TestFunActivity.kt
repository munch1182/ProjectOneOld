package com.munch.project.testsimple

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.munch.lib.common.RouterHelper
import com.munch.lib.common.component.ThemeProvider
import com.munch.lib.helper.DateHelper
import com.munch.lib.helper.formatDate
import com.munch.lib.helper.isServiceRunning
import com.munch.lib.helper.startActivity
import com.munch.lib.log
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.testsimple.jetpack.net.Api
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Create by munch1182 on 2020/12/16 17:09.
 */
@AndroidEntryPoint
class TestFunActivity : TestRvActivity() {

    override fun setupIntent() {
        super.setupIntent()
        intent = Intent().putExtras(newBundle("Test Function", null, isBtn = true))
    }

    @Inject
    lateinit var api: Api

    private val img by lazy { ImageView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addStartView(img)
        RouterHelper.inject(this)
    }

    override fun clickItem(view: View, pos: Int) {
        super.clickItem(view, pos)
        when (pos) {
            0 -> {
                log(
                    "yyyy-MM-dd HH:mm:ss".formatDate(DateHelper.dayMillis()),
                    System.currentTimeMillis(),
                    DateHelper.dayMillis(),
                    DateHelper.TIME_HOUR_MIN_SEC,
                    DateHelper.dayMillis(),
                )
            }
            1 -> {
                val day1 = DateHelper.newData(
                    2001, 1, 2, 0, 0, 0
                )
                val day2 = DateHelper.newData(
                    2001, 1, 1, 23, 59, 59
                )
                log(DateHelper.isOneDay(day1.time, day2.time))
            }
            2 -> {
            }
            3 -> {
            }
            4 -> {
            }
            5 -> {
                val service = RouterHelper.getService(ThemeProvider::class.java)
                toast("当前主题:${service?.getTheme()}")
            }
            6 -> {
                toastServiceRunning()
            }
            7 -> startActivity(TestFunInFragmentActivity::class.java)
        }
    }

    private fun toastServiceRunning() {
        val running = isServiceRunning()

        val str = when {
            running == null -> {
                toast("错误")
                return
            }
            running -> {
                "有"
            }
            else -> {
                "没有"
            }
        }
        toast(str.plus("服务运行中"))
    }

    override fun getItems(): MutableList<TestRvItemBean> {
        return TestRvItemBean.newArray(
            "test fun 1",
            "test fun 2",
            "test fun 3",
            "test fun 4",
            "test fun 5",
            "test component",
            "Is Service Running",
            "testInFragment"
        )
    }
}