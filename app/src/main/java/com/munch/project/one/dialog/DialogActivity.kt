package com.munch.project.one.dialog

import android.os.Bundle
import com.munch.lib.android.dialog.DialogManager
import com.munch.lib.android.dialog.DialogManagerImp
import com.munch.lib.android.extend.fmt
import com.munch.lib.fast.view.dialog.DialogHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.fastview.fvLlBtn
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef

class DialogActivity : BaseActivity(),
    ActivityDispatch by dispatchDef(),
    DialogManager by DialogManagerImp() {

    private val bind by fvLlBtn("message", "message2", "", "bottom")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.clickByStr {
            when (it) {
                "message" -> message()
                "message2" -> message2()
                "loading" -> bottom()
            }
        }
    }

    private fun bottom() {
        DialogHelper.bottom()
    }

    private fun message2() {
        DialogHelper
            .message("现在是: ${System.currentTimeMillis().fmt()}")
            .show()
    }

    private fun message() {
        DialogHelper.message()
            .title("当前时间")
            .message("现在是: ${System.currentTimeMillis().fmt()}")
            .show()
    }

}
