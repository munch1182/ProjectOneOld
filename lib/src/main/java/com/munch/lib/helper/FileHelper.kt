package com.munch.lib.helper

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.loader.content.CursorLoader


/**
 * Create by munch1182 on 2021/1/1 1:05.
 */
object FileHelper {

    fun getPathFromUri(uri: Uri, context: Context): String? {
        val elements = MediaStore.Images.Media.DATA
        val loader = CursorLoader(
            context, uri, arrayOf(elements),
            null,
            null,
            null
        )
        val cursor = loader.loadInBackground() ?: return null
        val columnIndex =
            cursor.getColumnIndex(elements).takeIf { it != -1 } ?: return null
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }
}