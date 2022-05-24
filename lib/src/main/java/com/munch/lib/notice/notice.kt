package com.munch.lib.notice

import android.content.DialogInterface
import com.munch.lib.OnCancel
import com.munch.lib.Priority


/**
 * Create by munch1182 on 2022/4/22 16:38.
 */
interface Notice {

    fun show()

    fun cancel()

    val priority: Priority
        get() = Priority(0)

    /**
     * 添加当notice取消时的回调
     *
     * 该回调会在notice消失时被自动移除
     *
     * 此方法与[Chose.Cancel]不同，无需主动触发
     */
    fun addOnCancel(onCancel: OnCancel? = null): Notice

    /**
     * 添加选项选择的回调
     *
     * 该回调会在notice消失时被自动移除
     *
     * 此方法的回调不应该主动调用[cancel]
     *
     * @see Chose.Ok
     * @see Chose.Cancel
     */
    fun addOnSelect(chose: OnSelect): Notice

    val isShowing: Boolean

    fun addOnSelectOk(onSelectOk: OnSelectOk): Notice {
        addOnSelect {
            if (it == Chose.Ok) {
                onSelectOk.invoke()
            }
        }
        return this
    }

    fun addOnSelectCancel(OnSelectCancel: OnSelectOk): Notice {
        addOnSelect {
            if (it == Chose.Cancel) {
                OnSelectCancel.invoke()
            }
        }
        return this
    }

}

typealias OnSelect = (chose: Chose) -> Unit
typealias OnSelectOk = () -> Unit
typealias OnSelectCancel = () -> Unit

@JvmInline
value class Chose(val item: Int) {

    companion object {
        val Ok = Chose(DialogInterface.BUTTON_POSITIVE)
        val Cancel = Chose(DialogInterface.BUTTON_NEGATIVE)
    }

    override fun toString(): String {
        return when {
            this == Ok -> "Ok"
            this == Cancel -> "Cancel"
            else -> super.toString()
        }
    }
}