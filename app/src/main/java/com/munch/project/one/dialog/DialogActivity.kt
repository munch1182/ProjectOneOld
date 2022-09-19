package com.munch.project.one.dialog

import com.munch.lib.android.dialog.DialogManager
import com.munch.lib.android.dialog.DialogManagerImp
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef

class DialogActivity : BaseActivity(),
    ActivityDispatch by dispatchDef(),
    DialogManager by DialogManagerImp() {


}
