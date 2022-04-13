package com.munch.project.one

import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.munch.lib.UnSupportException
import com.munch.lib.log.log
import com.munch.lib.task.ITask
import com.munch.lib.task.TaskHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/3/31 22:39.
 */
class RvActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            addView(TextDrawDiagramView(this@RvActivity))
        })

    }
}
