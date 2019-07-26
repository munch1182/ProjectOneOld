package com.munhc.lib.libnative.root

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.munhc.lib.libnative.backpressed.FragmentBackPressedHelper

/**
 * Created by Munch on 2019/7/13 13:58
 */
open class RootActivity : AppCompatActivity(), INext {

    override fun getIntent(): Intent {
        return super.getIntent() ?: Intent()
    }

    fun getBundle() = super.getIntent()?.extras ?: Bundle()

    override fun onBackPressed() {
        if (judgeHandleByFragment()) {
            //由Fragment控制返回
            if (FragmentBackPressedHelper.handleBackPressed(this)) {
                return
            }
        }
        super.onBackPressed()
    }

    /**
     * 用于控制是否使用[com.munhc.lib.libnative.backpressed.FragmentBackPressedHelper]判断返回，默认不判断
     */
    open fun judgeHandleByFragment(): Boolean {
        return false
    }

}