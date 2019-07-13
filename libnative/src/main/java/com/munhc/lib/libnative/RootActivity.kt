package com.munhc.lib.libnative

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Munch on 2019/7/13 13:58
 */
open class RootActivity : AppCompatActivity(), INext {

    override fun getIntent(): Intent {
        return super.getIntent() ?: Intent()
    }

    fun getBundle() = super.getIntent()?.extras ?: Bundle()

}