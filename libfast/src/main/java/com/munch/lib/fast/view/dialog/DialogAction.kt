package com.munch.lib.fast.view.dialog

import android.content.Context
import android.view.View
import com.munch.lib.android.extend.SealedClassToStringByName

/**
 * Create by munch1182 on 2022/9/20 11:30.
 */
interface IDialogActionKey

sealed class DialogActionKey : SealedClassToStringByName(), IDialogActionKey {
    object Title : DialogActionKey()
    object Content : DialogActionKey()
    object Ok : DialogActionKey()
    object Cancel : DialogActionKey()
}

interface DialogAction<KEY : IDialogActionKey> {
    val key: KEY
    val view: View // action的view
    fun setOnClickListener(l: View.OnClickListener): DialogAction<KEY> {// action点击事件
        view.setOnClickListener(l)
        return this
    }
}

/**
 * 通过传递的[DialogAction], 自行布局并返回最后显示的view
 */
fun interface DialogViewCreator<KEY : IDialogActionKey> {
    fun create(context: Context, map: Map<KEY, DialogAction<KEY>?>): View
}

/**
 * 分解Dialog的结构, 可通过[add]来让外部添加, 并最后通过[customViewCreator]来布局并最后返回自定义的view
 */
open class ActionDialogHelper<KEY : IDialogActionKey>(private var customViewCreator: DialogViewCreator<KEY>) {

    private val map = hashMapOf<KEY, DialogAction<KEY>?>()

    fun add(action: DialogAction<KEY>): ActionDialogHelper<KEY> {
        map[action.key] = action
        return this
    }

    fun setViewCreator(customViewCreator: DialogViewCreator<KEY>): ActionDialogHelper<KEY> {
        this.customViewCreator = customViewCreator
        return this
    }

    fun getContentView(context: Context) = customViewCreator.create(context, map)

    operator fun contains(key: KEY): Boolean = map.containsKey(key)
}