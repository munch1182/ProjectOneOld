package com.munch.test.lib.pre.info

import android.os.Bundle
import com.munch.lib.fast.BuildConfig
import com.munch.lib.fast.base.BaseTopActivity
import com.munch.lib.fast.extend.load
import com.munch.pre.lib.extend.StringHelper
import com.munch.pre.lib.helper.AppHelper
import com.munch.test.lib.pre.R
import com.munch.test.lib.pre.databinding.ActivityInfoBinding

/**
 * Create by munch1182 on 2021/3/31 16:17.
 */
class InfoActivity : BaseTopActivity() {

    private val bind by bind<ActivityInfoBinding>(R.layout.activity_info)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this

        val sb = StringBuilder()
        var index = 0
        AppHelper.PARAMETER.collect().forEach {
            if (index > 0) {
                sb.append(StringHelper.LINE_SEPARATOR)
            }
            sb.append(it.first).append(": ").append(it.second)
            index++
        }
        sb.append(StringHelper.LINE_SEPARATOR)
        val vn = AppHelper.getVersionCodeAndName()
        sb.append("app version: ").append("${vn?.second}(${vn?.first})")
        sb.append(StringHelper.LINE_SEPARATOR)
        //build后有效
        sb.append("build time: ").append(BuildConfig.BUILD_TIME)
        bind.infoPm.text = sb.toString()

        bind.infoIcon.load(AppHelper.getAppIcon(this, this.packageName))
        val apps = AppHelper.getInstallApp(this, this.packageName)
        if (!apps.isNullOrEmpty()) {
            bind.infoIconShow.load(apps[0].loadIcon(packageManager))
        }
    }

}