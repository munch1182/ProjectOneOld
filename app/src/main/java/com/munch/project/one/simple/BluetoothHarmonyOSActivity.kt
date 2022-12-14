package com.munch.project.one.simple

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.core.content.PermissionChecker
import com.munch.lib.android.extend.setDoubleClickListener
import com.munch.lib.android.extend.setting
import com.munch.lib.android.extend.toast
import com.munch.lib.android.helper.InfoHelper
import com.munch.lib.android.result.ExplainTime
import com.munch.lib.android.result.with
import com.munch.lib.fast.view.dialog.DialogHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.fastview.fvSvTv
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef

/**
 * Create by munch1182 on 2022/12/14 15:19.
 */
class BluetoothHarmonyOSActivity : BaseActivity(),
    ActivityDispatch by dispatchDef("权限检查需要使用PermissionChecker") {

    private val bind by fvSvTv()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showInfo()

        bind.textView.setDoubleClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                with(Manifest.permission.BLUETOOTH_CONNECT)
                    .setDialog { context, explainTime, _ ->
                        val chose = DialogHelper.message(context)
                        when (explainTime) {
                            ExplainTime.Before -> return@setDialog null
                            ExplainTime.ForDenied -> chose.message("将要请求蓝牙权限")
                            ExplainTime.ForDeniedForever -> chose.message("将要前往设置界面, 需要手动授予蓝牙权限")
                        }
                        return@setDialog chose
                    }.request { _, _ ->
                        runOnUiThread { showInfo() }
                    }
            } else {
                toast("版本低于31, 无法请求蓝牙权限")
            }
        }
        bind.textView.setOnLongClickListener {
            with(setting).startForResult { _, _ -> runOnUiThread { showInfo() } }
            true
        }
    }

    private fun showInfo() {
        val sb = StringBuilder()
        val version = harmonyOSVersion()

        sb.append(InfoHelper.phoneDesc).newLine()
        sb.newLine()

        if (version != null) {
            sb.append("版本: $version")
        } else {
            sb.append("是否是鸿蒙: ${isHarmonyOS()}")
        }
        sb.newLine()

        sb.append("当前目标版本: ${applicationInfo.targetSdkVersion}").newLine()
        sb.append("当前编译版本: ${Build.VERSION.SDK_INT}").newLine() // 如果编译版本低于31
        sb.newLine()

        sb.append("是否有蓝牙权限: ${hadBluetoothPermission()}").newLine()
        sb.append("是否被永久拒绝: ${hadBluetoothPermission2()}").newLine()
        sb.newLine()

        sb.append("双击请求蓝牙权限, 长按前往设置")
        bind.set(sb)
    }

    private fun hadBluetoothPermission2(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 这个判断对大多数手机关于蓝牙的判断无效(或许是全部)
            !shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            false
        }
    }

    private fun hadBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 权限判断只能用PermissionChecker不是使用context版本的
            PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PermissionChecker.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun StringBuilder.newLine() {
        append("\n")
    }

    private fun isHarmonyOS(): Boolean {
        return try {
            val exClass = Class.forName("com.huawei.system.BuildEx")
            val brand = exClass.getMethod("getOsBrand").invoke(exClass) ?: return false
            "Harmony".equals(brand.toString(), true)
        } catch (e: Exception) {
            false
        }
    }

    private fun harmonyOSVersion(): String? {
        val prop = "hw_sc.build.platform.version"
        try {
            val spClass = Class.forName("android.os.SystemProperties")
            val value = spClass.getDeclaredMethod("get", String::class.java).invoke(spClass, prop)
            if (value is String && value.isNotEmpty()) {
                return value
            }
        } catch (_: Exception) {

        }
        return null
    }
}