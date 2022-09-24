package com.munch.lib.fast.view.dispatch

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.view.children
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.munch.lib.android.dialog.IDialog
import com.munch.lib.android.extend.*
import com.munch.lib.fast.view.DataHelper
import com.munch.lib.fast.view.dialog.DialogHelper

interface IConfigDialog : ActivityDispatch {

    override fun onCreateActivity(activity: AppCompatActivity) {
        super.onCreateActivity(activity)
        val last = activity.contentView.parent.to<ViewGroup>().children
            .lastOrNull()?.to<ViewGroup>() // actionBarContainer
            ?.children?.firstOrNull() // toolbar
        last?.setDoubleClickListener { onCreateDialog(activity).show() }
    }

    fun onCreateDialog(activity: AppCompatActivity): IDialog =
        DialogHelper.bottom()
            .content(newContent(activity))
            .title(activity::class.java.simpleName.replace("Activity", ""))

    fun newContent(activity: AppCompatActivity): View {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            padding(horizontal = 16.dp2Px2Int())
            newMoreContent(activity)?.let { addView(it) }
            addView(AppCompatCheckBox(activity).apply {
                textSize = 16f
                gravity = Gravity.CENTER_VERTICAL
                clickEffect()

                isChecked =
                    DataHelper.firstPage?.canonicalName == activity::class.java.canonicalName

                text = "默认跳转到此页"

                setOnCheckedChangeListener { _, isChecked ->
                    DataHelper.setFirstPage(if (isChecked) activity::class else null)
                }
            })
        }
    }

    fun newMoreContent(activity: AppCompatActivity): View? = null
}

open class SupportConfigDialog : IConfigDialog {
    override val dispatchers: MutableList<ActivityDispatch> = mutableListOf()
}

abstract class SupportBindConfigDialog<VB : ViewBinding> : IConfigDialog {

    open lateinit var activity: ComponentActivity

    inline fun <reified VM : ViewModel> get(): Lazy<VM> {
        return lazy {
            ViewModelProvider(activity, activity.defaultViewModelProviderFactory)[VM::class.java]
        }
    }

    override fun onCreateActivity(activity: AppCompatActivity) {
        super.onCreateActivity(activity)
        this.activity = activity
    }

    override fun newMoreContent(activity: AppCompatActivity): View? {
        return this::class.java.findParameterized<VB>(ViewBinding::class.java)
            ?.inflate(LayoutInflater.from(activity))
            ?.apply { onCreate(this) }
            ?.root
    }

    abstract fun onCreate(bind: VB)
}