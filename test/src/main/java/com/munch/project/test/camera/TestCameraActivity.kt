package com.munch.project.test.camera

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.camera.core.AspectRatio
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.munch.lib.log
import com.munch.project.test.BaseActivity
import com.munch.project.test.R
import com.permissionx.guolindev.PermissionX

/**
 * Create by munch on 2020/12/7 17:41.
 */
class TestCameraActivity : BaseActivity() {

    private val helper by lazy { CameraXHelper(this, findViewById(R.id.camera_preview)) }
    private val takePhoto by lazy { findViewById<ImageButton>(R.id.camera_take_photo) }
    private val switchCamera by lazy { findViewById<ImageButton>(R.id.camera_switch) }
    private val thumbnail by lazy { findViewById<ImageButton>(R.id.camera_thumbnail) }
    private val ratio by lazy { findViewById<ImageButton>(R.id.camera_ratio) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        val parameter = CameraXHelper.Parameter()
        helper.setParameter(parameter.apply {
            aspectRatio = AspectRatio.RATIO_4_3
        })
            .setListener(object : CameraXHelper.CameraListener {
                override fun cameraCannotUse(flag: Int) {
                    log("cameraCannotUse：$flag")
                    toast("相机不可用")
                }

                override fun onCameraIsReady() {
                    log("onCameraIsReady")
                }

                override fun onImageSaved(uri: Uri?) {
                    log("onImageSaved：$uri")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        helper.showEffect(takePhoto.parent as View)
                    }
                    cameraIsUsed(false)
                    Glide.with(this@TestCameraActivity)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(thumbnail)
                }

                override fun onError(e: Exception?) {
                    e?.printStackTrace()
                    log("onError")
                }
            })
        val permissions = arrayListOf(android.Manifest.permission.CAMERA)
        PermissionX.init(this)
            .permissions(permissions)
            .onForwardToSettings { scope, _ ->
                scope.showForwardToSettingsDialog(
                    permissions,
                    "需要以下权限",
                    "前往",
                    "取消"
                )
            }
            .request { allGranted, _, _ ->
                if (allGranted) {
                    helper.startCamera()
                } else {
                    toast("拒绝了权限")
                }
            }
        takePhoto.setOnClickListener {
            helper.takePicture()
            cameraIsUsed(true)
        }
        switchCamera.setOnClickListener {
            helper.switchCamera()
        }
        ratio.setOnClickListener {
            helper.setParameter(parameter.apply {
                if (aspectRatio == AspectRatio.RATIO_4_3) {
                    aspectRatio = AspectRatio.RATIO_16_9
                    toast("16:9")
                } else {
                    aspectRatio = AspectRatio.RATIO_4_3
                    toast("4:3")
                }
            }).startCamera()
        }
    }

    private fun cameraIsUsed(taking: Boolean) {
        takePhoto.isEnabled = !taking
        switchCamera.isEnabled = !taking
    }
}