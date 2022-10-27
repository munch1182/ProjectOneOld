package com.munch.lib.fast.view.dialog

import android.content.Context
import android.view.View
import com.munch.lib.android.extend.SealedClassToStringByName

/**
 * ActionDialog使用时分为两部分:
 * 1. contentView的结构, 即[DialogActionKey]
 * 2. contentView的布局, 即[DialogViewCreator]
 * 在布局方向一致时, 即可增删结构快速形成细节不同的dialog
 *
 * Create by munch1182 on 2022/9/20 11:30.
 */

interface IDialogActionKey

sealed class DialogActionKey : SealedClassToStringByName(), IDialogActionKey {
    object Title : DialogActionKey()
    object Content : DialogActionKey()
    object Ok : DialogActionKey()
    object Cancel : DialogActionKey()
    object Background : DialogActionKey()
}

interface DialogAction<KEY : IDialogActionKey> {
    val key: KEY // 用于标记当前的Action在布局中所属的结构
    val view: View // action的view, 可以考虑在view中设置好间距, 也可以将间距放在布局时再考虑
    fun setOnClickListener(l: View.OnClickListener?): DialogAction<KEY> { // action点击事件
        view.setOnClickListener(l)
        return this
    }
}

/**
 * 对[DialogAction]进行布局
 * 已经设置的[DialogAction]在[map]中, 需要自行布局并返回最后显示的view
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

    fun get(key: KEY): DialogAction<KEY>? = map[key]

    operator fun contains(key: KEY): Boolean = map.containsKey(key)
}