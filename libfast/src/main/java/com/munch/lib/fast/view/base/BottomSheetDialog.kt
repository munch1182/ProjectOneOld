package com.munch.lib.fast.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.munch.lib.android.dialog.DialogManager
import com.munch.lib.android.dialog.IDialog
import com.munch.lib.android.extend.to
import java.lang.reflect.Method

open class BindBottomSheetDialogFragment : BottomSheetDialogFragment() {

    var viewBind: ViewBinding? = null
        private set

    protected var method: Method? = null

    protected inline fun <reified VB : ViewBinding> bind(): Lazy<VB> {
        method = VB::class.java.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java
        )
        return lazy { viewBind!!.to() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return viewBind?.root
            ?: inflaterView(inflater, container)
            ?: super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun inflaterView(inflater: LayoutInflater, container: ViewGroup?): View? {
        viewBind = method?.invoke(null, inflater, container, false)?.to()
        return viewBind?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBind = null
    }
}

fun BottomSheetDialogFragment.show() {
    ActivityHelper.curr?.let { show(it.supportFragmentManager, null) }
}

abstract class BottomSheetDialogWrapper : IDialog {
    protected abstract val f: BottomSheetDialogFragment
    override fun show() {
        f.show()
    }

    override fun cancel() {
        f.dismiss()
    }

    override fun getLifecycle() = f.lifecycle
}

@Suppress("NOTHING_TO_INLINE")
inline fun BottomSheetDialogFragment.toDialog(): IDialog = object : BottomSheetDialogWrapper() {
    override val f: BottomSheetDialogFragment = this@toDialog
}

@Suppress("NOTHING_TO_INLINE")
inline fun BottomSheetDialogFragment.offer(m: DialogManager) = m.add(this.toDialog())

