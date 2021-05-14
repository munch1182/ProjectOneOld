package com.munch.project.launcher.set

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.munch.pre.lib.extend.addPadding
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.helper.IntentHelper
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.databinding.ActivitySetBinding
import com.munch.project.launcher.extend.bind
import java.lang.Exception

/**
 * Create by munch1182 on 2021/5/14 11:21.
 */
class SettingActivity : BaseActivity() {

    private val bind by bind<ActivitySetBinding>(R.layout.activity_set)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        bind.setContainer.addPadding(t = AppHelper.PARAMETER.getStatusBarHeight())
        bind.setDefault.setOnClickListener {
            queryOrRequest()
        }
    }

    private fun queryOrRequest() {
        val pm = packageManager
        var set = false
        val info = pm.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            PackageManager.MATCH_DEFAULT_ONLY
        )
        if (info != null) {
            set = info.activityInfo.packageName == packageName
        }
        if (set) {
            toast("已经是默认应用了")
        } else {
            try {
                startActivity(IntentHelper.advancedIntent())
            } catch (e: Exception) {
                startActivity(IntentHelper.appIntent())
            }
        }
    }
}