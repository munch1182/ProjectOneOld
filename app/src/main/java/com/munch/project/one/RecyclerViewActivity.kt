package com.munch.project.one

import com.munch.lib.fast.view.ActivityDispatch
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef

class RecyclerViewActivity : BaseActivity(), ActivityDispatch by dispatchDef()