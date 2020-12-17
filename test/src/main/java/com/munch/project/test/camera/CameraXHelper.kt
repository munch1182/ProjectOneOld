package com.munch.project.test.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.view.View
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

/**
 * Create by munch1182 on 2020/12/7 17:52.
 */
class CameraXHelper constructor(
    private val context: Context,
    private val owner: LifecycleOwner,
    private val previewView: PreviewView
) {

    private var lensFacing = CAMERA_NO
    private var canSwitchCamera = false
    private var cameraIsReady = false

    companion object {
        private const val CAMERA_NO = 2
        private const val CAMERA_BACK = CameraSelector.LENS_FACING_BACK
        private const val CAMERA_FRONT = CameraSelector.LENS_FACING_FRONT
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val ANIMATION_FAST_MILLIS = 50L
        private const val ANIMATION_SLOW_MILLIS = 100L
        private const val PATTERN_TIME = "yyyyMMdd_HHmmss"

        const val FLAG_ERROR_NO_CAMERA = 0
        const val FLAG_ERROR = 1
        const val FLAG_ERROR_NO_PERMISSION = 2
        const val FLAG_ERROR_NULL = 3
        const val FLAG_SCAN_COMPLETED = 4
    }

    private var imageCapture: ImageCapture? = null
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    private var listener: CameraListener? = null

    private var parameter: Parameter? = null
    private val outputDir by lazy { getOutputDirectory() }

    constructor(
        context: ComponentActivity,
        previewView: PreviewView
    ) : this(context, context, previewView)

    constructor(
        context: Fragment,
        previewView: PreviewView
    ) : this(context.context ?: throw NullPointerException(), context, previewView)

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun startCamera() {
        if (!hasPermission()) {
            notifyCaller(FLAG_ERROR_NO_PERMISSION, null)
            return
        }
        //必须等camera_preview就绪才能继续，否则后台重启会有空指针
        previewView.post {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(Runnable {
                val cameraProvider: ProcessCameraProvider
                cameraProvider = try {
                    cameraProviderFuture.get()
                } catch (e: Exception) {
                    notifyCaller(FLAG_ERROR, e)
                    return@Runnable
                }
                if (lensFacing == CAMERA_NO) {
                    judgeCamera(cameraProvider)
                }
                if (lensFacing == CAMERA_NO) {
                    notifyCaller(FLAG_ERROR_NO_CAMERA, null)
                    return@Runnable
                }
                val rotation = parameter?.rotation ?: previewView.display.rotation
                val metrics = DisplayMetrics()
                previewView.display.getRealMetrics(metrics)
                val aspectRatio = parameter?.aspectRatio ?: aspectRatio(metrics)
                val preview = Preview.Builder()
                    .setTargetRotation(parameter?.rotation ?: rotation)
                    .apply {
                        val resolution = parameter?.resolution
                        //二者不能并存
                        if (resolution != null) {
                            setTargetResolution(resolution)
                        } else {
                            setTargetAspectRatio(parameter?.aspectRatio ?: aspectRatio)
                        }
                    }
                    .build()
                val camera =
                    if (lensFacing == CAMERA_BACK) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
                val cameraSelector = CameraSelector.Builder().requireLensFacing(camera).build()
                imageCapture = ImageCapture.Builder() //分辨率
                    .setCaptureMode(
                        parameter?.captureMode ?: ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
                    )
                    .setTargetRotation(parameter?.rotation ?: rotation)
                    .setFlashMode(parameter?.flashMode ?: ImageCapture.FLASH_MODE_AUTO)
                    .apply {
                        val resolution = parameter?.resolution
                        //二者不能并存
                        if (resolution != null) {
                            setTargetResolution(resolution)
                        } else {
                            setTargetAspectRatio(aspectRatio)
                        }
                    }
                    .build()
                cameraProvider.unbindAll()
                try {
                    cameraProvider.bindToLifecycle(
                        owner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                } catch (e: Exception) {
                    notifyCaller(FLAG_ERROR, e)
                    return@Runnable
                }
                cameraIsReady = true
                listener?.onCameraIsReady()
            }, ContextCompat.getMainExecutor(context))
        }
    }

    /**
     * 切换镜头
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun switchCamera() {
        if (errorNoReady()) {
            return
        }
        if (!canSwitchCamera) {
            notifyCaller(FLAG_ERROR_NO_CAMERA, Exception("不能转换镜头"))
            return
        }
        if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            lensFacing = CameraSelector.LENS_FACING_FRONT
        } else if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            lensFacing = CameraSelector.LENS_FACING_BACK
        }
        startCamera()
    }

    private fun errorNoReady(): Boolean {
        if (!cameraIsReady) {
            notifyCaller(FLAG_ERROR, Exception("相机还未准备好"))
            return true
        }
        return false
    }

    fun setListener(listener: CameraListener?): CameraXHelper {
        this.listener = listener
        return this
    }

    /**
     * 在[CameraListener.onCameraIsReady]之后调用
     */
    fun canSwitchCamera(): Boolean {
        return if (errorNoReady()) {
            false
        } else canSwitchCamera
    }

    /**
     * 拍照方法
     */
    fun takePicture() {
        if (errorNoReady()) {
            return
        }
        if (!hasPermission()) {
            notifyCaller(FLAG_ERROR_NO_PERMISSION, null)
            return
        }
        if (imageCapture == null) {
            notifyCaller(FLAG_ERROR_NULL, null)
            return
        }
        val name =
            "IMG_" + SimpleDateFormat(PATTERN_TIME, Locale.getDefault())
                .format(System.currentTimeMillis()) + ".jpg"
        val file = File(parameter?.dir ?: outputDir, name)
        val fileOptions =
            ImageCapture.OutputFileOptions.Builder(file)
                .setMetadata(ImageCapture.Metadata())
                .build()
        imageCapture!!.takePicture(
            fileOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val uri =
                        if (outputFileResults.savedUri == null) Uri.fromFile(file) else outputFileResults.savedUri!!
                    previewView.post {
                        listener?.onImageSaved(uri)
                        if (parameter == null || parameter?.notifySystem == true) {
                            notifySystem(uri)
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    notifyCaller(FLAG_ERROR, exception)
                }
            })
    }

    private fun notifySystem(uri: Uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            context.sendBroadcast(Intent("android.hardware.action.NEW_PICTURE", uri))
        }
        MediaScannerConnection.scanFile(
            context,
            arrayOf(uri.path),
            arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension(uri.toFile().extension))
        ) { _, _ ->
            notifyCaller(FLAG_SCAN_COMPLETED, null)
        }
    }

    private fun getOutputDirectory(): File? {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, context.packageName).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else context.filesDir
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun showEffect(container: View) {
        container.postDelayed({
            container.foreground = ColorDrawable(Color.WHITE)
            container.postDelayed(
                { container.foreground = null }, ANIMATION_FAST_MILLIS
            )
        }, ANIMATION_SLOW_MILLIS)
    }

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun aspectRatio(metrics: DisplayMetrics): Int {
        val previewRatio = metrics.widthPixels.coerceAtLeast(metrics.heightPixels) * 1.0 /
                metrics.widthPixels.coerceAtMost(metrics.heightPixels)
        return if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            AspectRatio.RATIO_4_3
        } else AspectRatio.RATIO_16_9
    }

    private fun judgeCamera(cameraProvider: ProcessCameraProvider) {
        //有后置镜头
        when {
            hasBackCamera(cameraProvider) -> {
                lensFacing = CAMERA_BACK
                //能切换镜头
                canSwitchCamera = hasFrontCamera(cameraProvider)
                //没有后置镜头判断前置摄像头
            }
            hasFrontCamera(cameraProvider) -> {
                lensFacing = CAMERA_FRONT
                //不能切换镜头
                canSwitchCamera = false
            }
            else -> {
                lensFacing = CAMERA_FRONT
                canSwitchCamera = false
            }
        }

        if (parameter != null && canSwitchCamera) {
            lensFacing = parameter!!.lensFacing
        }
    }

    private fun notifyCaller(flag: Int, e: Exception?) {
        previewView.post {
            when (flag) {
                FLAG_ERROR_NO_CAMERA, FLAG_ERROR_NO_PERMISSION ->
                    listener?.cameraCannotUse(flag)
                FLAG_ERROR, FLAG_ERROR_NULL ->
                    listener?.onError(e)
                FLAG_SCAN_COMPLETED -> {
                }
            }
        }
    }

    private fun hasBackCamera(cameraProvider: ProcessCameraProvider): Boolean {
        return try {
            cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        } catch (e: CameraInfoUnavailableException) {
            false
        }
    }

    private fun hasFrontCamera(cameraProvider: ProcessCameraProvider): Boolean {
        return try {
            cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        } catch (e: CameraInfoUnavailableException) {
            false
        }
    }

    /**
     * 在{@see #startCamera()}方法之前调用即可改变摄像头参数
     */
    fun setParameter(parameter: Parameter): CameraXHelper {
        this.parameter = parameter
        return this
    }

    class Parameter {

        /**
         * @see ImageCapture.FLASH_MODE_AUTO
         * @see ImageCapture.FLASH_MODE_OFF
         * @see ImageCapture.FLASH_MODE_ON
         */
        val flashMode: Int = ImageCapture.FLASH_MODE_AUTO

        /**
         * @see ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
         * @see ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
         */
        var captureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY

        /**
         * 拍照方向，默认随手机方向
         * @see android.view.Surface.ROTATION_0
         * @see android.view.Surface.ROTATION_90
         * @see android.view.Surface.ROTATION_180
         * @see android.view.Surface.ROTATION_270
         */
        var rotation: Int? = null

        /**
         * 长宽比，4：3 / 16：9
         * @see AspectRatio.RATIO_4_3
         * @see AspectRatio.RATIO_16_9
         */
        var aspectRatio: Int? = null

        /**
         * 解析度
         */
        var resolution: Size? = null

        /**
         * 首选镜头，如不可用则无效
         * @see CameraXHelper.CAMERA_FRONT
         * @see CameraXHelper.CAMERA_BACK
         */
        var lensFacing: Int = CAMERA_BACK

        /**
         * 存储位置
         */
        var dir: File? = null

        /**
         * 拍照完通知系统使图片在系统中可见
         */
        var notifySystem = true

    }

    interface CameraListener {
        /**
         * @param flag [CameraXHelper.FLAG_ERROR_NO_CAMERA][CameraXHelper.FLAG_ERROR_NO_PERMISSION]
         */
        fun cameraCannotUse(flag: Int)
        fun onCameraIsReady()
        fun onImageSaved(uri: Uri?)
        fun onError(e: Exception?)
    }
}