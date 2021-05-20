package com.munch.test.project.one.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.munch.pre.lib.helper.file.StorageHelper
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityIjkPlayerBinding
import com.munch.test.project.one.player.media.MediaSetting
import com.munch.test.project.one.requestPermission


/**
 * Create by munch1182 on 2021/5/11 9:55.
 */
class IjkPlayerActivity : BaseTopActivity() {

    private val bind by bind<ActivityIjkPlayerBinding>(R.layout.activity_ijk_player)

    private val requestFile =
        registerForActivityResult(object : ActivityResultContract<String, Uri?>() {
            override fun createIntent(context: Context, input: String): Intent {
                return StorageHelper.fileIntent(input)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                return intent?.data
            }
        }) {
            it ?: return@registerForActivityResult
            bind.playerVideoView.setData(it)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        bind.playerVideoView.attachControllerView(
            DefMediaControllerView(),
            MediaSetting(autoPlay = true)
        )
        bind.playerChoseFile.setOnClickListener {
            requestPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) {
                requestFile.launch("video/*")
            }
        }
    }


}