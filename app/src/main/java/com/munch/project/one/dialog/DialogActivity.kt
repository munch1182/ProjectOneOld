package com.munch.project.one.dialog

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentDialog
import androidx.appcompat.app.AlertDialog
import com.munch.lib.android.dialog.ChoseDialogWrapper
import com.munch.lib.android.dialog.DialogManager
import com.munch.lib.android.dialog.DialogManagerImp
import com.munch.lib.android.extend.bind
import com.munch.lib.android.log.log
import com.munch.lib.android.result.ExplainTime
import com.munch.lib.android.result.ResultHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.databinding.ActivityDialogBinding

class DialogActivity : BaseActivity(),
    ActivityDispatch by dispatchDef(),
    DialogManager by DialogManagerImp() {

    private val bind by bind<ActivityDialogBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.permission.setOnClickListener { showPermission() }
    }

    private fun showPermission() {
        ResultHelper.with(this)
            .permission(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CAMERA,
                Manifest.permission.CALL_PHONE
            )
            .attach(this)
            .setDialog { context, explainTime, strings ->
                when (explainTime) {
                    ExplainTime.Before -> PD(context, explainTime, strings)
                    ExplainTime.ForDenied -> PD(context, explainTime, strings)
                    ExplainTime.ForDeniedForever -> PD(context, null, strings)
                }
            }
            .request { _, map ->
                map.forEach { (s, b) -> log(s, b) }
            }
    }

    private class PD(
        private val context: Context,
        private val explainTime: ExplainTime?,
        private val p: Array<String>
    ) : ChoseDialogWrapper() {

        override fun crateDialog(): ComponentDialog {
            return AlertDialog.Builder(context)
                .setTitle(if (explainTime == null) "前往设置" else "权限请求: $explainTime")
                .setMessage(p.joinToString { "$it\n" })
                .setPositiveButton("请求") { _, _ -> choseNext() }
                .setNegativeButton("取消") { _, _ -> choseCancel() }
                .create()
        }
    }

}
