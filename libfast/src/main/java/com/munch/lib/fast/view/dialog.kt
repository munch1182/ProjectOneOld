package com.munch.lib.fast.view

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.munch.lib.fast.R
import com.munch.lib.fast.base.BindBottomSheetDialogFragment
import com.munch.lib.fast.base.DataHelper
import com.munch.lib.fast.databinding.LayoutConfigDialogBinding

/**
 * Created by munch1182 on 2022/4/17 2:15.
 */
class ConfigDialog(private val clazz: String) : BindBottomSheetDialogFragment() {

    constructor(activity: Activity) : this(activity::class.java.canonicalName!!)

    private val bind by bind<LayoutConfigDialogBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind.configStartUp.setOnCheckedChangeListener { _, isChecked -> saveStartUp(isChecked) }

        DataHelper.getStartUp()?.let { it.canonicalName == clazz }
            ?.let { bind.configStartUp.isChecked = it }
    }

    private fun saveStartUp(save: Boolean) {
        DataHelper.saveStartUp(if (save) clazz else null)
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

    override val dispatchers: MutableList<ActivityDispatch> = mutableListOf()
}