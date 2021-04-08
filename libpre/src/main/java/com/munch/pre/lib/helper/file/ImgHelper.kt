@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.pre.lib.helper.file

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.IntRange
import androidx.core.content.FileProvider
import com.munch.pre.lib.DefaultDepend
import com.munch.pre.lib.base.BaseApp
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Create by munch1182 on 2020/12/31 21:58.
 */
@DefaultDepend([BaseApp::class])
object ImgHelper {

    /**
     * @param outputUri 如不设置，则从[Intent.getExtras]和[android.os.Bundle.getParcelable("data)]中获取Bitmap
     * 注意，如图片过大通过intent传递可能会崩溃
     * 此uri不能使用[FileProvider]转化的uri，且在系统裁剪能够访问的范围内
     * @param output 输出宽高 , 如[400/800] [1080/1920]
     * @param aspect 缩放比例 , 如[1/1]
     */
    fun getCorpIntent(
        uri: Uri,
        outputUri: Uri? = null,
        output: String? = null,
        aspect: String? = null,
        scale: Boolean = true
    ): Intent {
        val split = "/"
        return Intent("com.android.camera.action.CROP").apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } else {
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            setDataAndType(uri, "image/*")
            if (output != null && output.contains(split)) {
                val split1 = output.split(split)
                putExtra("outputX", split1[0].toInt())
                putExtra("outputY", split1[1].toInt())
            }
            if (aspect != null && aspect.contains(split)) {
                val split1 = aspect.split(split)
                putExtra("aspectX", split1[0].toInt())
                putExtra("aspectY", split1[1].toInt())
            }
            putExtra("scale", scale)
            putExtra("scaleUpIfNeeded", scale)
            putExtra("return-data", outputUri == null)
            putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            if (outputUri != null) {
                putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            }
        }
    }

    /**
     * 通过[Intent.getData]获取Result
     */
    fun albumIntent() = Intent(Intent.ACTION_PICK, null)
        .setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")

    fun imageCaptureIntent(context: Context, file: File) =
        imageCaptureIntent(file.toUriCompat(context))

    fun imageCaptureIntent(uri: Uri): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } else {
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }
    }

    /**
     * 当同时传递opts和quality时，先按照opts生成bitmap，再按照quality压缩
     */
    fun res2File(
        resId: Int,
        newFile: File,
        resources: Resources = BaseApp.getInstance().resources,
        @IntRange(from = 0, to = 100) quality: Int = 100,
        opts: BitmapFactory.Options? = null
    ) = bitmap2File(
        BitmapFactory.decodeResource(resources, resId, opts),
        newFile,
        quality
    )


    /**
     * 将bitmap转为file文件
     * 默认不压缩
     */
    fun bitmap2File(
        bitmap: Bitmap, newFile: File, @IntRange(from = 0, to = 100) quality: Int = 100
    ): File? {
        val file = newFile.newFile() ?: return null
        var fos: FileOutputStream? = null
        val baos = ByteArrayOutputStream()
        // 把压缩后的数据存放到baos中
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        try {
            fos = FileOutputStream(file)
            fos.write(baos.toByteArray())
            fos.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return file
    }

    fun imgCompress(
        file: File, newFile: File, @IntRange(from = 0, to = 100) quality: Int = 30,
        sampleSize: Int = 2
    ) = imgCompress(file.absolutePath, newFile, quality, sampleSize)

    /**
     *@param quality  质量压缩，只会压缩图片的大小，不会压缩像素，所以加载内存不会降低
     * 0-100 100为不压缩
     * @param sampleSize 采样率压缩，设置图片的采样率，降低图片像素，数值越高，图片像素越低
     * 必须是2的倍数，小于1则为1，不压缩则为1
     * @see android.graphics.BitmapFactory.Options.inSampleSize
     */
    fun imgCompress(
        filePath: String,
        newFile: File,
        @IntRange(from = 0, to = 100) quality: Int = 30,
        sampleSize: Int = 2
    ): File? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false//为true的时候不会真正加载图片，而是得到图片的宽高信息。
        options.inSampleSize = sampleSize
        val bitmap = BitmapFactory.decodeFile(filePath, options)
        return bitmap2File(bitmap, newFile, quality)
    }

}