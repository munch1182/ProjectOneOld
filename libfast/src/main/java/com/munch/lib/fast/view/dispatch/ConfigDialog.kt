package com.munch.lib.fast.view.dispatch

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.munch.lib.android.extend.contentView
import com.munch.lib.android.extend.setDoubleClickListener
import com.munch.lib.android.extend.to
import com.munch.lib.fast.R
import com.munch.lib.fast.databinding.FragmentConfigBinding
import com.munch.lib.fast.view.DataHelper
import com.munch.lib.fast.view.base.BindBottomSheetDialogFragment
import com.munch.lib.fast.view.base.show

class ConfigDialog : BindBottomSheetDialogFragment() {

    private val bind by bind<FragmentConfigBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind.configJumpAuto.isChecked =
            DataHelper.firstPage?.canonicalName == requireActivity()::class.java.canonicalName

        bind.configTitle.text = requireActivity()::class.java.simpleName.replace("Activity", "")

        bind.configJumpAuto.setOnCheckedChangeListener { _, isChecked ->
            DataHelper.setFirstPage(if (isChecked) requireActivity()::class else null)
        }
    }

    override fun getTheme() = R.style.App_Fast_Dialog_Bottom
}

interface IConfigDialog : ActivityDispatch {

    override fun onCreateActivity(activity: AppCompatActivity) {
        super.onCreateActivity(activity)
        val last = activity.contentView.parent.to<ViewGroup>().children
            .lastOrNull()?.to<ViewGroup>() // actionBarContainer
            ?.children?.firstOrNull() // toolbar
        last?.setDoubleClickListener {
            onCreateDialog(activity).show()
        }
    }

    fun onCreateDialog(activity: AppCompatActivity): BindBottomSheetDialogFragment = ConfigDialog()
}

class SupportConfigDialog : IConfigDialog {
    override val dispatchers: MutableList<ActivityDispatch> = mutableListOf()
}