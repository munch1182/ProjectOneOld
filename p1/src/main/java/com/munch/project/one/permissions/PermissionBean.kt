package com.munch.project.one.permissions

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.databinding.BindingAdapter
import com.munch.lib.app.AppHelper

/**
 * Create by munch1182 on 2021/10/14 16:35.
 */
typealias PB = PermissionBean

data class PermissionBean(
    val name: String,
    val minVersion: Int = 1,
    val maxVersion: Int = Int.MAX_VALUE,
    var note: String = "普通权限",
    var isGrantedJudge: (() -> Boolean) = {
        ActivityCompat.checkSelfPermission(AppHelper.app, name) == PackageManager.PERMISSION_GRANTED
    },
    var intent: Intent? = null
) {

    companion object {

        @JvmStatic
        @BindingAdapter("bind_permission")
        fun bind(btn: Button, bean: PermissionBean) {
            if (!bean.isSupport) {
                btn.text = "不可用"
                btn.isEnabled = false
            } else {
                btn.text = if (bean.isGranted) "已获取" else "申请"
            }
        }
    }

    val isGranted: Boolean
        get() = isGrantedJudge.invoke()
    val isSupport: Boolean
        get() = Build.VERSION.SDK_INT in minVersion..maxVersion

    val versionStr: String
        get() = "Version: $minVersion${if (maxVersion != Int.MAX_VALUE) "-$maxVersion" else "+"} "
    val needJumpStr: String
        get() = "Jump: ${intent != null}"
}