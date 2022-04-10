package com.munch.project.one

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.munch.lib.log.log
import com.munch.lib.result.OnPermissionResultListener
import com.munch.lib.result.ResultHelper

/**
 * Create by munch1182 on 2022/3/31 22:39.
 */
class RvActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResultHelper.with(this)
            .permission(Manifest.permission.CAMERA)
            .request(object : OnPermissionResultListener {
                override fun onPermissionResult(
                    isGrantAll: Boolean,
                    grantedArray: Array<String>,
                    deniedArray: Array<String>
                ) {
                    log(isGrantAll, grantedArray, deniedArray)
                }
            })
    }
}
