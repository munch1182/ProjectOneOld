package com.munch.project.one.dialog

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.munch.lib.android.dialog.BaseDialog
import com.munch.lib.android.extend.ctx
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef

class DialogActivity : BaseActivity(), ActivityDispatch by dispatchDef() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DialogPermission(ctx)
            .show()
    }

    private class DialogPermission(private val context: Context) : BaseDialog() {

        private lateinit var dialog: AlertDialog

        override fun onCreateDialog(context: Context) {
            dialog = AlertDialog.Builder(context)
                .setView(TextView(context))
                .create()

        }

        override fun show() {
            super.show()
            onCreateDialog(context)
            dialog.show()
        }

        override fun cancel() {
            super.cancel()
            dialog.cancel()
        }
    }
}