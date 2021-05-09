package com.munch.pre.lib.base

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Create by munch1182 on 2021/5/8 14:24.
 */
open class BaseRootActivity : AppCompatActivity() {

    /**
     * 延迟加载，在onCreate中调用但延迟到onResume之后在执行(甚至会延迟到lifecycle中的onResume之后)
     * 因为在onCreate中执行所以只会执行一次
     *
     * 适用于延迟显示，加快页面启动速度
     */
    open fun delayLoad(load: () -> Unit) {
        window.decorView.post { BaseApp.getInstance().getMainHandler().post { load.invoke() } }
    }

    open fun toast(msg: String) {
        runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
    }
}

open class BaseRootFragment : Fragment() {

    open fun delayLoad(load: () -> Unit) {
        if (activity is BaseRootActivity) {
            (activity as BaseRootActivity).delayLoad(load)
        }
    }

    open fun toast(msg: String) {
        if (activity is BaseRootActivity) {
            (activity as BaseRootActivity).toast(msg)
        }
    }

}