package com.munch.lib.fast.view

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.munch.lib.extend.BindBottomSheetDialogFragment
import com.munch.lib.fast.R
import com.munch.lib.fast.base.DataHelper
import com.munch.lib.fast.databinding.LayoutConfigDialogBinding
import com.munch.lib.log.log

/**
 * Created by munch1182 on 2022/4/17 2:15.
 */
class ConfigDialog(private val clazz: String) : BindBottomSheetDialogFragment() {

    constructor(activity: Activity) : this(activity::class.java.canonicalName!!)

    private val bind by bind<LayoutConfigDialogBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind.configStartUp.setOnClickListener { saveStartUp() }

        DataHelper.getStartUp()?.let { it.canonicalName == clazz }?.let {
            bind.configStartUp.isChecked = it
        }
    }

    private fun saveStartUp() {
        DataHelper.saveStartUp(clazz)
    }

    override fun getTheme() = R.style.App_Fast_Dialog_Bottom
}

interface ISupportConfigDialog : ActivityDispatch {

    override fun onCreateActivity(activity: AppCompatActivity) {
        super.onCreateActivity(activity)

        val config = ConfigDialog(activity::class.java.canonicalName!!)

        // TODO: 改为手势操作
        /*activity.window.decorView.rootView.setOnClickListener {

        }*/
        config.show(activity.supportFragmentManager, null)
    }
}

object SupportConfigDialog : ISupportConfigDialog {

    override val list: MutableList<ActivityDispatch> = mutableListOf()
}