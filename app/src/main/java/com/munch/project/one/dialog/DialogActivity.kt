package com.munch.project.one.dialog

import android.os.Bundle
import com.munch.lib.android.dialog.DefaultDialogManager
import com.munch.lib.android.dialog.IDialogManager
import com.munch.lib.android.dialog.offer
import com.munch.lib.android.extend.fmt
import com.munch.lib.fast.view.dialog.DialogHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.fastview.fvFvBtn
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef

class DialogActivity : BaseActivity(),
    ActivityDispatch by dispatchDef(),
    IDialogManager by DefaultDialogManager() {

    private val bind by fvFvBtn(
        "message", "message2", "",
        "bottom", "bottom2", "",
        "offer"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.clickByStr {
            when (it) {
                "message" -> message()
                "message2" -> message2()
                "bottom" -> bottom()
                "bottom2" -> bottom2()
                "offer" -> offer()
            }
        }
    }

    private fun offer() {
        message()
        bottom()
        bottom2()
        message()
    }

    private fun bottom2() {
    }

    private fun bottom() {
        DialogHelper.bottom()
            .title("Title")
            .content("curr: ${System.currentTimeMillis().fmt()}")
            .ok()
            .cancel()
            .offer(this)
            .show()
    }

    private fun message2() {
        DialogHelper
            .message("curr: ${System.currentTimeMillis().fmt()}")
            .offer(this)
            .show()
    }

    private fun message() {
        DialogHelper.message()
            .title("Title")
            .message("curr: ${System.currentTimeMillis().fmt()}")
            .offer(this)
            .show()
    }

}