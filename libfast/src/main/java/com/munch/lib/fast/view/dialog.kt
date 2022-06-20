package com.munch.lib.fast.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.munch.lib.extend.*
import com.munch.lib.fast.R
import com.munch.lib.fast.base.BindBottomSheetDialogFragment
import com.munch.lib.fast.base.DataHelper
import com.munch.lib.fast.databinding.LayoutConfigDialogBinding
import java.lang.reflect.Method

/**
 * Created by munch1182 on 2022/4/17 2:15.
 */
open class ConfigDialog : BindBottomSheetDialogFragment() {

    private val bind by bind<LayoutConfigDialogBinding>()

    private val clazz by lazy { activity?.let { it::class.java.canonicalName } }

    var contentViewBinding: ViewBinding? = null
        private set

    protected var contentMethod: Method? = null

    @Suppress("UNCHECKED_CAST")
    protected inline fun <reified VB : ViewBinding> add(): Lazy<VB> {
        contentMethod = VB::class.inflateParent()
        return lazy { contentViewBinding as VB }
    }

    override fun inflaterView(inflater: LayoutInflater, container: ViewGroup?): View? {
        return super.inflaterView(inflater, container)?.toViewGroup()?.apply {
            contentMethod?.inflate(layoutInflater, this, false)?.let {
                showSplit()
                contentViewBinding = it
                addView(it.root, 0)
            }
        }
    }

    protected open fun showSplit() {
        bind.configSplit.visibility = View.VISIBLE
    }

    protected open fun setColor(@ColorInt color: Int) {
        bind.root.setBackgroundColor(color)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind.configStartUp.setOnCheckedChangeListener { _, isChecked -> saveStartUp(isChecked) }

        DataHelper.startUp?.let { it.canonicalName == clazz }
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

        val last = activity.contentView().parentView()?.children
            ?.lastOrNull()?.toViewGroup() // actionBarContainer
            ?.children?.firstOrNull() // toolbar
        last?.setDoubleClickListener {
            onCreateDialog(activity).show(activity.supportFragmentManager, "CONFIG")
        }
    }

    /**
     * 在此方法中创建Activity，此方法只会被调用一次
     */
    fun onCreateDialog(activity: AppCompatActivity): BottomSheetDialogFragment = ConfigDialog()
}

class SupportConfigDialog(
    private val dialogCreator: ((AppCompatActivity) -> BottomSheetDialogFragment)? = null
) : ISupportConfigDialog {

    override val dispatchers: MutableList<ActivityDispatch> = mutableListOf()

    override fun onCreateDialog(activity: AppCompatActivity): BottomSheetDialogFragment {
        return dialogCreator?.invoke(activity) ?: super.onCreateDialog(activity)
    }
}