package com.munch.lib.helper

import android.content.Context
import android.net.Uri
import androidx.loader.content.CursorLoader


/**
 * Create by munch1182 on 2021/1/1 1:05.
 */
object FileHelper {

    fun getPathFromUri(context: Context, type: String, uri: Uri): String? {
        val loader = CursorLoader(
            context, uri, arrayOf(type),
            null,
            null,
            null
        )
        val cursor = loader.loadInBackground() ?: return null
        val columnIndex =
            cursor.getColumnIndex(type).takeIf { it != -1 } ?: return null
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }
}