@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.InputStream

/**
 * 适配android10和android11的作用域存储ScopedStorage
 *
 * 建议使用android:requestLegacyExternalStorage关闭android10的作用域存储避开android10与android11的差异
 *
 * 在ScopedStorage中，图片、音频、视频使用MediaStore访问，其余则需要使用系统文件选择器访问
 *
 * Create by munch1182 on 2021/2/23 9:16.
 */
object StorageHelper {

    /**
     * 调用系统文件选择器的intent
     */
    fun fileIntent(type: String = "*/*") =
        Intent(Intent.ACTION_GET_CONTENT).setType(type).addCategory(Intent.CATEGORY_OPENABLE)

    /**
     * @see android.Manifest.permission.READ_EXTERNAL_STORAGE
     */
    fun queryImages(context: Context, sortOrder: String? = null) =
        queryAll(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, sortOrder)

    /**
     * @see android.Manifest.permission.READ_EXTERNAL_STORAGE
     */
    fun queryVideos(context: Context, sortOrder: String? = null) =
        queryAll(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, sortOrder)

    /**
     * @see android.Manifest.permission.READ_EXTERNAL_STORAGE
     */
    fun queryAudio(context: Context, sortOrder: String? = null) =
        queryAll(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, sortOrder)

    /**
     * 根据查询条件查询uri
     *
     * @param uri [MediaStore.Images.Media.EXTERNAL_CONTENT_URI]/[MediaStore.Video.Media.EXTERNAL_CONTENT_URI]/[MediaStore.Audio.Media.EXTERNAL_CONTENT_URI]
     * @param sortOrder 排序方式，例如："${MediaStore.MediaColumns.DATE_ADDED} desc"
     */
    fun queryAll(
        context: Context,
        uri: Uri,
        sortOrder: String?,
        selection: String? = null,
        selectionArgs: Array<String>? = null
    ): ArrayList<Uri> {
        val list = arrayListOf<Uri>()
        context.contentResolver.query(uri, null, selection, selectionArgs, sortOrder)?.apply {
            while (this.moveToNext()) {
                val id = this.getLong(this.getColumnIndex(MediaStore.MediaColumns._ID))
                list.add(ContentUris.withAppendedId(uri, id))
            }
        }?.close()
        return list
    }

    /**
     * 将图片uri转为bitmap
     */
    fun uri2Bitmap(context: Context, uri: Uri): Bitmap? {
        var bitmap: Bitmap? = null
        context.contentResolver.openFileDescriptor(uri, "r")?.apply {
            bitmap = BitmapFactory.decodeFileDescriptor(this.fileDescriptor)
        }?.close()
        return bitmap
    }

    /**
     * 将ins插入相册
     */
    fun insertAlbum(context: Context, ins: InputStream, displayName: String, mimeType: String) =
        insert(
            context, ins,
            displayName, mimeType,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Environment.DIRECTORY_DCIM
        )

    /**
     * 将ins插入图片
     */
    fun insertPictures(context: Context, ins: InputStream, displayName: String, mimeType: String) =
        insert(
            context, ins,
            displayName, mimeType,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Environment.DIRECTORY_PICTURES
        )

    /**
     * 将ins插入音乐
     */
    fun insertMusic(context: Context, ins: InputStream, displayName: String, mimeType: String) =
        insert(
            context, ins,
            displayName, mimeType,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Environment.DIRECTORY_MUSIC
        )

    /**
     * 将ins插入视频
     */
    fun insertVideo(context: Context, ins: InputStream, displayName: String, mimeType: String) =
        insert(
            context, ins,
            displayName, mimeType,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Environment.DIRECTORY_MOVIES
        )


    /**
     * @param ins 要插入的文件对象流，方法内已关闭
     * @param displayName 显示名称
     * @param mimeType 文件类型
     * @param uriPath 文件要存入的uri类型
     * @param path 文件在30版本要存入的位置，是相对位置  [Environment.DIRECTORY_DCIM]/[Environment.DIRECTORY_PICTURES]/[Environment.DIRECTORY_MOVIES]/[Environment.DIRECTORY_MUSIC]
     *
     * 该方法无需权限
     */
    fun insert(
        context: Context, ins: InputStream,
        displayName: String, mimeType: String, uriPath: Uri, path: String
    ): Uri? {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //30放入相对位置
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, path)
        } else {
            //30以下需要放入绝对位置
            @Suppress("DEPRECATION")
            values.put(
                MediaStore.MediaColumns.DATA,
                "${Environment.getExternalStorageDirectory().path}/$path/$displayName"
            )
        }
        val uri = context.contentResolver.insert(uriPath, values)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.run {
                ins.copyAndClose(this)
            }
        }
        ins.closeQuietly()
        return uri
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun insertDownload(context: Context, ins: InputStream, displayName: String, mimeType: String) {
        insert(
            context, ins,
            displayName, mimeType,
            MediaStore.Downloads.EXTERNAL_CONTENT_URI, Environment.DIRECTORY_DOWNLOADS
        )
    }

}