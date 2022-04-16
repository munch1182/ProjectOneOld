package com.munch.project.one.contentresolver

import android.Manifest
import android.os.Build
import android.os.Bundle
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ISupportActionBar
import com.munch.lib.fast.view.fvFv
import com.munch.lib.log.log
import com.munch.lib.result.OnPermissionResultListener
import com.munch.lib.result.ResultHelper

/**
 * Create by munch1182 on 2022/4/16 11:06.
 */
class ContentResolverActivity : BaseFastActivity(), ISupportActionBar {

    private val bind by fvFv(
        arrayOf(
            "listen",
            "not listen",
            "",
            "listen2",
            "not listen2",
            "",
            "register",
            "unregister"
        )
    )
    private val call by lazy { CallObserver(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bind.init()
        val listener = object : OnPermissionResultListener {
            override fun onPermissionResult(
                isGrantAll: Boolean,
                grantedArray: Array<String>,
                deniedArray: Array<String>
            ) {
                log(isGrantAll, grantedArray, deniedArray)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ResultHelper.with(this)
                .permission(
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_PHONE_NUMBERS
                ).request(listener)
        } else {
            ResultHelper.with(this)
                .permission(
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_PHONE_STATE
                ).request(listener)
        }
        bind.click { _, index ->
            when (index) {
                0 -> call.listener()
                1 -> call.noListener()
                2 -> call.listenerOnS()
                3 -> call.unListenerOnS()
                4 -> call.register()
                5 -> call.unregister()
                else -> {}
            }
        }
    }
}