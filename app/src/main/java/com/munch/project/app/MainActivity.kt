package com.munch.project.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.munch.lib.common.RouterHelper
import com.munch.lib.common.start2Component
import com.munch.lib.helper.restartApp2Activity

/**
 * 此类作为壳引用启动页，主要用于xml注册
 * 如果不涉及app重启，可以被Splash取代
 *
 * 重启时，做重启操作的页面导引到此页，此页面自身重启，再从此页跳转到真正的首页，可以避免对首页的直接引用
 */
@Route(path = RouterHelper.App.MAIN)
class MainActivity : AppCompatActivity() {

    /**
     * 用以标记接收到重启的请求
     */
    @Autowired(name = RouterHelper.App.KEY_RESTART)
    @JvmField
    var restart: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RouterHelper.inject(this)
        if (restart) {
            //如果需要重启，则重启自身来清除栈内activity，再跳转首页activity
            restartApp2Activity(this::class.java)
        } else {
            start2Component(RouterHelper.Test.MAIN)
        }
        overridePendingTransition(R.anim.anim_mask_in, R.anim.anim_mask_in)
        finish()
    }
}