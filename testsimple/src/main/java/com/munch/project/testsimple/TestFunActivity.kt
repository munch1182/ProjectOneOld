package com.munch.project.testsimple

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.munch.lib.common.RouterHelper
import com.munch.lib.common.component.ThemeProvider
import com.munch.lib.helper.*
import com.munch.lib.log
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean

/**
 * Create by munch1182 on 2020/12/16 17:09.
 */
class TestFunActivity : TestRvActivity() {

    private val liveData = MutableLiveData<ArrayList<String>>(arrayListOf())

    override fun isBtn() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        liveData.observe(this){
            log(it)
        }
    }

    override fun clickItem(view: View, pos: Int) {
        super.clickItem(view, pos)
        when (pos) {
            0 -> {
            }
            1 -> {
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