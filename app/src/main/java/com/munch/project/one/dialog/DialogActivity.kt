package com.munch.project.one.dialog

import android.os.Bundle
import com.munch.lib.android.dialog.DefaultDialogManager
import com.munch.lib.android.dialog.IDialogManager
import com.munch.lib.android.dialog.offer
import com.munch.lib.android.extend.fmt
import com.munch.lib.fast.view.dialog.*
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.fastview.fvFvBtn
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.databinding.LayoutDialogBottomTestBinding

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
        DialogHelper.bottom()
            .view<LayoutDialogBottomTestBinding>(this)
            .onShow { dialog ->
                dialogTestContent.text =
                    " DialogHelper.bottom()\n\t.view<AnyViewBind>\n\t.onShow { dialog -> \n\t\tassert(this is AnyViewBind)\n\t\tassert(dialog is IDialog)\n\t}\n\t.show()"
                dialogTestOk.setOnClickListener { dialog.dismiss() }
                dialogTestCancel.setOnClickListener { dialog.dismiss() }
            }
            .offer(this)
            .show()
    }

    private fun bottom() {
        DialogHelper.bottom()
            .titleStr("Title")
            .contentStr("curr: ${System.currentTimeMillis().fmt()}")
            .okStr()
            .cancelStr()
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
            .message(" DialogHelper.message()\n\t.title(AnyTitle)\n\t.message(AnaMessage)\n\t.show()")
            .offer(this)
            .show()
    }

}
