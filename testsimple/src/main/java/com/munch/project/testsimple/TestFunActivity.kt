package com.munch.project.testsimple

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.munch.lib.common.RouterHelper
import com.munch.lib.common.component.ThemeProvider
import com.munch.lib.extend.retrofit.BaseUrlManager
import com.munch.lib.helper.FileHelper
import com.munch.lib.helper.ResultHelper
import com.munch.lib.helper.isServiceRunning
import com.munch.lib.helper.startActivity
import com.munch.lib.log
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvItemBean
import com.munch.project.testsimple.jetpack.net.Api
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
                ResultHelper.with(this)
                    .startForResult(FileHelper.fileIntent())
                    .res { isOk, resultCode, data -> }
            }
            1 -> {
                lifecycleScope.launch {
                    BaseUrlManager.getInstance().setBaseUrl("https://www.baidu.com/")
                    val articleList3 = api.getArticleList3(0).whenFail {
                        log(msg)
                    }?.successData()
                    log("$articleList3")

                }
            }
            2 -> {
                lifecycleScope.launch {
                    BaseUrlManager.getInstance().setBaseUrl("https://www.wanandroid.com/")
                    val articleList3 = api.getArticleList3(0).whenFail {
                        log(msg)
                    }?.successData()
                    log("$articleList3")

                }
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