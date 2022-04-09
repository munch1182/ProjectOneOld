package com.munch.project.one

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.munch.lib.android.base.OnUpdate
import com.munch.lib.android.helper.ARSHelper
import com.munch.lib.android.helper.IARSHelper
import com.munch.lib.android.log.log
import com.munch.lib.android.task.ThreadHandler
import java.util.concurrent.TimeUnit

/**
 * Create by munch1182 on 2022/3/31 22:39.
 */
class RvActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextDrawDiagramView(this).apply {
        })
    }
}
