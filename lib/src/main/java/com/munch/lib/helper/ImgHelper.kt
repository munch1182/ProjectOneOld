package com.munch.lib.helper

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.IntRange
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Create by munch1182 on 2020/12/31 21:58.
 */
object ImgHelper {

    /**
     * 需要在manifest中声明provider
     */
    fun getUri(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileProvider", file)
        } else {
            Uri.fromFile(file)
        }
    }

    fun albumIntent() = Intent(Intent.ACTION_PICK, null)
        .setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")

    fun imageCaptureIntent(context: Context, file: File) = imageCaptureIntent(getUri(context, file))

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

    fun imgCompress(
        file: File, newFile: File, @IntRange(from = 0, to = 100) quality: Int = 30,
        sampleSize: Int = 2
    ) = imgCompress(file.absolutePath, newFile, quality, sampleSize)

    /**
     *@param quality  质量压缩，只会压缩图片的大小，不会压缩像素，所以加载内存不会降低
     * 0-100 100为不压缩
     * @param sampleSize 采样率压缩，设置图片的采样率，降低图片像素，数值越高，图片像素越低
     * 不压缩则为1
     */
    fun imgCompress(
        filePath: String,
        newFile: File,
        @IntRange(from = 0, to = 100) quality: Int = 30,
        sampleSize: Int = 2
    ): File {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false//为true的时候不会真正加载图片，而是得到图片的宽高信息。
        options.inSampleSize = sampleSize
        val bitmap = BitmapFactory.decodeFile(filePath, options)
        val baos = ByteArrayOutputStream()
        // 把压缩后的数据存放到baos中
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        var fos: FileOutputStream? = null
        try {
            if (newFile.exists()) {
                newFile.delete()
            } else {
                newFile.createNewFile()
            }
            fos = FileOutputStream(newFile)
            fos.write(baos.toByteArray())
            fos.flush()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            fos?.close()
        }
        return newFile
    }
}