package com.munch.project.one

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.munch.lib.extend.bind
import com.munch.lib.extend.init
import com.munch.lib.result.OnIntentResultListener
import com.munch.lib.result.intent
import com.munch.project.one.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val vb by bind<ActivityMainBinding>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vb.init()
        /*vb.text.text = "${BuildConfig.BUILD_TYPE}\n${BuildConfig.APPLICATION_ID}\n" +
                "${BuildConfig.VERSION_CODE}\n${BuildConfig.VERSION_NAME}\n" +
                "${BuildConfig.VERSION_DESC}\n${BuildConfig.BUILD_TIME}\n" +
                "${Build.VERSION.SDK_INT}"*/
//        replace(R.id.fl, TestFragment())
//        ResultHelper.with(this)
//            .permission(Manifest.permission.CAMERA)
//            .request(object : OnPermissionResultListener {
//                override fun onPermissionResult(
//                    isGrantAll: Boolean,
//                    grantedArray: Array<String>,
//                    deniedArray: Array<String>
//                ) {
//
//                }
//            })
    }
}