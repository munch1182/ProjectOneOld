package com.munch.project.one

import android.os.Bundle
import android.widget.TextView
import androidx.core.view.setPadding
import com.munch.lib.android.extend.dp2Px2Int
import com.munch.lib.android.extend.newMWLP
import com.munch.project.one.base.BaseActivity

class PhoneInfoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            layoutParams = newMWLP
            setPadding(16.dp2Px2Int())
        })
    }
}