package com.munch.lib.libnative.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

/**
 * Created by Munch on 2019/8/14 9:25
 */
object ImageHelper {


    /**
     * @param authority FileProvider authority
     * @return intent 用于startActivityForResult获取结果，结果在[file]
     */
    fun takePhoto(context: Context, file: File, authority: String): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, authority, file)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            uri = Uri.fromFile(file)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        return intent
    }
}