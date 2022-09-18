package com.munch.project.one.dialog

import android.os.Bundle
import com.munch.lib.android.dialog.DialogManager
import com.munch.lib.android.dialog.DialogManagerImp
import com.munch.lib.android.extend.bind
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
        bind.permission.setOnClickListener { }
    }

}
